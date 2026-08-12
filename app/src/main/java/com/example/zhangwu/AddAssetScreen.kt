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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.zhangwu.model.Asset
import com.example.zhangwu.viewmodel.CategoryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 添加资产页面（修复安全区域适配版）
 * 功能：独立界面，适配状态栏/底部导航栏，无遮挡
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAssetScreen(
    onBackClick: () -> Unit,
    onSaveAsset: (Asset) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    // 修复：添加CategoryViewModel实例，用于获取分类列表
    val categoryViewModel: CategoryViewModel = viewModel()
    val categoryList by categoryViewModel.categoryList.collectAsState()
    // ===================== 表单状态 =====================
    var assetImageUri by remember { mutableStateOf<Uri?>(null) }
    var assetName by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var expectedYears by remember { mutableStateOf("") } // 留空=不设置
    var purchaseDate by remember { mutableStateOf("") }
    var assetStatus by remember { mutableStateOf("服役中") }
    var sellDate by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    // 修复：添加品类选择相关状态
    var selectedCategory by remember { mutableStateOf("全部") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    // 新增：标签支持
    var tags by remember { mutableStateOf(emptyList<String>()) }
    var newTag by remember { mutableStateOf("") }
    var showTagDialog by remember { mutableStateOf(false) }
    // 日期选择器
    var showPurchaseDatePicker by remember { mutableStateOf(false) }
    var showSellDatePicker by remember { mutableStateOf(false) }
    val purchaseDatePickerState = rememberDatePickerState()
    val sellDatePickerState = rememberDatePickerState()
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        assetImageUri = uri
    }
    // 日期格式化
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA) }
    fun onDateSelected(date: Long): String = dateFormat.format(Date(date))
    // ===================== 表单保存 =====================
    fun saveAsset() {
        if (assetName.isBlank() || purchasePrice.isBlank() || purchaseDate.isBlank()) {
            Toast.makeText(context, "请填写完整基础信息", Toast.LENGTH_SHORT).show()
            return
        }
        val newAsset = Asset(
            name = assetName,
            purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
            purchaseDate = dateFormat.parse(purchaseDate)?.time ?: System.currentTimeMillis(),
            expectedYears = expectedYears.toIntOrNull() ?: 0,
            status = assetStatus,
            sellDate = if (assetStatus != "服役中") dateFormat.parse(sellDate)?.time else null,
            sellPrice = if (assetStatus != "服役中") sellPrice.toDoubleOrNull() else null,
            remark = remark,
            imageUri = assetImageUri?.toString(),
            category = selectedCategory,
            tags = tags
        )
        onSaveAsset(newAsset)
        Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show()
        onBackClick()
    }
    // ===================== 页面布局（核心修复：安全区域适配） =====================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // 核心修复1：自动适配状态栏+底部导航栏安全区域
    ) {
        // 统一标题栏（不会被状态栏遮挡）
        CommonPageTitle(
            title = "添加资产",
            showBackButton = true,
            onBackClick = onBackClick
        )
        // 表单内容区域（可滚动，不挤压按钮）
        Column(
            modifier = Modifier
                .weight(1f) // 核心修复2：权重占满剩余空间，按钮固定底部
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
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
                    Text("资产图片", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (assetImageUri != null) {
                            AsyncImage(
                                model = assetImageUri,
                                contentDescription = "资产图片",
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
            // 2. 资产名称
            OutlinedTextField(
                value = assetName,
                onValueChange = { assetName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("资产名称") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            // 3. 购入价格
            OutlinedTextField(
                value = purchasePrice,
                onValueChange = { purchasePrice = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("购入价格") },
                leadingIcon = { Text("¥", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            // 预期使用年限（可选）
            OutlinedTextField(
                value = expectedYears,
                onValueChange = { expectedYears = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("预期使用年限（选填）") },
                supportingText = { Text("留空则不计算日均成本和进度", fontSize = 11.sp) },
                trailingIcon = { Text("年", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // 4. 品类选择
            // 修复：添加品类选择功能，使用弹窗下拉选择的交互形式
            // 4. 品类选择
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("品类选择", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showCategoryDialog = true }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "选择品类",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // 新增：标签管理
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("标签", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { showTagDialog = true }, modifier = Modifier.padding(0.dp)) {
                        Text("管理标签")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = if (tags.isNotEmpty()) tags.joinToString(", ") else "暂无标签",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTagDialog = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "管理标签",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    singleLine = false,
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // 5. 购买时间
            OutlinedTextField(
                value = purchaseDate,
                onValueChange = { purchaseDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("购买时间 (yyyy-MM-dd)") },
                // 修复：移除左侧无效日历图标，只保留右侧可点击图标
                trailingIcon = {
                    IconButton(onClick = { showPurchaseDatePicker = true }) {
                        Icon(
                            Icons.Default.CalendarMonth, 
                            "选择日期",
                            tint = MaterialTheme.colorScheme.primary // 修复：使用Material You动态主色
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            // 5. 资产状态选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("资产状态", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatusRadioButton("服役中", assetStatus) { assetStatus = it }
                        StatusRadioButton("已退役", assetStatus) { assetStatus = it }
                        StatusRadioButton("已售出", assetStatus) { assetStatus = it }
                    }
                }
            }
            // 6. 退役/售出补充信息
            if (assetStatus != "服役中") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("售出/退役信息", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = sellDate,
                            onValueChange = { sellDate = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("售出/退役时间") },
                            // 修复：移除左侧无效日历图标，只保留右侧可点击图标
                            trailingIcon = {
                                IconButton(onClick = { showSellDatePicker = true }) {
                                    Icon(
                                        Icons.Default.CalendarMonth, 
                                        "选择日期",
                                        tint = MaterialTheme.colorScheme.primary // 修复：使用Material You动态主色
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = sellPrice,
                            onValueChange = { sellPrice = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("售出价格") },
                            leadingIcon = { Text("¥", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // 修复：将美元符号替换为人民币符号
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            // 7. 备注
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注信息") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        // 8. 保存按钮（固定底部，不被遮挡）
        Button(
            onClick = { saveAsset() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(), // 核心修复3：适配底部手势栏安全区域
            shape = MaterialTheme.shapes.medium
        ) {
            Text("保存资产", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
    // 日期选择器弹窗
    if (showPurchaseDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showPurchaseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    purchaseDatePickerState.selectedDateMillis?.let {
                        purchaseDate = onDateSelected(it)
                    }
                    showPurchaseDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = purchaseDatePickerState)
        }
    }
    if (showSellDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showSellDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    sellDatePickerState.selectedDateMillis?.let {
                        sellDate = onDateSelected(it)
                    }
                    showSellDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = sellDatePickerState)
        }
    }
    
    // 修复：添加品类选择弹窗
    if (showCategoryDialog) {
        ModalBottomSheet(
            onDismissRequest = { showCategoryDialog = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 弹窗标题
                Text(
                    "选择品类",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 分类列表
                categoryList.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { 
                                selectedCategory = category
                                showCategoryDialog = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = { 
                                selectedCategory = category
                                showCategoryDialog = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            category,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // 取消按钮
                Button(
                    onClick = { showCategoryDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("取消", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 标签管理弹窗
    if (showTagDialog) {
        ModalBottomSheet(
            onDismissRequest = { showTagDialog = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 弹窗标题
                Text(
                    "管理标签",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 添加标签输入框
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("添加新标签") },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (newTag.isNotBlank() && !tags.contains(newTag)) {
                                tags = tags + newTag
                                newTag = ""
                            }
                        }) {
                            Icon(
                                Icons.Default.Add,
                                "添加标签",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    singleLine = true
                )

                // 已添加标签列表
                if (tags.isNotEmpty()) {
                    Text(
                        "已添加标签",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text(tag) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "删除标签",
                                        modifier = Modifier
                                            .size(InputChipDefaults.IconSize)
                                            .clickable {
                                                tags = tags.filter { it != tag }
                                            }
                                    )
                                }
                            )
                        }
                    }
                }

                // 确定按钮
                Button(
                    onClick = { showTagDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("确定", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ===================== 状态单选按钮组件 =====================
@Composable
fun StatusRadioButton(
    status: String,
    selectedStatus: String,
    onStatusChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onStatusChange(status) }
    ) {
        RadioButton(
            selected = selectedStatus == status,
            onClick = { onStatusChange(status) },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(status)
    }
}
