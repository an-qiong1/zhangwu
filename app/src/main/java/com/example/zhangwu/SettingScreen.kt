package com.example.zhangwu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhangwu.ui.theme.ZhangwuTheme
import com.example.zhangwu.FeedbackScreen
import com.example.zhangwu.RewardScreen
import com.example.zhangwu.BackupRestoreScreen
import com.example.zhangwu.model.Asset
import com.example.zhangwu.model.WishItem
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.zhangwu.viewmodel.ThemeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import androidx.lifecycle.ViewModel

// 全局主题状态的CompositionLocal
val LocalThemeState = compositionLocalOf<MutableState<com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode>> {
    error("ThemeState未初始化")
}

@Composable
fun SettingScreen(
    assetsList: List<Asset> = emptyList(),
    wishlistItems: List<WishItem> = emptyList(),
    categoriesList: List<String> = listOf("全部", "通勤代步", "文娱数码", "穿戴配饰"),
    onOpenCategoryManager: () -> Unit = {},
    onOpenBackupRestore: () -> Unit = {},
    onOpenFeedback: () -> Unit = {},
    onOpenReward: () -> Unit = {},
    onRestoreSuccess: (List<Asset>, List<WishItem>, List<String>) -> Unit = { _, _, _ -> }
) {
    val themeMode = LocalThemeState.current
    val context = LocalContext.current
    // 修复：使用ViewModelProvider创建ThemeViewModel实例
    val themeViewModel: ThemeViewModel = viewModel()
    val viewModelThemeMode by themeViewModel.themeMode.collectAsState()
    
    // 修复：同步ThemeViewModel的状态到本地ThemeMode
    LaunchedEffect(viewModelThemeMode) {
        themeMode.value = viewModelThemeMode
    }
    
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Material You适配：使用动态主题色
    ) {
        // 统一标题（和资产页完全一致，无搜索）
        CommonPageTitle(title = "设置")

        // 改造：可滚动布局，解决底部Tab栏遮挡问题
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp), // 改造：调整底部内边距，确保版本号显示在tab栏上方
            verticalArrangement = Arrangement.spacedBy(20.dp) // 风格改造：增加间距
        ) {
            item {
                // 风格改造：大圆角主题模式卡片
                Card(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large, // 风格改造：使用大圆角
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // 风格改造：增加卡片阴影
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Material You适配：使用动态主题色
                ) {
                    Column(Modifier.padding(24.dp)) { // 风格改造：增加内边距
                        Text(
                            "主题模式", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface // Material You适配：使用动态主题色
                        )
                        Spacer(modifier = Modifier.height(12.dp)) // 风格改造：增加间距
                        ThemeOption("跟随系统", themeMode.value == com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.FOLLOW_SYSTEM) { themeMode.value = com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.FOLLOW_SYSTEM }
                        ThemeOption("浅色模式", themeMode.value == com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.LIGHT) { themeMode.value = com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.LIGHT }
                        ThemeOption("深色模式", themeMode.value == com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.DARK) { themeMode.value = com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.DARK }
                    }
                }
            }
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp) // 风格改造：增加间距
                ) {
                    SettingItem("分类管理", Icons.Default.Category, onClick = onOpenCategoryManager)
                    SettingItem("备份与恢复", Icons.Default.Backup, onClick = onOpenBackupRestore)
                    SettingItem("问题反馈", Icons.Default.Feedback, onClick = onOpenFeedback)
                    SettingItem("打赏一下", Icons.Default.MonetizationOn, onClick = onOpenReward)
                    SettingItem("开源地址", Icons.Default.Code, onClick = { Toast.makeText(context, "https://github.com/yourusername/zhangwu", Toast.LENGTH_LONG).show() })
                }
            }
            item {
                // 版本号显示
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "版本 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOption(title: String, selected: Boolean, onClick: () -> Unit) {
    // 修复：使用ThemeViewModel管理主题状态
    val themeViewModel: ThemeViewModel = viewModel()
    
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                // 修复：先更新ThemeViewModel的状态，再调用onClick
                when (title) {
                    "跟随系统" -> themeViewModel.setThemeMode(com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.FOLLOW_SYSTEM)
                    "浅色模式" -> themeViewModel.setThemeMode(com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.LIGHT)
                    "深色模式" -> themeViewModel.setThemeMode(com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.DARK)
                }
                onClick()
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = {
                // 修复：先更新ThemeViewModel的状态，再调用onClick
                when (title) {
                    "跟随系统" -> themeViewModel.setThemeMode(com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.FOLLOW_SYSTEM)
                    "浅色模式" -> themeViewModel.setThemeMode(com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.LIGHT)
                    "深色模式" -> themeViewModel.setThemeMode(com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.DARK)
                }
                onClick()
            },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary, // Material You适配：使用动态主色
                unselectedColor = MaterialTheme.colorScheme.outline // Material You适配：使用动态主题色
            )
        )
        Text(
            title, 
            Modifier.padding(start = 8.dp),
            color = MaterialTheme.colorScheme.onSurface // Material You适配：使用动态主题色
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {}
) {
    // 风格改造：大圆角设置项卡片
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large, // 风格改造：使用大圆角
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // 风格改造：增加卡片阴影
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Material You适配：使用动态主题色
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { // 风格改造：增加内边距
            Box(Modifier
                .size(48.dp) // 风格改造：增大图标容器
                .clip(MaterialTheme.shapes.medium) // 风格改造：使用中圆角
                .background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) { // Material You适配：使用动态主题色
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) // Material You适配：使用动态主色
            }
            Text(
                title, 
                Modifier
                    .weight(1f)
                    .padding(start = 16.dp), 
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface // Material You适配：使用动态主题色
            ) // 风格改造：增大字体
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos, 
                null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Material You适配：使用动态主题色
            ) // 风格改造：添加图标颜色
        }
    }
}

@Preview
@Composable
fun SettingPreview() {
    val themeState = remember { mutableStateOf(com.example.zhangwu.viewmodel.ThemeViewModel.ThemeMode.FOLLOW_SYSTEM) }
    CompositionLocalProvider(LocalThemeState provides themeState) {
        ZhangwuTheme { SettingScreen() }
    }
}

