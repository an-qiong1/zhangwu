package com.example.zhangwu

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.zhangwu.ui.theme.ZhangwuTheme

/**
 * 问题反馈页面
 * 功能：选择反馈类型 + 填写内容 + 一键唤起邮箱发送
 * 固定收件人：anqiong2436@outlook.com
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 固定收件人邮箱
    val FEEDBACK_EMAIL = "anqiong2436@outlook.com"
    // 表单状态
    var feedbackType by remember { mutableStateOf("Bug反馈") }
    var feedbackContent by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    // 发送确认弹窗
    var showConfirmDialog by remember { mutableStateOf(false) }

    // 反馈类型选项
    val typeList = listOf("Bug反馈", "功能建议", "其他问题")

    // 一键唤起邮箱发送
    fun sendFeedbackEmail() {
        if (feedbackContent.isBlank()) {
            Toast.makeText(context, "请填写反馈内容", Toast.LENGTH_SHORT).show()
            return
        }
        // 构建邮件主题 + 正文
        val subject = "【掌物APP】$feedbackType"
        val body = buildString {
            append("反馈类型：$feedbackType\n\n")
            append("反馈内容：\n$feedbackContent\n\n")
            append("联系方式：${contactInfo.ifBlank { "无" }}")
        }

        // 调用系统邮箱 (mailto 协议)
        val uri = Uri.parse("mailto:$FEEDBACK_EMAIL?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        // 跳转邮箱
        runCatching {
            context.startActivity(intent)
            showConfirmDialog = true
        }.onFailure {
            Toast.makeText(context, "未检测到邮箱应用，请安装后重试", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        CommonPageTitle(
            title = "问题反馈",
            showBackButton = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 1. 反馈类型选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "反馈类型",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    typeList.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { feedbackType = type }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = feedbackType == type,
                                onClick = { feedbackType = type },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(type, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // 2. 反馈内容
            OutlinedTextField(
                value = feedbackContent,
                onValueChange = { feedbackContent = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("反馈内容（必填）") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                minLines = 5,
                maxLines = 8,
                placeholder = { Text("请详细描述Bug或建议...") }
            )

            // 3. 联系方式
            OutlinedTextField(
                value = contactInfo,
                onValueChange = { contactInfo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("联系方式（可选）") },
                placeholder = { Text("QQ/微信/邮箱，方便我们回复您") }
            )

            // 4. 一键提交按钮
            Button(
                onClick = { sendFeedbackEmail() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("一键提交反馈", fontWeight = FontWeight.Bold)
            }

            // 邮箱提示
            Text(
                "将自动唤起邮箱，收件人已自动填写，点击发送即可",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // 发送成功提示弹窗
    if (showConfirmDialog) {
        Dialog(onDismissRequest = { showConfirmDialog = false }) {
            Card(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("提示", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("已自动填充邮箱信息，请在邮箱App内点击「发送」完成提交", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(20.dp))
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedbackScreenPreview() {
    ZhangwuTheme {
        FeedbackScreen(onBackClick = {})
    }
}