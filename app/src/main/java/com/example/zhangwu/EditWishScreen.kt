package com.example.zhangwu

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhangwu.model.WishItem
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import coil.compose.AsyncImage

/**
 * 编辑心愿页面
 * @param wish 要编辑的心愿
 * @param onBackClick 返回按钮点击回调
 * @param onSaveWish 保存心愿回调
 * @param onDeleteWish 删除心愿回调
 * @param categories 分类列表（用于购买弹窗）
 * @param onPurchaseWish 购买心愿回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWishScreen(
    wish: WishItem,
    onBackClick: () -> Unit,
    onSaveWish: (WishItem) -> Unit,
    onDeleteWish: (WishItem) -> Unit,
    // 新增：购买心愿相关参数
    categories: List<String> = listOf("全部", "未分类"),
    onPurchaseWish: (WishItem, String, Long, Int) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    // 表单状态
    var name by remember { mutableStateOf(wish.name) }
    var price by remember { mutableStateOf(if (wish.price % 1.0 == 0.0) wish.price.toLong().toString() else wish.price.toString()) }
    var targetDate by remember { mutableStateOf(wish.targetDate) }
    var remark by remember { mutableStateOf(wish.remark) }
    var wishImageUri by remember { mutableStateOf<Uri?>(wish.imageUri.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }) }

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        wishImageUri = uri
    }

    // 日期选择器状态
    var showDatePicker by remember { mutableStateOf(false) }
    val targetDatePickerState = rememberDatePickerState()

    // 购买弹窗状态
    var showPurchaseDialog by remember { mutableStateOf(false) }
    
    // 日期格式化
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA) }
    fun onDateSelected(date: Long): String = dateFormat.format(Date(date))

    // 表单保存
    fun saveWish() {
        if (name.isNotEmpty() && price.isNotEmpty()) {
            val updatedWish = wish.copy(
                name = name,
                price = price.toDoubleOrNull() ?: 0.0,
                targetDate = targetDate,
                remark = remark,
                imageUri = wishImageUri?.toString() ?: ""
            )
            onSaveWish(updatedWish)
            Toast.makeText(context, "编辑成功", Toast.LENGTH_SHORT).show()
            onBackClick()
        } else {
            Toast.makeText(context, "请填写完整基础信息", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑心愿") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(it)
                    .padding(16.dp)
            ) {
                // 图片上传区域
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
                Spacer(modifier = Modifier.height(16.dp))

                // 心愿名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("心愿名称") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

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
                    // 修复：添加右侧日历图标，点击可选择日期
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
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

                // 标记已购买按钮（仅未购买时显示）
                if (!wish.isPurchased) {
                    Button(
                        onClick = { showPurchaseDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "购买")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("标记为已购买", fontSize = 16.sp, fontWeight = MaterialTheme.typography.labelLarge.fontWeight)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 删除按钮
                Button(
                    onClick = {
                        // 修复：添加删除物品功能
                        onDeleteWish(wish)
                        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除心愿", fontSize = 16.sp, fontWeight = MaterialTheme.typography.labelLarge.fontWeight)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // 保存按钮
                Button(
                    onClick = { saveWish() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), // 深色模式适配：使用动态主题色
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("保存修改", fontSize = 16.sp, fontWeight = MaterialTheme.typography.labelLarge.fontWeight)
                }
                
                // 日期选择器
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                targetDatePickerState.selectedDateMillis?.let {
                                    targetDate = onDateSelected(it)
                                }
                                showDatePicker = false
                            }) {
                                Text("确定")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("取消")
                            }
                        }
                    ) {
                        DatePicker(state = targetDatePickerState)
                    }
                }

                // 购买心愿弹窗
                if (showPurchaseDialog) {
                    PurchaseWishDialog(
                        wish = wish,
                        categories = categories,
                        onDismiss = { showPurchaseDialog = false },
                        onConfirm = { category, purchaseDate, expectedYears ->
                            onPurchaseWish(wish, category, purchaseDate, expectedYears)
                            showPurchaseDialog = false
                            Toast.makeText(context, "已转入资产", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        }
                    )
                }
            }
        }
    )
}
