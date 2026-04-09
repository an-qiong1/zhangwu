package com.example.zhangwu

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.zhangwu.model.Asset
import com.example.zhangwu.model.WishlistItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.zhangwu.CommonPageTitle
import com.example.zhangwu.ui.theme.ZhangwuTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 备份恢复页面（纯本地版）
 * 功能：导出数据到本地JSON文件 / 从本地导入JSON备份文件
 * 修复点：布局间距、弹窗交互、文本排版、点击体验
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onBackClick: () -> Unit,
    // 传入需要备份的所有数据
    assetsList: List<Asset>,
    wishlistItems: List<WishlistItem>,
    categoriesList: List<String>,
    // 恢复数据后的回调
    onRestoreSuccess: (List<Asset>, List<WishlistItem>, List<String>) -> Unit
) {
    val context = LocalContext.current
    val gson = Gson()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 加载/确认弹窗状态
    var showLoadingDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    // 控制弹窗自动关闭的Job
    var dialogDismissJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    
    // 文件保存launcher，用于保存备份文件到Download文件夹
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        
        showLoadingDialog = true
        dialogMessage = "正在导出数据..."
        
        // 取消之前的自动关闭任务
        dialogDismissJob?.cancel()
        
        // 构建备份数据对象
        val backupData = BackupData(
            assets = assetsList,
            wishlist = wishlistItems,
            categories = categoriesList,
            backupTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
        )
        
        try {
            // 写入JSON文件到SAF返回的URI
            val json = gson.toJson(backupData)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            dialogMessage = "备份成功！\n文件已保存到Download文件夹"
        } catch (e: Exception) {
            dialogMessage = "备份失败：${e.message}"
        } finally {
            // 延迟关闭弹窗，让用户看到提示（支持手动提前关闭）
            dialogDismissJob = scope.launch {
                delay(3000)
                showLoadingDialog = false
            }
        }
    }

    // ===================== 数据备份（导出） =====================
    fun exportData() {
        // 备份文件名：掌物_备份_20240520_1530.json
        val fileName = "掌物_备份_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.CHINA).format(Date())}.json"
        // 启动文件保存对话框，让用户选择保存位置（默认是Download文件夹）
        saveFileLauncher.launch(fileName)
    }

    // ===================== 数据恢复（导入） =====================
    // 文件选择器（选择本地JSON备份文件）
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        showLoadingDialog = true
        dialogMessage = "正在恢复数据..."

        // 取消之前的自动关闭任务
        dialogDismissJob?.cancel()

        try {
            // 读取选中的JSON文件
            val inputStream = context.contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader()?.readText()
            inputStream?.close()

            if (json.isNullOrBlank()) {
                dialogMessage = "备份文件为空"
                return@rememberLauncherForActivityResult
            }

            // 解析JSON数据
            val type = object : TypeToken<BackupData>() {}.type
            val backupData = gson.fromJson<BackupData>(json, type)

            // 回调更新数据
            onRestoreSuccess(
                backupData.assets,
                backupData.wishlist,
                backupData.categories
            )

            dialogMessage = "恢复成功！\n备份时间：${backupData.backupTime}"
        } catch (e: Exception) {
            dialogMessage = "恢复失败：${e.message}"
        } finally {
            // 延迟关闭弹窗
            dialogDismissJob = scope.launch {
                delay(3000)
                showLoadingDialog = false
            }
        }
    }

    // ===================== 页面布局 =====================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .systemBarsPadding()
        ) {
            // 顶部标题栏
            CommonPageTitle(
                title = "备份与恢复",
                showBackButton = true,
                onBackClick = onBackClick
            )

            // 主内容区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // 风格改造：大圆角备份按钮卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = MaterialTheme.shapes.large, // 风格改造：使用大圆角
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    onClick = { exportData() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp), // 风格改造：增加内边距
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp) // 风格改造：增大图标容器
                                .clip(RoundedCornerShape(16.dp)) // 风格改造：使用中圆角
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "导出备份",
                                modifier = Modifier.size(32.dp), // 风格改造：增大图标
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp)) // 风格改造：增加间距
                        Text(
                            "导出数据备份",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp)) // 风格改造：增加间距
                        Text(
                            text = "将所有资产、心愿单、分类数据导出为JSON文件\n保存路径：Download文件夹",
                            style = MaterialTheme.typography.bodyMedium, // 风格改造：增大字体
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp // 风格改造：增大行高
                        )
                    }
                }

                // 风格改造：大圆角恢复按钮卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = MaterialTheme.shapes.large, // 风格改造：使用大圆角
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    onClick = {
                        // 打开文件选择器，仅显示JSON文件
                        importFileLauncher.launch(arrayOf("application/json"))
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp), // 风格改造：增加内边距
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp) // 风格改造：增大图标容器
                                .clip(RoundedCornerShape(16.dp)) // 风格改造：使用中圆角
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Upload,
                                contentDescription = "导入恢复",
                                modifier = Modifier.size(32.dp), // 风格改造：增大图标
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp)) // 风格改造：增加间距
                        Text(
                            "导入数据恢复",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp)) // 风格改造：增加间距
                        Text(
                            text = "选择本地JSON备份文件，恢复所有数据\n注意：恢复后将覆盖当前数据",
                            style = MaterialTheme.typography.bodyMedium, // 风格改造：增大字体
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp // 风格改造：增大行高
                        )
                    }
                }

                // 风格改造：重要提示，使用卡片样式
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    shape = MaterialTheme.shapes.large, // 风格改造：使用大圆角
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "⚠️ 重要提示：\n1. 备份文件请妥善保存，卸载APP后数据将丢失\n2. 恢复数据会覆盖当前所有内容，请谨慎操作",
                        style = MaterialTheme.typography.bodyMedium, // 风格改造：增大字体
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 风格改造：加载/结果弹窗
        if (showLoadingDialog) {
            Dialog(
                onDismissRequest = {
                    // 支持手动关闭弹窗
                    showLoadingDialog = false
                    dialogDismissJob?.cancel()
                }
            ) {
                Card(
                    shape = MaterialTheme.shapes.large, // 风格改造：使用大圆角
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp), // 风格改造：增加内边距
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(56.dp), // 风格改造：增大进度条
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp)) // 风格改造：增加间距
                        Text(
                            dialogMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ===================== 数据模型 =====================
/**
 * 备份数据封装类
 */
data class BackupData(
    val assets: List<Asset>,
    val wishlist: List<WishlistItem>,
    val categories: List<String>,
    val backupTime: String
)

// ===================== 权限配置（需添加） =====================
// 打开 AndroidManifest.xml 添加存储权限
// <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32"/>
// <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28"/>
// <application ... android:requestLegacyExternalStorage="true">

@Preview(showBackground = true, device = "spec:width=360dp,height=780dp")
@Composable
fun BackupRestoreScreenPreview() {
    val sampleAssets = listOf(
        Asset(id = 1, name = "iPhone 15 Pro", purchasePrice = 7999.0, category = "文娱数码"),
        Asset(id = 2, name = "MacBook Air", purchasePrice = 9499.0, category = "文娱数码")
    )
    val sampleWishlist = listOf(
        WishlistItem(id = 1, name = "新款相机", price = 12000.0, targetAmount = 12000.0, savedAmount = 3000.0)
    )
    val sampleCategories = listOf("文娱数码", "生活用品", "家居家电")

    ZhangwuTheme {
        BackupRestoreScreen(
            onBackClick = {},
            assetsList = sampleAssets,
            wishlistItems = sampleWishlist,
            categoriesList = sampleCategories,
            onRestoreSuccess = { _, _, _ -> }
        )
    }
}