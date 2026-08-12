package com.example.zhangwu

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.zhangwu.model.WishItem
import com.example.zhangwu.viewmodel.WishViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)

/**
 * 添加心愿页面
 * @param onBackClick 返回按钮点击回调
 * @param onSaveWishlist 保存心愿回调
 */
@Composable
fun AddWishScreen(
    onBackClick: () -> Unit,
    onSaveWishlist: (WishItem) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    // 表单状态
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var wishImageUri by remember { mutableStateOf<Uri?>(null) }

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        wishImageUri = uri
    }

    // 修复：添加日期选择器状态
    var showTargetDatePicker by remember { mutableStateOf(false) }
    val targetDatePickerState = rememberDatePickerState()

    // 日期格式化
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA) }
    fun onDateSelected(date: Long): String = dateFormat.format(Date(date))
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加心愿") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) {innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 图片上传区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("心愿图片", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (wishImageUri != null) {
                            AsyncImage(
                                model = wishImageUri,
                                contentDescription = "心愿图片",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "上传图片",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "点击上传图片",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 2. 心愿名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("心愿名称") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            // 目标价格
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("目标价格") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // 目标日期
            OutlinedTextField(
                value = targetDate,
                onValueChange = { targetDate = it },
                label = { Text("目标日期 (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                // 修复：添加右侧可点击日历图标，与添加资产页保持一致
                trailingIcon = {
                    IconButton(onClick = { showTargetDatePicker = true }) {
                        Icon(
                            Icons.Default.CalendarMonth, 
                            "选择日期",
                            tint = MaterialTheme.colorScheme.primary // 修复：使用Material You动态主色
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 备注
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            // 保存按钮
            Button(
                onClick = {
                    // 修复：添加心愿页的数据回传逻辑
                    // 验证输入
                    if (name.isNotEmpty() && price.isNotEmpty()) {
                        val wishItem = WishItem(
                            name = name,
                            price = price.toDoubleOrNull() ?: 0.0,
                            targetDate = targetDate,
                            remark = remark,
                            imageUri = wishImageUri?.toString() ?: ""
                        )
                        // 回传数据
                        onSaveWishlist(wishItem)
                        // 返回上一页
                        onBackClick()
                    } else {
                        Toast.makeText(context, "请填写完整基础信息", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), // 深色模式适配：使用动态主题色
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存")
            }
        }
    }
    
    // 修复：添加日期选择器弹窗
    if (showTargetDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showTargetDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    targetDatePickerState.selectedDateMillis?.let {
                        targetDate = onDateSelected(it)
                    }
                    showTargetDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = targetDatePickerState)
        }
    }
}
