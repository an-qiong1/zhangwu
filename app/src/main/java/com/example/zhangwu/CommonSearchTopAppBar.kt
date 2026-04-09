package com.example.zhangwu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zhangwu.ui.theme.ZhangwuTheme

// 通用搜索页面（资产 + 心愿单 共用）
@Composable
fun <T> CommonSearchScreen(
    title: String,
    originalList: List<T>,
    itemContent: @Composable (T) -> Unit,
    filter: (T, String) -> Boolean,
    onBackClick: () -> Unit
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    // 搜索过滤
    val filteredList = remember(originalList, searchText.text) {
        if (searchText.text.isBlank()) emptyList()
        else originalList.filter { filter(it, searchText.text) }
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
            // 顶部搜索栏（全屏展开）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                }

                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = { Text("搜索$title...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchText.text.isNotEmpty()) {
                            IconButton(onClick = { searchText = TextFieldValue("") }) {
                                Icon(Icons.Default.Close, "清空")
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // 搜索结果列表
            if (searchText.text.isNotBlank() && filteredList.isEmpty()) {
                CommonEmptyState(Icons.Default.SearchOff, "未找到相关内容")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(filteredList) { item ->
                        itemContent(item)
                    }
                }
            }
        }
    }
}

// 空状态
@Composable
fun CommonEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
            Text(text, Modifier.padding(top = 16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommonSearchScreenPreview() {
    ZhangwuTheme {
        CommonSearchScreen(
            title = "资产",
            originalList = listOf("电脑", "手机", "平板", "显示器"),
            itemContent = { text ->
                Text(
                    text = text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            },
            filter = { item, query -> item.contains(query, ignoreCase = true) },
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonEmptyStatePreview() {
    ZhangwuTheme {
        CommonEmptyState(
            icon = Icons.Default.SearchOff,
            text = "未找到相关内容"
        )
    }
}
