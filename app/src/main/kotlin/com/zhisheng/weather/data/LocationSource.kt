package com.zhisheng.weather.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.zhisheng.weather.model.City
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

// 定位（v0.0.2）——严格可选：
// · 权限只在用户主动点「定位当前城市」时申请，App 启动/刷新绝不触碰位置
// · 只用系统 LocationManager，不引入 Google Play 服务
// · 只申请 COARSE（城市级足够），拒绝后功能照常可用（手动搜城市）
// · 拿到坐标后用免 key 接口反查中文城市名
object LocationSource {

    const val PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION

    sealed interface Result {
        data class Ok(val city: City) : Result
        data class Failed(val message: String) : Result
    }

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED

    fun locationEnabledOnDevice(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return try {
            LocationManagerCompatIsEnabled(lm)
        } catch (_: Exception) {
            false
        }
    }

    private fun LocationManagerCompatIsEnabled(lm: LocationManager): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) lm.isLocationEnabled
        else lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER)

    // 定位 + 反查城市。调用前必须已确认权限（由 UI 层申请），此处再兜一次校验。
    suspend fun locate(context: Context): Result {
        if (!hasPermission(context)) return Result.Failed("未授予位置权限")
        if (!locationEnabledOnDevice(context)) return Result.Failed("系统定位服务未开启")

        val loc = withTimeoutOrNull(12_000L) { currentLocation(context) }
            ?: return Result.Failed("定位超时，请到空旷处重试或手动搜索城市")

        return when (val c = reverseGeocode(loc.latitude, loc.longitude)) {
            null -> Result.Failed("已取到坐标但未能反查城市名，请手动搜索")
            else -> Result.Ok(c)
        }
    }

    @Suppress("MissingPermission")
    private suspend fun currentLocation(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        // 先用最近一次缓存位置（5 分钟内即可用，省一次定位耗电）
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        val cached = providers.mapNotNull { p -> runCatching { lm.getLastKnownLocation(p) }.getOrNull() }
            .maxByOrNull { it.time }
        if (cached != null && System.currentTimeMillis() - cached.time < 5 * 60_000L) return cached

        val provider = providers.firstOrNull() ?: return cached

        // 单次定位请求（回调在主线程 Looper 上注册）
        return suspendCancellableCoroutine { cont ->
            val listener = object : android.location.LocationListener {
                private var done = false
                override fun onLocationChanged(location: Location) {
                    if (done) return
                    done = true
                    runCatching { lm.removeUpdates(this) }
                    if (cont.isActive) cont.resume(location)
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
                override fun onProviderEnabled(provider: String) = Unit
                override fun onProviderDisabled(provider: String) {
                    if (done) return
                    done = true
                    runCatching { lm.removeUpdates(this) }
                    if (cont.isActive) cont.resume(null)
                }
            }
            try {
                lm.requestLocationUpdates(
                    provider, 0L, 0f, listener,
                    android.os.Looper.getMainLooper(),
                )
            } catch (_: Exception) {
                if (cont.isActive) cont.resume(cached)
                return@suspendCancellableCoroutine
            }
            cont.invokeOnCancellation { runCatching { lm.removeUpdates(listener) } }
        }
    }

    // 坐标 → 中文城市。小米 geo 接口免 key 且直接给 locationKey + 归属地，优先用；
    // 失败退和风 GeoAPI（按坐标 lookup）。两者都失败则如实报错，不猜城市。
    private suspend fun reverseGeocode(lat: Double, lon: Double): City? =
        xiaomiReverse(lat, lon) ?: qweatherReverse(lat, lon)

    private suspend fun xiaomiReverse(lat: Double, lon: Double): City? = try {
        XiaomiApi.instance.geoCity(latitude = lat, longitude = lon)
            .firstOrNull { it.status == 0 && !it.locationKey.isNullOrBlank() }
            ?.let { h ->
                City(
                    name = h.name.orEmpty().ifBlank { return null },
                    affiliation = h.affiliation.orEmpty().split(",").map { it.trim() }
                        .filter { it.isNotBlank() && it != "中国" }.reversed().joinToString("·"),
                    latitude = h.latitude?.toDoubleOrNull() ?: lat,
                    longitude = h.longitude?.toDoubleOrNull() ?: lon,
                    locationKey = h.locationKey!!,
                )
            }
    } catch (ce: kotlinx.coroutines.CancellationException) {
        throw ce
    } catch (_: Exception) {
        null
    }

    private suspend fun qweatherReverse(lat: Double, lon: Double): City? {
        if (!QWeatherApi.enabled) return null
        return try {
            val loc = QWeatherApi.service
                .cityLookup(String.format(java.util.Locale.US, "%.2f,%.2f", lon, lat))
                .location.firstOrNull() ?: return null
            City(
                name = loc.name.orEmpty().ifBlank { return null },
                affiliation = listOfNotNull(loc.adm1, loc.adm2)
                    .filter { it.isNotBlank() }.distinct().joinToString("·"),
                latitude = loc.lat?.toDoubleOrNull() ?: lat,
                longitude = loc.lon?.toDoubleOrNull() ?: lon,
                locationKey = loc.id ?: "$lon,$lat",
            )
        } catch (ce: kotlinx.coroutines.CancellationException) {
            throw ce
        } catch (_: Exception) {
            null
        }
    }

}
