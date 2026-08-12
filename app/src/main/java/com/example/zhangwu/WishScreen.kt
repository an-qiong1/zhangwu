package com.example.zhangwu

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.zhangwu.model.WishItem
import com.example.zhangwu.viewmodel.WishViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.ExperimentalFoundationApi

/**
 * 心愿单页面
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WishScreen(
    onSearchClick: () -> Unit = {},
    onWishClick: (WishItem) -> Unit = {},
    showBatchActions: Boolean = false,
    selectedWishes: List<WishItem> = emptyList(),
    onBatchActionsChange: (Boolean) -> Unit = {},
    onSelectedWishesChange: (List<WishItem>) -> Unit = {},
    // 新增：分类列表（用于购买弹窗）
    categories: List<String> = listOf("全部", "未分类"),
    // 新增：购买心愿回调
    onPurchaseWish: (WishItem, String, Long, Int) -> Unit = { _, _, _, _ -> }
) {
    val wishViewModel: WishViewModel = viewModel()
    val wishes by wishViewModel.wishItems.collectAsState()

    // 购买弹窗状态
    var purchasingWish by remember { mutableStateOf<WishItem?>(null) }

    // 心愿总览统计 - 性能优化：用 derivedStateOf 避免每次重组都 sumOf
    val totalCount by remember { derivedStateOf { wishes.size } }
    val totalPrice by remember { derivedStateOf { wishes.sumOf { it.price } } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CommonPageTitle(
                title = "心愿单",
                showSearchIcon = true,
                onSearchClick = onSearchClick
            )

            if (wishes.isEmpty()) {
                // 空状态
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
                            tint = MaterialTheme.colorScheme.primary
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
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.fillMaxSize(),
                    // 修复：底部内容间距加到 240.dp，保证最后一行卡片不被底部导航栏 + FAB 覆盖
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 心愿总览大卡片（占满全宽）
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        WishOverviewCard(
                            totalCount = totalCount,
                            totalPrice = totalPrice
                        )
                    }

                    // 心愿列表
                    items(wishes, key = { it.id }) { wish ->
                        WishCard(
                            wish = wish,
                            onClick = {
                                if (showBatchActions) {
                                    val newSelectedWishes = if (selectedWishes.contains(wish)) {
                                        selectedWishes.filter { it.id != wish.id }
                                    } else {
                                        selectedWishes + wish
                                    }
                                    onSelectedWishesChange(newSelectedWishes)
                                    if (newSelectedWishes.isEmpty()) {
                                        onBatchActionsChange(false)
                                    }
                                } else {
                                    onWishClick(wish)
                                }
                            },
                            onLongClick = {
                                onBatchActionsChange(true)
                                onSelectedWishesChange(listOf(wish))
                            },
                            isSelected = selectedWishes.contains(wish),
                            onPurchase = {
                                purchasingWish = wish
                            }
                        )
                    }
                }
            }
        }
    }

    // 购买心愿弹窗
    purchasingWish?.let { wish ->
        PurchaseWishDialog(
            wish = wish,
            categories = categories,
            onDismiss = { purchasingWish = null },
            onConfirm = { category, purchaseDate, expectedYears ->
                onPurchaseWish(wish, category, purchaseDate, expectedYears)
                purchasingWish = null
            }
        )
    }
}

/**
 * 心愿总览大卡片：显示总数量和总价格
 */
@Composable
fun WishOverviewCard(
    totalCount: Int,
    totalPrice: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "心愿总览 ($totalCount)",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("心愿总数", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(
                        "$totalCount 件",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("心愿总价", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(
                        "¥ ${"%.2f".format(totalPrice)}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 方案A：整宽横向大卡片，左图右文，高度 wrap content
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WishCard(
    wish: WishItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    // 新增：标记已购买回调
    onPurchase: () -> Unit = {},
    // 是否显示购买按钮（搜索界面传 false）
    showPurchaseButton: Boolean = true
) {
    val statusColor = if (wish.isPurchased) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val statusText = if (wish.isPurchased) "已购入" else "进行中"
    // 价格格式化简化：整数不保留小数，非整数最多1位
    val priceLong = wish.price.toLong()
    val priceText = if (wish.price == priceLong.toDouble()) {
        priceLong.toString()
    } else {
        "%.1f".format(wish.price)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, MaterialTheme.shapes.large, false)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 选择状态指示
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            // 横向布局：左图 + 右文
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ========== 左侧：图片/图标区域（固定尺寸，圆角卡片） ==========
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (wish.imageUri.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(wish.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = wish.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "心愿",
                            tint = statusColor,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // ========== 右侧：文字信息区 ==========
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 第一行：名称（允许2行）
                    Text(
                        wish.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 第二行：目标价格
                    Text(
                        "目标价 ¥$priceText",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                    if (wish.targetDate.isNotEmpty() && wish.targetDate != "null") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "目标日期 ${wish.targetDate}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // 第三行：状态 + 购入按钮 两端对齐
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 状态：小圆点 + 文字
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                statusText,
                                fontSize = 12.sp,
                                color = statusColor,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 标记已购买（仅未购买且 showPurchaseButton=true 时显示）
                        if (!wish.isPurchased && showPurchaseButton) {
                            AssistChip(
                                onClick = onPurchase,
                                label = {
                                    Text(
                                        "购入",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "标记已购买",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = MaterialTheme.shapes.medium,
                                colors = AssistChipDefaults.assistChipColors(
                                    leadingIconContentColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
