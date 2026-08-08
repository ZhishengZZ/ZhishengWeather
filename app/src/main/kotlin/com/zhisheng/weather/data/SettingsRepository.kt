package com.zhisheng.weather.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// 数据源偏好：AUTO 保持既有降级链（和风→小米→公共源），其余为手动锁定单一源。
// 装不上和风的用户锁 OPEN_METEO 即可拿到完整体验（实况/逐时/逐日/空气质量全免 key）。
enum class SourcePref(val key: String, val cn: String, val en: String, val desc: String) {
    AUTO("auto", "自动优选", "AUTO", "和风→小米→公共源自动降级"),
    QWEATHER("qweather", "和风天气", "QWEATHER", "需自备凭据·全功能"),
    XIAOMI("xiaomi", "小米天气", "XIAOMI", "免配置·国内"),
    OPEN_METEO("openmeteo", "Open-Meteo", "OPEN-METEO", "免配置·全球");

    companion object {
        fun from(v: String?): SourcePref = entries.firstOrNull { it.key == v } ?: AUTO
    }
}

// 氛围层强度：克制为默认（背景层低透明度，绝不压信息）
enum class AmbienceLevel(val key: String, val cn: String, val factor: Float) {
    OFF("off", "关闭", 0f),
    SUBTLE("subtle", "克制", 1f),
    VIVID("vivid", "明显", 1.9f);

    companion object {
        fun from(v: String?): AmbienceLevel = entries.firstOrNull { it.key == v } ?: SUBTLE
    }
}

// 设置仓储
object SettingsRepository {

    private lateinit var store: DataStore<Preferences>

    private val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
    private val KEY_SHOW_TYPHOON = booleanPreferencesKey("show_typhoon")
    private val KEY_SOURCE = stringPreferencesKey("source_pref")
    private val KEY_AMBIENCE = stringPreferencesKey("ambience")
    private val KEY_SCANLINES = booleanPreferencesKey("scanlines")
    private val KEY_LOCATION_ENABLED = booleanPreferencesKey("location_enabled")
    private val KEY_WIND_UNIT = stringPreferencesKey("wind_unit")
    private val KEY_PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
    private val KEY_SHOW_AQI = booleanPreferencesKey("show_aqi")
    private val KEY_SHOW_INDICES = booleanPreferencesKey("show_indices")
    private val KEY_SHOW_YESTERDAY = booleanPreferencesKey("show_yesterday")
    private val KEY_SHOW_PRECIP = booleanPreferencesKey("show_precip")
    private val KEY_SHOW_TELEMETRY = booleanPreferencesKey("show_telemetry")
    private val KEY_BOOT_ANIM = booleanPreferencesKey("boot_anim")
    private val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")

    fun init(context: Context) {
        store = context.applicationContext.settingsStore
    }

    // c=摄氏度 f=华氏度
    val tempUnit: Flow<String> by lazy { store.data.map { it[KEY_TEMP_UNIT] ?: "c" } }
    val showTyphoon: Flow<Boolean> by lazy { store.data.map { it[KEY_SHOW_TYPHOON] ?: true } }
    val sourcePref: Flow<SourcePref> by lazy { store.data.map { SourcePref.from(it[KEY_SOURCE]) } }
    val ambience: Flow<AmbienceLevel> by lazy { store.data.map { AmbienceLevel.from(it[KEY_AMBIENCE]) } }
    val scanlines: Flow<Boolean> by lazy { store.data.map { it[KEY_SCANLINES] ?: true } }
    // 定位总开关：关闭时 App 完全不碰位置权限（默认关，兑现「不主动获取权限」）
    val locationEnabled: Flow<Boolean> by lazy { store.data.map { it[KEY_LOCATION_ENABLED] ?: false } }
    // kmh / ms / bft
    val windUnit: Flow<String> by lazy { store.data.map { it[KEY_WIND_UNIT] ?: "kmh" } }
    // hpa / mmhg / inhg
    val pressureUnit: Flow<String> by lazy { store.data.map { it[KEY_PRESSURE_UNIT] ?: "hpa" } }
    val showAqi: Flow<Boolean> by lazy { store.data.map { it[KEY_SHOW_AQI] ?: true } }
    val showIndices: Flow<Boolean> by lazy { store.data.map { it[KEY_SHOW_INDICES] ?: true } }
    val showYesterday: Flow<Boolean> by lazy { store.data.map { it[KEY_SHOW_YESTERDAY] ?: true } }
    val showPrecip: Flow<Boolean> by lazy { store.data.map { it[KEY_SHOW_PRECIP] ?: true } }
    val showTelemetry: Flow<Boolean> by lazy { store.data.map { it[KEY_SHOW_TELEMETRY] ?: true } }
    val bootAnim: Flow<Boolean> by lazy { store.data.map { it[KEY_BOOT_ANIM] ?: true } }
    val keepScreenOn: Flow<Boolean> by lazy { store.data.map { it[KEY_KEEP_SCREEN_ON] ?: false } }

    suspend fun setTempUnit(unit: String) = store.edit { it[KEY_TEMP_UNIT] = unit }
    suspend fun setShowTyphoon(show: Boolean) = store.edit { it[KEY_SHOW_TYPHOON] = show }
    suspend fun setSourcePref(p: SourcePref) = store.edit { it[KEY_SOURCE] = p.key }
    suspend fun setAmbience(a: AmbienceLevel) = store.edit { it[KEY_AMBIENCE] = a.key }
    suspend fun setScanlines(v: Boolean) = store.edit { it[KEY_SCANLINES] = v }
    suspend fun setLocationEnabled(v: Boolean) = store.edit { it[KEY_LOCATION_ENABLED] = v }
    suspend fun setWindUnit(v: String) = store.edit { it[KEY_WIND_UNIT] = v }
    suspend fun setPressureUnit(v: String) = store.edit { it[KEY_PRESSURE_UNIT] = v }
    suspend fun setShowAqi(v: Boolean) = store.edit { it[KEY_SHOW_AQI] = v }
    suspend fun setShowIndices(v: Boolean) = store.edit { it[KEY_SHOW_INDICES] = v }
    suspend fun setShowYesterday(v: Boolean) = store.edit { it[KEY_SHOW_YESTERDAY] = v }
    suspend fun setShowPrecip(v: Boolean) = store.edit { it[KEY_SHOW_PRECIP] = v }
    suspend fun setShowTelemetry(v: Boolean) = store.edit { it[KEY_SHOW_TELEMETRY] = v }
    suspend fun setBootAnim(v: Boolean) = store.edit { it[KEY_BOOT_ANIM] = v }
    suspend fun setKeepScreenOn(v: Boolean) = store.edit { it[KEY_KEEP_SCREEN_ON] = v }
}
