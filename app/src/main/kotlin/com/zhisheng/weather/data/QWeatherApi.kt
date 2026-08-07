package com.zhisheng.weather.data

import com.zhisheng.weather.BuildConfig
import java.util.Locale
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// 和风天气 API（新版 v1 + 旧版 v7 混合，JWT Bearer 认证）
interface QWeatherService {

    @GET("weather/v1/current/{lat}/{lon}")
    suspend fun current(
        @Path("lat") lat: String,
        @Path("lon") lon: String,
        @Query("localTime") localTime: Boolean = true,
    ): QwCurrent

    @GET("weather/v1/hourly/{lat}/{lon}")
    suspend fun hourly(
        @Path("lat") lat: String,
        @Path("lon") lon: String,
        @Query("localTime") localTime: Boolean = true,
    ): QwHourly

    @GET("weather/v1/daily/{lat}/{lon}")
    suspend fun daily(
        @Path("lat") lat: String,
        @Path("lon") lon: String,
        @Query("days") days: Int,
        @Query("localTime") localTime: Boolean = true,
        @Query("lang") lang: String = "zh",
    ): QwDaily

    @GET("weatheralert/v1/current/{lat}/{lon}")
    suspend fun alerts(
        @Path("lat") lat: String,
        @Path("lon") lon: String,
        @Query("localTime") localTime: Boolean = true,
    ): QwAlerts

    @GET("airquality/v1/current/{lat}/{lon}")
    suspend fun air(@Path("lat") lat: String, @Path("lon") lon: String): QwAir

    @GET("v7/minutely/5m")
    suspend fun minutely(@Query("location") location: String): QwMinutely

    @GET("v7/indices/1d")
    suspend fun indices(
        @Query("location") location: String,
        @Query("type") type: String,
    ): QwIndices

    @GET("geo/v2/city/lookup")
    suspend fun cityLookup(
        @Query("location") query: String,
        @Query("number") number: Int = 10,
    ): QwCityLookup
}

object QWeatherApi {

    // local.properties 未配置凭据时为 false，自动走小米源
    val enabled: Boolean
        get() = BuildConfig.QW_HOST.isNotBlank() &&
            BuildConfig.QW_PROJECT_ID.isNotBlank() &&
            BuildConfig.QW_PRIVATE_KEY.isNotBlank()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request()
        val token = QwAuth.token()
        val first = if (token != null) {
            req.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            req
        }
        var resp = chain.proceed(first)
        // 401 = token 被拒（时钟漂移/凭据作废）：作废缓存重签一次再试（v1.2.4）
        if (resp.code == 401 && token != null) {
            resp.close()
            QwAuth.invalidate()
            val t2 = QwAuth.token()
            if (t2 != null) {
                resp = chain.proceed(req.newBuilder().header("Authorization", "Bearer $t2").build())
            } else {
                resp = chain.proceed(first)
            }
        }
        resp
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    val service: QWeatherService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.QW_HOST.trimEnd('/') + "/")
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(QWeatherService::class.java)
    }

    fun lat(v: Double) = String.format(Locale.US, "%.2f", v)
    fun lonLat(c: com.zhisheng.weather.model.City) =
        String.format(Locale.US, "%.2f,%.2f", c.longitude, c.latitude)
}
