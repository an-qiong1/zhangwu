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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.zhangwu.model.WishlistItem
import com.example.zhangwu.ui.theme.ZhangwuTheme
/**
 * 添加心愿单页面（修复安全区域适配版）
 * 功能：独立界面，适配状态栏/底部导航栏，无遮挡
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishlistScreen(
    onBackClick: () -> Unit,
    onSaveWishlist: (WishlistItem) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    // ===================== 表单状态 =====================
    var wishImageUri by remember { mutableStateOf<Uri?>(null) }
    var wishName by remember { mutableStateOf("") }
    var wishPrice by remember { mutableStateOf("") }
    var wishRemark by remember { mutableStateOf("") }
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        wishImageUri = uri
    }
    // ===================== 表单保存逻辑 =====================
    fun saveWishlist() {
        if (wishName.isBlank() || wishPrice.isBlank()) {
            Toast.makeText(context, "请填写心愿名称和目标价格", Toast.LENGTH_SHORT).show()
            return
        }
        val newWish = WishlistItem(
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            name = wishName,
            price = wishPrice.toDoubleOrNull() ?: 0.0,
            savedAmount = 0.0,
            targetAmount = wishPrice.toDoubleOrNull() ?: 0.0,
            category = "心愿",
            icon = "🎁",
            remark = wishRemark,
            imageUri = wishImageUri?.toString() ?: ""
        )
        onSaveWishlist(newWish)
        Toast.makeText(context, "心愿添加成功", Toast.LENGTH_SHORT).show()
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
            title = "添加心愿",
            showBackButton = true,
            onBackClick = onBackClick
        )
        // 表单内容区域（可滚动，不挤压按钮）
        Column(
            modifier = Modifier
                .weight(1f) // 核心修复2：权重占满剩余空间，按钮固定底部
                .fillMaxWidth()
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
                value = wishName,
                onValueChange = { wishName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("心愿名称") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null) },
                singleLine = true
            )
            // 3. 目标价格
            OutlinedTextField(
                value = wishPrice,
                onValueChange = { wishPrice = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("目标价格") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            // 4. 备注信息
            OutlinedTextField(
                value = wishRemark,
                onValueChange = { wishRemark = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注信息") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                minLines = 3,
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        // 底部保存按钮（固定底部，不被遮挡）
        Button(
            onClick = { saveWishlist() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(), // 核心修复3：适配底部手势栏安全区域
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("保存心愿", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddWishlistScreenPreview() {
    ZhangwuTheme {
        AddWishlistScreen(
            onBackClick = {},
            onSaveWishlist = {}
        )
    }
}