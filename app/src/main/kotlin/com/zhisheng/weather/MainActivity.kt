package com.zhisheng.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhisheng.weather.model.City
import com.zhisheng.weather.ui.SearchScreen
import com.zhisheng.weather.ui.WeatherViewModel
import com.zhisheng.weather.ui.home.HomeScreen
import com.zhisheng.weather.ui.SettingsScreen
import com.zhisheng.weather.ui.theme.ZhishengWeatherTheme

private enum class Screen { HOME, SEARCH, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 屏幕熄灭时启动 → 点亮屏幕（终端型应用打开即可见）
        val pm = getSystemService(POWER_SERVICE) as android.os.PowerManager
        if (!pm.isInteractive) {
            pm.newWakeLock(
                android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "zhisheng:wake",
            ).acquire(15_000)
        }
        enableEdgeToEdge()
        setContent {
            ZhishengWeatherTheme {
                val vm: WeatherViewModel = viewModel()
                var screen by remember { mutableStateOf(Screen.HOME) }

                // 每次打开 / 回到前台都拉最新天气（10 分钟内同城不重复拉）
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    vm.refresh(force = false)
                }

                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        if (targetState == Screen.SEARCH) {
                            (fadeIn(tween(260)) + slideInHorizontally { it / 3 }) togetherWith
                                (fadeOut(tween(180)) + slideOutHorizontally { -it / 3 })
                        } else {
                            (fadeIn(tween(260)) + slideInHorizontally { -it / 3 }) togetherWith
                                (fadeOut(tween(180)) + slideOutHorizontally { it / 3 })
                        }
                    },
                    label = "screen",
                ) { current ->
                    when (current) {
                        Screen.SETTINGS -> SettingsScreen(onBack = { screen = Screen.HOME })
                        Screen.SEARCH -> SearchScreen(
                            onCityPicked = { city: City ->
                                vm.addCityAndSelect(city)
                                screen = Screen.HOME
                            },
                            onBack = { screen = Screen.HOME },
                        )
                        Screen.HOME -> HomeScreen(
                            viewModel = vm,
                            onSearchClick = { screen = Screen.SEARCH },
                            onSettingsClick = { screen = Screen.SETTINGS },
                        )
                    }
                }
            }
        }
    }
}
