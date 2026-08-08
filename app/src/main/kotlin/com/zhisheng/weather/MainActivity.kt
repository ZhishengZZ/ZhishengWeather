package com.zhisheng.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhisheng.weather.data.SettingsRepository
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
        // 灭屏时启动 → 点亮屏幕。API 27+ 用 setShowWhenLocked/setTurnScreenOn，
        // 旧的 SCREEN_BRIGHT_WAKE_LOCK 在 27+ 已废弃且实际无效（v0.0.2）
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        enableEdgeToEdge()
        setContent {
            ZhishengWeatherTheme {
                val vm: WeatherViewModel = viewModel()
                // rememberSaveable：旋转/进程重建后仍停在原来那屏（v0.0.2）
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                val uiState by vm.uiState.collectAsState()

                // 常亮屏幕（设置项）
                val keepOn by SettingsRepository.keepScreenOn.collectAsState(initial = false)
                val view = LocalView.current
                DisposableEffect(keepOn) {
                    view.keepScreenOn = keepOn
                    onDispose { view.keepScreenOn = false }
                }

                // 系统返回键：搜索/设置页退回主屏，而不是直接退出 App（v0.0.2）
                BackHandler(enabled = screen != Screen.HOME) {
                    screen = Screen.HOME
                }

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
                        Screen.SETTINGS -> SettingsScreen(
                            onBack = { screen = Screen.HOME },
                            onLocate = { vm.locateCurrentCity() },
                            locating = uiState.locating,
                            locateMessage = uiState.locateMessage,
                            onClearLocateMessage = { vm.clearLocateMessage() },
                        )
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
