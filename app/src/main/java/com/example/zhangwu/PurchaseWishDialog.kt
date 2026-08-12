package com.example.zhangwu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhangwu.model.WishItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 购买心愿弹窗：让用户选择分类、购买日期、预期年限
 * 确认后通过 [onConfirm] 回调将心愿转为资产
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseWishDialog(
    wish: WishItem,
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (category: String, purchaseDate: Long, expectedYears: Int) -> Unit
) {
    // 默认值
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull { it != "全部" } ?: "未分类")
    }
    var purchaseDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var expectedYears by remember { mutableStateOf("2") }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA) }
    val dateText = remember(purchaseDateMillis) { dateFormat.format(Date(purchaseDateMillis)) }

    // 日期选择器状态
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = purchaseDateMillis
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("标记已购买", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "「${wish.name}」将转入资产，¥${"%.2f".format(wish.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 分类选择
                Text("分类", style = MaterialTheme.typography.labelLarge)
                var categoryMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { categoryMenuExpanded = true }) {
                                Text("▼", fontSize = 12.sp)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        categories.filter { it != "全部" }.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // 购买日期
                Text("购买日期", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = dateText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "选择日期",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                // 预期年限
                Text("预期使用年限", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = expectedYears,
                    onValueChange = { expectedYears = it.filter { c -> c.isDigit() }.take(2) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("年") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val years = expectedYears.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    onConfirm(selectedCategory, purchaseDateMillis, years)
                }
            ) {
                Text("确认购买")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // 日期选择器弹窗
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        purchaseDateMillis = it
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
