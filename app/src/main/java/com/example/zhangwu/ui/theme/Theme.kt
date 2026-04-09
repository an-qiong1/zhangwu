package com.example.zhangwu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// 风格改造：重新定义颜色方案，使用柔和的马卡龙配色
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF165DFF), // 主色：柔和蓝
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB5DFFB), // 主色容器：浅天蓝
    onPrimaryContainer = Color(0xFF001A4D),
    secondary = Color(0xFFF8D5E3), // 辅色：淡粉色
    onSecondary = Color(0xFF4A2135),
    secondaryContainer = Color(0xFFF8D5E3), // 辅色容器：淡粉色
    onSecondaryContainer = Color(0xFF4A2135),
    tertiary = Color(0xFFD8C4E6), // 第三色：淡紫色
    onTertiary = Color(0xFF3E2A50),
    tertiaryContainer = Color(0xFFD8C4E6), // 第三色容器：淡紫色
    onTertiaryContainer = Color(0xFF3E2A50),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF8F9FA), // 背景：极浅灰
    onBackground = Color(0xFF121212),
    surface = Color(0xFFFFFFFF), // 卡片：纯白
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFE5E6EB),
    outlineVariant = Color(0xFFD1D5DB)
)

// 风格改造：重新定义深色模式颜色方案
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF63A3FF), // 主色：亮蓝
    onPrimary = Color(0xFF001A4D),
    primaryContainer = Color(0xFF002F66),
    onPrimaryContainer = Color(0xFFE6F0FF),
    secondary = Color(0xC28DA0), // 辅色：深粉
    onSecondary = Color(0xFFF8D5E3),
    secondaryContainer = Color(0x4A2135),
    onSecondaryContainer = Color(0xFFF8D5E3),
    tertiary = Color(0xA58BBE), // 第三色：深紫
    onTertiary = Color(0xFFD8C4E6),
    tertiaryContainer = Color(0x3E2A50),
    onTertiaryContainer = Color(0xFFD8C4E6),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0x8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1E1E1E), // 背景：深灰
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF2A2A2A), // 卡片：深灰
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0x3A3A3A),
    onSurfaceVariant = Color(0xE0E0E0),
    outline = Color(0x3A3A3A),
    outlineVariant = Color(0x4D4D4D)
)

// 风格改造：重新定义形状，使用大圆角
private val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp), // 中圆角：16dp
    large = RoundedCornerShape(20.dp), // 大圆角：20dp
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun ZhangwuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true, // 启用动态颜色，使用Material You
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes, // 风格改造：使用新的形状定义
        content = content
    )
}