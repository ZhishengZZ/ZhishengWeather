package com.zhisheng.weather

import android.app.Application
import com.zhisheng.weather.data.CityRepository
import com.zhisheng.weather.data.SettingsRepository

class ZhishengApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CityRepository.init(this)
        SettingsRepository.init(this)
    }
}
