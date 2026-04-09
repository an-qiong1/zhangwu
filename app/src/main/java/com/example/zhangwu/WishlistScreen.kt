package com.example.zhangwu

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhangwu.model.WishlistItem
import com.example.zhangwu.ui.theme.ZhangwuTheme
import com.example.zhangwu.ui.theme.COLOR_SERVING
import com.example.zhangwu.ui.theme.COLOR_RETIRED
import com.example.zhangwu.ui.theme.COLOR_SOLD
import com.example.zhangwu.ui.theme.COLOR_OVERVIEW_BG
import com.example.zhangwu.ui.theme.COLOR_BORDER
import com.example.zhangwu.ui.theme.COLOR_PAGE_BG

// 扩展属性，添加进度计算
val WishlistItem.progress: Float
    get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat() else 0f

@Composable
fun WishlistScreen(
    wishlistItems: List<WishlistItem> = emptyList(),
    onSearchClick: () -> Unit = {},
    categories: List<String> = listOf("全部", "通勤代步", "文娱数码", "穿戴配饰"),
    selectedCategory: Int = 0,
    onCategoryChange: (Int) -> Unit = {}
) {
    val filteredItems = if (selectedCategory == 0) {
        wishlistItems
    } else {
        wishlistItems.filter { it.category == categories[selectedCategory] }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(COLOR_PAGE_BG)
    ) {
        // ===================== 统一标题栏（和资产页完全一致） =====================
        CommonPageTitle(
            title = "心愿单",
            showSearchIcon = true,
            onSearchClick = onSearchClick
        )

        // 分类筛选栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(COLOR_OVERVIEW_BG)
                .clip(RoundedCornerShape(12.dp))
                .border(width = 1.dp, color = COLOR_BORDER, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.forEachIndexed { index, name ->
                    val isSelected = selectedCategory == index
                    val chipColor = when (name) {
                        "通勤代步" -> COLOR_SERVING
                        "文娱数码" -> COLOR_RETIRED
                        "穿戴配饰" -> COLOR_SOLD
                        else -> COLOR_SERVING
                    }
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(index) },
                        label = { Text(name, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) chipColor else COLOR_BORDER,
                            selectedBorderColor = chipColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.animateContentSize(tween(200))
                    )
                }
            }
        }

        // 内容区域
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = COLOR_SERVING
                    )
                    Text(
                        text = "还没有心愿，去添加一个吧",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredItems) { WishlistCard(it) }
            }
        }
    }
}

@Composable
fun WishlistCard(item: WishlistItem) {
    val statusColor = if (item.savedAmount >= item.targetAmount) COLOR_SOLD else COLOR_SERVING
    val statusText = if (item.savedAmount >= item.targetAmount) "已购入" else "未购入"
    
    val categoryIcon = when (item.category) {
        "通勤代步" -> Icons.Default.DirectionsCar
        "文娱数码" -> Icons.Default.PhoneAndroid
        "穿戴配饰" -> Icons.Default.Watch
        else -> Icons.Default.PhoneAndroid
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp), false),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 状态标签
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(statusColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(statusText, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(categoryIcon, contentDescription = item.category, tint = statusColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(item.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("目标金额: ¥ ${item.targetAmount}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("已存金额: ¥ ${item.savedAmount}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${(item.progress * 100).toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    Text(" 完成", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
fun WishlistPreview() {
    ZhangwuTheme {
        WishlistScreen()
    }
}