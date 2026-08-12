package com.example.zhangwu

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhangwu.model.Asset
import com.example.zhangwu.model.WishItem
import com.example.zhangwu.viewmodel.WebDavViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 备份恢复页面
 * 功能：本地 JSON 备份/恢复 + WebDAV 坚果云同步
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onBackClick: () -> Unit,
    assetsList: List<Asset>,
    wishlistItems: List<WishItem>,
    categoriesList: List<String>,
    onRestoreSuccess: (List<Asset>, List<WishItem>, List<String>) -> Unit
) {
    val context = LocalContext.current
    val gson = Gson()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val webDavViewModel: WebDavViewModel = viewModel()

    val webDavConfig by webDavViewModel.config.collectAsState()
    val lastSyncTime by webDavViewModel.lastSyncTime.collectAsState()
    val syncState by webDavViewModel.syncState.collectAsState()

    // 弹窗状态
    var showLoadingDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var dialogDismissJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // WebDAV 配置弹窗
    var showWebDavConfigDialog by remember { mutableStateOf(false) }
    // 恢复确认弹窗
    var pendingRestoreData by remember { mutableStateOf<BackupData?>(null) }

    // 同步状态变化时更新弹窗
    LaunchedEffect(syncState) {
        when (val s = syncState) {
            is WebDavViewModel.SyncState.Syncing -> {
                showLoadingDialog = true
                dialogMessage = s.message
            }
            is WebDavViewModel.SyncState.Success -> {
                showLoadingDialog = true
                dialogMessage = s.message
                dialogDismissJob?.cancel()
                dialogDismissJob = scope.launch { delay(2000); showLoadingDialog = false }
                webDavViewModel.resetState()
            }
            is WebDavViewModel.SyncState.Error -> {
                showLoadingDialog = true
                dialogMessage = "失败：${s.message}"
                dialogDismissJob?.cancel()
                dialogDismissJob = scope.launch { delay(3000); showLoadingDialog = false }
                webDavViewModel.resetState()
            }
            else -> {}
        }
    }

    // 本地导出 launcher
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        showLoadingDialog = true
        dialogMessage = "正在导出数据..."
        dialogDismissJob?.cancel()
        val backupData = BackupData(
            assets = assetsList,
            wishlist = wishlistItems,
            categories = categoriesList,
            backupTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
        )
        try {
            val json = gson.toJson(backupData)
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            dialogMessage = "备份成功！\n文件已保存到Download文件夹"
        } catch (e: Exception) {
            dialogMessage = "备份失败：${e.message}"
        } finally {
            dialogDismissJob = scope.launch { delay(3000); showLoadingDialog = false }
        }
    }

    // 本地导入 launcher
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        showLoadingDialog = true
        dialogMessage = "正在恢复数据..."
        dialogDismissJob?.cancel()
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
            if (json.isNullOrBlank()) {
                dialogMessage = "备份文件为空"
                return@rememberLauncherForActivityResult
            }
            val type = object : TypeToken<BackupData>() {}.type
            val backupData = gson.fromJson<BackupData>(json, type)
            onRestoreSuccess(backupData.assets, backupData.wishlist, backupData.categories)
            dialogMessage = "恢复成功！\n备份时间：${backupData.backupTime}"
        } catch (e: Exception) {
            dialogMessage = "恢复失败：${e.message}"
        } finally {
            dialogDismissJob = scope.launch { delay(3000); showLoadingDialog = false }
        }
    }

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
            CommonPageTitle(
                title = "备份与恢复",
                showBackButton = true,
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ========== 本地备份 ==========
                Text(
                    "本地备份",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(4.dp),
                    onClick = {
                        val fileName = "掌物_备份_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.CHINA).format(Date())}.json"
                        saveFileLauncher.launch(fileName)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Download, "导出", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("导出数据备份", fontWeight = FontWeight.Bold)
                            Text("保存到本地 JSON 文件", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(4.dp),
                    onClick = { importFileLauncher.launch(arrayOf("application/json")) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Upload, "导入", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("导入数据恢复", fontWeight = FontWeight.Bold)
                            Text("从本地 JSON 文件恢复（覆盖当前）", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ========== WebDAV 坚果云同步 ==========
                Text(
                    "坚果云 WebDAV 同步",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 配置状态卡片
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Cloud,
                                contentDescription = null,
                                tint = if (webDavConfig.isConfigured) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (webDavConfig.isConfigured) "已配置" else "未配置",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (webDavConfig.isConfigured) "账号：${webDavConfig.username}"
                                       else "请点击右侧按钮配置坚果云账号",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { showWebDavConfigDialog = true }) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("配置")
                            }
                        }
                        if (lastSyncTime.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "上次同步：$lastSyncTime",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 上传到坚果云
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(4.dp),
                    onClick = {
                        if (!webDavConfig.isConfigured) {
                            Toast.makeText(context, "请先配置坚果云账号", Toast.LENGTH_SHORT).show()
                            showWebDavConfigDialog = true
                        } else {
                            webDavViewModel.upload(assetsList, wishlistItems, categoriesList)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudUpload, "上传", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("上传到坚果云", fontWeight = FontWeight.Bold)
                            Text("把当前数据同步到云端（覆盖）", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // 从坚果云恢复
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(4.dp),
                    onClick = {
                        if (!webDavConfig.isConfigured) {
                            Toast.makeText(context, "请先配置坚果云账号", Toast.LENGTH_SHORT).show()
                            showWebDavConfigDialog = true
                        } else {
                            webDavViewModel.download { data -> pendingRestoreData = data }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudDownload, "恢复", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("从坚果云恢复", fontWeight = FontWeight.Bold)
                            Text("下载云端备份并覆盖本地（需确认）", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // 重要提示
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "⚠️ 重要提示：\n1. 备份文件请妥善保存\n2. 恢复数据会覆盖当前所有内容\n3. 坚果云需使用「应用专用密码」，非登录密码",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 加载/结果弹窗
        if (showLoadingDialog) {
            Dialog(onDismissRequest = {
                showLoadingDialog = false
                dialogDismissJob?.cancel()
            }) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(0.85f).padding(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (syncState is WebDavViewModel.SyncState.Syncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        Text(dialogMessage, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // 恢复确认弹窗
        pendingRestoreData?.let { data ->
            AlertDialog(
                onDismissRequest = { pendingRestoreData = null },
                title = { Text("确认恢复") },
                text = {
                    Text(
                        "将从坚果云恢复数据，覆盖当前所有内容。\n" +
                        "云端备份时间：${data.backupTime}\n" +
                        "资产 ${data.assets.size} 项 / 心愿 ${data.wishlist.size} 项 / 分类 ${data.categories.size} 项"
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        onRestoreSuccess(data.assets, data.wishlist, data.categories)
                        pendingRestoreData = null
                        Toast.makeText(context, "恢复成功", Toast.LENGTH_SHORT).show()
                    }) { Text("确认恢复") }
                },
                dismissButton = {
                    TextButton(onClick = { pendingRestoreData = null }) { Text("取消") }
                }
            )
        }

        // WebDAV 配置弹窗
        if (showWebDavConfigDialog) {
            WebDavConfigDialog(
                initialConfig = webDavConfig,
                onDismiss = { showWebDavConfigDialog = false },
                onSave = { newConfig ->
                    webDavViewModel.updateConfig(newConfig)
                    showWebDavConfigDialog = false
                    Toast.makeText(context, "配置已保存", Toast.LENGTH_SHORT).show()
                },
                onTest = { webDavViewModel.testConnection() }
            )
        }
    }
}

/**
 * WebDAV 配置弹窗
 */
@Composable
private fun WebDavConfigDialog(
    initialConfig: com.example.zhangwu.webdav.WebDavConfig,
    onDismiss: () -> Unit,
    onSave: (com.example.zhangwu.webdav.WebDavConfig) -> Unit,
    onTest: () -> Unit
) {
    var serverUrl by remember { mutableStateOf(initialConfig.serverUrl) }
    var username by remember { mutableStateOf(initialConfig.username) }
    var password by remember { mutableStateOf(initialConfig.password) }
    var remotePath by remember { mutableStateOf(initialConfig.remotePath) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("配置坚果云 WebDAV") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = serverUrl, onValueChange = { serverUrl = it },
                    label = { Text("服务器地址") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("账号（邮箱）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("应用专用密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = remotePath, onValueChange = { remotePath = it },
                    label = { Text("远程备份目录") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(
                    "提示：应用专用密码需在坚果云网页端「安全选项」中创建",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    com.example.zhangwu.webdav.WebDavConfig(
                        serverUrl = serverUrl,
                        username = username,
                        password = password,
                        remotePath = remotePath
                    )
                )
            }) { Text("保存") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onTest) { Text("测试连接") }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

// ===================== 数据模型 =====================
data class BackupData(
    val version: Int = 2,
    val assets: List<Asset>,
    val wishlist: List<WishItem>,
    val categories: List<String>,
    val backupTime: String
)
