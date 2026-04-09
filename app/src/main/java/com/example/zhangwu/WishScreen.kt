package com.example.zhangwu

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
    onSelectedWishesChange: (List<WishItem>) -> Unit = {}
) {
    // 修复：使用ViewModel来管理全局可观察的数据
    val wishViewModel: WishViewModel = viewModel()
    
    // 修复：页面生命周期与数据刷新逻辑
    // 当页面可见时，确保数据是最新的
    LaunchedEffect(Unit) {
        // 这里可以添加数据刷新逻辑，比如从网络或本地存储加载数据
    }
    
    // 修复：直接使用所有心愿列表，去除分类筛选
    val wishes = wishViewModel.wishItems
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // 深色模式适配：使用动态主题色
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 统一标题栏
            CommonPageTitle(
                title = "心愿单",
                showSearchIcon = true,
                onSearchClick = onSearchClick
            )

            // 内容区域
            // 修复：空状态与列表状态的切换逻辑
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
                            tint = MaterialTheme.colorScheme.primary // 深色模式适配：使用动态主题色
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
                // 心愿列表
                Spacer(modifier = Modifier.height(24.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 160.dp), // 修复：增加底部内边距，确保最后一行卡片不被Tab栏遮挡
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(wishes) { wish ->
                        WishCard(
                            wish = wish,
                            onClick = {
                                if (showBatchActions) {
                                    // 批量选择模式：切换选择状态
                                    val newSelectedWishes = if (selectedWishes.contains(wish)) {
                                        selectedWishes.filter { it.id != wish.id }
                                    } else {
                                        selectedWishes + wish
                                    }
                                    onSelectedWishesChange(newSelectedWishes)
                                    // 如果没有选中的心愿，退出批量选择模式
                                    if (newSelectedWishes.isEmpty()) {
                                        onBatchActionsChange(false)
                                    }
                                } else {
                                    // 正常模式：跳转到编辑页面
                                    onWishClick(wish)
                                }
                            },
                            onLongClick = {
                                // 长按进入批量选择模式
                                onBatchActionsChange(true)
                                onSelectedWishesChange(listOf(wish))
                            },
                            isSelected = selectedWishes.contains(wish)
                        )
                    }
                }
            }
        }
        

        

    }
}

/**
 * 风格改造：心愿卡片，使用大圆角和柔和的设计
 * @param wish 心愿数据
 * @param onClick 点击事件回调
 * @param onLongClick 长按事件回调
 * @param isSelected 是否被选中
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WishCard(
    wish: WishItem, 
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false
) {
    val statusColor = if (wish.savedAmount >= wish.targetAmount) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary // 深色模式适配：使用动态主题色
    val statusText = if (wish.savedAmount >= wish.targetAmount) "已完成" else "进行中"
    
    // 改造：正方形心愿卡片，和资产页卡片风格统一
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // 改造：卡片改为正方形，宽高比1:1
            .shadow(8.dp, MaterialTheme.shapes.large, false) // 风格改造：使用大圆角阴影
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ), // 点击和长按事件
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.surfaceVariant // 选中状态：使用surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface // 未选中状态：使用surface
            }
        ), // Material You适配：使用动态主题色
        shape = MaterialTheme.shapes.large, // 风格改造：使用大圆角
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // 风格改造：增加卡片阴影
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 选择状态指示
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
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
            Column(modifier = Modifier.padding(20.dp)) { // 风格改造：增加内边距
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 风格改造：左侧爱心图标
                    Box(
                        modifier = Modifier
                            .size(48.dp) // 风格改造：增大图标容器
                            .clip(MaterialTheme.shapes.medium) // 风格改造：使用中圆角
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "心愿", tint = statusColor, modifier = Modifier.size(24.dp)) // 风格改造：增大图标
                    }
                    Spacer(modifier = Modifier.width(16.dp)) // 风格改造：增加间距
                    // 心愿名称
                    Text(
                        wish.name, 
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2, // 风格改造：允许两行显示
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp)) // 风格改造：增加间距
                // 目标价格和日期
                Text("目标价格: ¥${wish.price}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                // 只在有目标日期时显示
                if (wish.targetDate.isNotEmpty() && wish.targetDate != "null") {
                    Text("目标日期: ${wish.targetDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(16.dp)) // 风格改造：增加间距

                // 攒钱进度
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${(wish.progress * 100).toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = statusColor) // 风格改造：增大字体
                    Text(" 完成", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
                }

                Spacer(modifier = Modifier.height(16.dp)) // 风格改造：增加间距
                // 进度条
                LinearProgressIndicator(
                    progress = { wish.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(MaterialTheme.shapes.small), // 风格改造：增加进度条高度，使用小圆角
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                // 剩余金额
                Text(
                    "还需 ¥${"%.2f".format(wish.remainingAmount)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 风格改造：状态标识，右下角纯色小圆点+文字
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp), // 风格改造：增加间距
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 纯色小圆点
                    Box(
                        modifier = Modifier
                            .size(8.dp) // 风格改造：增大圆点
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp)) // 风格改造：增加间距
                    // 状态文字
                    Text(
                        statusText,
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


