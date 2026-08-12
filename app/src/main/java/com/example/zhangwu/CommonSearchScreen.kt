package com.example.zhangwu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.zhangwu.model.Asset
import com.example.zhangwu.model.WishItem
import com.example.zhangwu.ui.theme.ZhangwuTheme
import androidx.compose.foundation.isSystemInDarkTheme

/**
 * 改造：独立全屏搜索页面（全新UI风格）
 * 支持资产/心愿单 通用搜索
 * 改造点：
 * 1. 大圆角搜索框，取消按钮嵌入内部
 * 2. 搜索图标和提示文字的智能显示/隐藏
 * 3. 适配深色模式，风格统一
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CommonSearchScreen(
    title: String,
    searchHint: String = "名称、分类、标签、备注、状态等",
    originalList: List<T>,
    itemContent: @Composable (T) -> Unit,
    filterRule: (T, String) -> Boolean,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // 搜索文本状态
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    // 搜索结果过滤
    val filteredList = remember(originalList, searchText.text) {
        if (searchText.text.isBlank()) emptyList()
        else originalList.filter { filterRule(it, searchText.text) }
    }

    // 页面加载完成自动聚焦搜索框，弹出键盘
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 改造：全新设计的顶部搜索栏
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // 改造：大圆角搜索框容器，取消按钮嵌入内部
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp) // 修复：增加高度，确保「取消」文字完全显示
                        .clip(RoundedCornerShape(16.dp)) // 修复：使用16dp圆角，符合MD3搜索栏规范
                        .background(MaterialTheme.colorScheme.surfaceVariant) // 修复：统一使用surfaceVariant背景，与页面背景形成清晰区分
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 改造：搜索图标 - 无输入时显示，有输入时隐藏
                        if (searchText.text.isEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // 改造：输入框/提示文字区域
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // 改造：无输入时显示提示文字
                            if (searchText.text.isEmpty()) {
                                Text(
                                    text = searchHint,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp,
                                    maxLines = 1
                                )
                            }

                            // 改造：真正的输入框（透明背景，无边框）
                            // 核心交互逻辑：使用BasicTextField实现完全自定义的输入框
                            androidx.compose.foundation.text.BasicTextField(
                                value = searchText,
                                onValueChange = { newValue -> searchText = newValue },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface, // 修复：统一使用onSurface，确保深浅色模式下都清晰可见
                                    fontSize = 16.sp
                                ),
                                decorationBox = { innerTextField ->
                                    // 改造：不显示任何装饰，完全自定义
                                    innerTextField()
                                }
                            )
                        }

                        // 改造：取消/清空按钮 - 嵌入搜索框内部最右侧
                        if (searchText.text.isNotEmpty()) {
                            // 改造：有输入时显示清空按钮
                            IconButton(
                                onClick = { searchText = TextFieldValue("") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清空",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            // 改造：无输入时显示取消按钮（可点击返回）
                            TextButton(
                                onClick = {
                                    keyboardController?.hide()
                                    onBackClick()
                                },
                                modifier = Modifier
                                    .height(40.dp) // 修复：增加按钮高度，确保文字完全显示
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "取消",
                                    color = MaterialTheme.colorScheme.primary, // 修复：使用动态主题色，确保深色模式下显示正确
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // 改造：搜索结果区域（保持原有逻辑）
            when {
                // 改造：无搜索结果状态
                searchText.text.isNotBlank() && filteredList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "未找到「${searchText.text}」相关内容",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }

                // 改造：有搜索结果（资产用网格布局）
                filteredList.isNotEmpty() && filteredList.first() is Asset -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredList) { item ->
                            itemContent(item)
                        }
                    }
                }

                // 改造：心愿单用列表布局
                filteredList.isNotEmpty() -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredList) { item ->
                            itemContent(item)
                        }
                    }
                }

                // 改造：初始状态（无输入）显示提示
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "输入关键词开始搜索",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "支持搜索名称、分类、标签、备注、状态等",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssetSearchPreview() {
    val sampleAssets = listOf(
        Asset(id = 1, name = "iPhone 15 Pro", purchasePrice = 7999.0, category = "文娱数码"),
        Asset(id = 2, name = "MacBook Air", purchasePrice = 9499.0, category = "文娱数码"),
        Asset(id = 3, name = "iPad Pro", purchasePrice = 6799.0, category = "文娱数码")
    )
    ZhangwuTheme {
        CommonSearchScreen(
            title = "搜索资产",
            originalList = sampleAssets,
            itemContent = { 
                ItemCard(
                    asset = it, 
                    onClick = {}
                ) 
            },
            filterRule = { asset, query -> asset.name.contains(query, ignoreCase = true) },
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WishlistSearchPreview() {
    val sampleWishlist = listOf(
        WishItem(id = 1L, name = "新款相机", price = 12000.0, targetDate = ""),
        WishItem(id = 2L, name = "旅游基金", price = 5000.0, targetDate = "")
    )
    ZhangwuTheme {
        CommonSearchScreen(
            title = "搜索心愿单",
            originalList = sampleWishlist,
            itemContent = { WishCard(wish = it, onClick = {}) },
            filterRule = { item, query -> item.name.contains(query, ignoreCase = true) },
            onBackClick = {}
        )
    }
}
