package com.example.zhangwu

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.zhangwu.ui.theme.ZhangwuTheme
import com.example.zhangwu.LocalThemeState
import com.example.zhangwu.viewmodel.ThemeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.example.zhangwu.webdav.AutoSyncHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // 修复：添加返回键时间戳，用于双击退出
    private var lastBackPressedTime = 0L

    // WebDAV 自动同步助手
    private lateinit var autoSyncHelper: AutoSyncHelper

    // ProcessLifecycleObserver：监听 APP 前后台切换
    private val appLifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onAppBackgrounded() {
            // 退出 APP 时自动上传到坚果云
            lifecycleScope.launch {
                autoSyncHelper.autoUpload()
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onAppForegrounded() {
            // 进入 APP 时检查是否需要首次同步
            lifecycleScope.launch {
                val needRestore = autoSyncHelper.checkFirstSync()
                if (needRestore) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "云端有备份，可前往「备份与恢复」页面恢复",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 启用 Android 15/16 默认的沉浸式边缘到边缘显示 (Edge-to-Edge)
        enableEdgeToEdge()
        Log.d("MainActivity", "onCreate started")
        super.onCreate(savedInstanceState)

        // 初始化自动同步助手
        autoSyncHelper = AutoSyncHelper(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        setContent {
            // 修复：使用ViewModelProvider创建ThemeViewModel实例
            val themeViewModel: ThemeViewModel = viewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            // 根据ThemeMode计算是否启用深色主题
            val darkTheme = themeViewModel.isDarkTheme(isSystemInDarkTheme())

            // 修复：使用viewmodel中的ThemeMode枚举
            val localThemeMode = remember {
                mutableStateOf(themeMode)
            }

            // 修复：监听ThemeViewModel状态变化，更新本地ThemeMode
            LaunchedEffect(themeMode) {
                localThemeMode.value = themeMode
            }

            // 修复：添加子页面状态管理，用于处理系统返回键
            val isSubScreenVisible = rememberSaveable { mutableStateOf(false) }
            val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
            val backPressedDispatcher = backPressedDispatcherOwner?.onBackPressedDispatcher

            CompositionLocalProvider(LocalThemeState provides localThemeMode, LocalSubScreenState provides isSubScreenVisible) {
                ZhangwuTheme(darkTheme = darkTheme) {
                    // 根据主题适配状态栏和导航栏文字颜色
                    val view = LocalView.current
                    val colorScheme = MaterialTheme.colorScheme
                    DisposableEffect(Unit) {
                        val window = (view.context as ComponentActivity).window
                        val windowInsetsController = WindowCompat.getInsetsController(window, view)
                        // 根据背景颜色亮度决定状态栏和导航栏图标颜色
                        val useLightIcons = colorScheme.surface.luminance() < 0.5f
                        windowInsetsController.isAppearanceLightStatusBars = useLightIcons
                        windowInsetsController.isAppearanceLightNavigationBars = useLightIcons

                        onDispose {}
                    }

                    // 修复：处理系统返回键
                    BackHandler {
                        if (isSubScreenVisible.value) {
                            // 如果有子页面，返回上一页
                            // 子页面的BackHandler会处理返回逻辑
                        } else {
                            // 如果在首页，提示再按一次退出
                            if (System.currentTimeMillis() - lastBackPressedTime < 2000) {
                                finish()
                            } else {
                                Toast.makeText(this@MainActivity, "再按一次退出应用", Toast.LENGTH_SHORT).show()
                                lastBackPressedTime = System.currentTimeMillis()
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ZhangwuAppScreen()
                    }
                }
            }
        }
    }
}

// 修复：添加子页面状态的CompositionLocal
val LocalSubScreenState = compositionLocalOf<MutableState<Boolean>> {
    error("SubScreenState未初始化")
}



