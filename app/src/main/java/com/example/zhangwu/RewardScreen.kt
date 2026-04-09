package com.example.zhangwu

import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.zhangwu.ui.theme.ZhangwuTheme
import java.io.File
import java.io.OutputStream

/**
 * 打赏页面（纯本地离线版）
 * 功能：展示本地微信/支付宝收款码 + 点击放大 + 保存图片
 * 修复点：布局嵌套、弹窗适配、交互反馈、权限提示、代码健壮性
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 本地图片资源（请确保drawable中存在对应图片）
    val wechatQrRes = R.drawable.wechat_reward_qr // 微信收款码资源ID
    val alipayQrRes = R.drawable.alipay_reward_qr // 支付宝收款码资源ID

    // 放大弹窗状态
    var showBigQr by remember { mutableStateOf(false) }
    var isWechatQr by remember { mutableStateOf(true) } // true=微信，false=支付宝
    // 保存图片加载状态
    var isSaving by remember { mutableStateOf(false) }

    // 保存本地图片到相册（适配Android 10+，增加异常捕获和权限提示）
    fun saveQrToGallery(resId: Int, isWechat: Boolean) {
        if (isSaving) return // 防止重复点击
        isSaving = true

        try {
            // 从资源文件获取Bitmap（增加异常捕获）
            val bitmap = runCatching {
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    Uri.parse("android.resource://${context.packageName}/${resId}")
                )
            }.getOrElse {
                Toast.makeText(context, "收款码资源加载失败", Toast.LENGTH_SHORT).show()
                isSaving = false
                return
            }

            // 文件名
            val fileName = if (isWechat) "微信收款码.png" else "支付宝收款码.png"
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                // Android 10+ 存储到公共目录
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/掌物打赏")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            // 插入到相册（增加空值判断）
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                var outputStream: OutputStream? = null
                try {
                    outputStream = context.contentResolver.openOutputStream(it)
                    outputStream?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                    // Android 10+ 完成文件写入
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(it, contentValues, null, null)
                    Toast.makeText(context, "收款码已保存到相册", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "保存失败：${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    outputStream?.close()
                }
            } ?: run {
                Toast.makeText(context, "保存失败：无法获取相册写入路径", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "保存失败：${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isSaving = false
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
                .systemBarsPadding()
        ) {
            // 顶部标题栏（和其他页面统一）
            CommonPageTitle(
                title = "打赏一下",
                showBackButton = true,
                onBackClick = onBackClick
            )

            // 主内容区域（优化滚动和间距）
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // 标题说明（优化样式和间距）
                Text(
                    text = "感谢您的支持～扫码打赏鼓励一下",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 微信收款码卡片（统一内边距和圆角）
                RewardCard(
                    title = "微信支付",
                    resId = wechatQrRes,
                    bgColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onQrClick = {
                        isWechatQr = true
                        showBigQr = true
                    },
                    onDownloadClick = { saveQrToGallery(wechatQrRes, true) }
                )

                // 支付宝收款码卡片（增加间距，统一样式）
                RewardCard(
                    title = "支付宝支付",
                    resId = alipayQrRes,
                    bgColor = androidx.compose.ui.graphics.Color(0xFF1677FF), // 支付宝蓝
                    textColor = androidx.compose.ui.graphics.Color.White,
                    onQrClick = {
                        isWechatQr = false
                        showBigQr = true
                    },
                    onDownloadClick = { saveQrToGallery(alipayQrRes, false) },
                    modifier = Modifier.padding(top = 16.dp)
                )

                // 权限提示（Android 13+）
                Text(
                    text = "⚠️ Android 13+需授予相册写入权限才能保存图片",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 收款码放大弹窗（优化尺寸和交互）
        if (showBigQr) {
            Dialog(
                onDismissRequest = {
                    showBigQr = false
                    isSaving = false // 关闭弹窗时重置保存状态
                }
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.95f) // 优化弹窗宽度
                        .fillMaxHeight(0.85f), // 优化弹窗高度
                    elevation = CardDefaults.cardElevation(8.dp) // 统一阴影
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 弹窗标题（优化内边距）
                        Text(
                            text = if (isWechatQr) "微信收款码" else "支付宝收款码",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(20.dp)
                        )

                        // 放大的收款码（优化缩放和内边距）
                        Image(
                            painter = painterResource(id = if (isWechatQr) wechatQrRes else alipayQrRes),
                            contentDescription = "收款码",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            contentScale = ContentScale.Fit
                        )

                        // 保存按钮（优化点击区域和视觉）
                        IconButton(
                            onClick = {
                                if (isWechatQr) {
                                    saveQrToGallery(wechatQrRes, true)
                                } else {
                                    saveQrToGallery(alipayQrRes, false)
                                }
                                showBigQr = false
                            },
                            enabled = !isSaving, // 保存中禁用按钮
                            modifier = Modifier
                                .padding(20.dp)
                                .size(52.dp) // 增大点击区域
                                .clip(CircleShape)
                                .background(
                                    if (isSaving) MaterialTheme.colorScheme.surfaceVariant
                                    else MaterialTheme.colorScheme.primaryContainer
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "保存到相册",
                                tint = if (isSaving) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp) // 增大图标
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 收款码卡片组件（优化样式和交互）
 */
@Composable
fun RewardCard(
    title: String,
    resId: Int,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    onQrClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)), // 和其他页面统一圆角
        elevation = CardDefaults.cardElevation(4.dp) // 统一阴影
    ) {
        Column(
            modifier = Modifier
                .background(bgColor)
                .padding(bottom = 16.dp), // 优化底部内边距
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 支付方式标题（优化内边距）
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(16.dp)
            )

            // 收款码图片（优化点击区域和内边距）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // 保持1:1比例
                    .background(androidx.compose.ui.graphics.Color.White)
                    .clickable { onQrClick() }
                    .padding(20.dp) // 增大内边距，优化显示
            ) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "$title 收款码",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // 操作按钮（优化间距和点击区域）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = onQrClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = textColor),
                    modifier = Modifier.height(48.dp) // 增大按钮高度
                ) {
                    Text("点击放大", fontWeight = FontWeight.Medium)
                }
                TextButton(
                    onClick = onDownloadClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = textColor),
                    modifier = Modifier.height(48.dp) // 增大按钮高度
                ) {
                    Text("保存图片", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ===================== 预览函数（优化预览体验） =====================
@Preview(showBackground = true, device = "spec:width=360dp,height=780dp")
@Composable
fun RewardScreenPreview() {
    ZhangwuTheme {
        RewardScreen(onBackClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun RewardCardPreview() {
    ZhangwuTheme {
        RewardCard(
            title = "微信支付",
            resId = R.drawable.wechat_reward_qr, // 修复：使用现有的微信二维码图片作为预览
            bgColor = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onQrClick = {},
            onDownloadClick = {}
        )
    }
}