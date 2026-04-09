package com.example.zhangwu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.example.zhangwu.ui.theme.ZhangwuTheme
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 设置状态栏颜色以匹配当前主题
 * 浅色主题：状态栏背景浅色，文字黑色
 * 深色主题：状态栏背景深色，文字白色
 */
@Composable
fun SetSystemBarColors(
    darkTheme: Boolean = isSystemInDarkTheme(),
    statusBarColor: Color = MaterialTheme.colorScheme.surface,
    navigationBarColor: Color = MaterialTheme.colorScheme.surface
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(darkTheme, statusBarColor, navigationBarColor) {
            val window = (view.context as androidx.activity.ComponentActivity).window
            val windowInsetsController = WindowCompat.getInsetsController(window, view)

            // 设置状态栏和导航栏颜色
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()

            // 根据背景亮度决定状态栏图标颜色
            val isLightStatusBar = statusBarColor.luminance() > 0.5f
            windowInsetsController.isAppearanceLightStatusBars = isLightStatusBar

            // 根据背景亮度决定导航栏图标颜色
            val isLightNavigationBar = navigationBarColor.luminance() > 0.5f
            windowInsetsController.isAppearanceLightNavigationBars = isLightNavigationBar

            onDispose {}
        }
    }
}

/**
 * 完全透明的状态栏，内容延伸到状态栏后面
 * 适用于需要全屏沉浸式体验的场景
 */
@Composable
fun TransparentSystemBars() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as androidx.activity.ComponentActivity).window
            val windowInsetsController = WindowCompat.getInsetsController(window, view)

            // 设置透明状态栏和导航栏
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            // 启用全屏布局，内容延伸到系统栏后面
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // 默认使用深色图标（适合深色背景）
            windowInsetsController.isAppearanceLightStatusBars = false
            windowInsetsController.isAppearanceLightNavigationBars = false

            onDispose {
                // 恢复默认
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        }
    }
}

/**
 * 根据当前主题自动适配系统栏颜色
 * 在页面顶层调用，确保状态栏和导航栏颜色与主题一致
 */
@Composable
fun AdaptiveSystemBars() {
    val isDarkTheme = isSystemInDarkTheme()
    val statusBarColor = MaterialTheme.colorScheme.surface
    val navigationBarColor = MaterialTheme.colorScheme.surface

    SetSystemBarColors(
        darkTheme = isDarkTheme,
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor
    )
}

/**
 * 获取状态栏高度，用于自定义布局
 */
val WindowInsets.Companion.statusBarHeight
    @Composable
    get() = statusBars

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun SetSystemBarColorsPreview() {
    ZhangwuTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "SetSystemBarColors Preview")
                SetSystemBarColors()
            }
        }
    }
}
