package com.example.zhangwu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhangwu.ui.theme.ZhangwuTheme

// 统一页面标题栏（支持返回按钮和搜索图标，标题样式与资产主页面完全一致）
@Composable
fun CommonPageTitle(
    title: String,
    showSearchIcon: Boolean = false,
    showBackButton: Boolean = false,
    onSearchClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：返回按钮或占位符
        if (showBackButton) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp) // 统一图标按钮大小
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // 修复：移除左侧占位符，让标题左对齐到16dp位置，与卡片左侧边框对齐
        }

        // 标题（和资产页掌物字样完全一致）
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 右侧：搜索图标或占位符
        Spacer(modifier = Modifier.weight(1f)) // 占位符，将标题推到左侧
        if (showSearchIcon) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(48.dp) // 统一图标按钮大小
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "搜索",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp)) // 占位符保持布局平衡
        }
    }
}

// 统一添加按钮
@Composable
fun CommonAddFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier.padding(end = 16.dp, bottom = 72.dp)
    ) {
        Icon(Icons.Filled.Add, "添加")
    }
}

@Preview(showBackground = true)
@Composable
fun CommonPageTitlePreview() {
    ZhangwuTheme {
        CommonPageTitle(
            title = "页面标题",
            showBackButton = true,
            showSearchIcon = true
        )
    }
}

@Preview
@Composable
fun CommonAddFABPreview() {
    ZhangwuTheme {
        CommonAddFAB(onClick = {})
    }
}

