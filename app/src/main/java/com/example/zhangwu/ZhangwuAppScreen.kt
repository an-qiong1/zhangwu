package com.example.zhangwu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.Immutable
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.*
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhangwu.model.Asset
import com.example.zhangwu.ui.theme.ZhangwuTheme
import com.example.zhangwu.viewmodel.AssetViewModel
import android.widget.Toast
import com.example.zhangwu.AddAssetScreen
import com.example.zhangwu.AddWishScreen
import com.example.zhangwu.EditAssetScreen
import com.example.zhangwu.EditWishScreen
import com.example.zhangwu.CategoryManagerScreen
import com.example.zhangwu.BackupRestoreScreen
import com.example.zhangwu.FeedbackScreen
import com.example.zhangwu.RewardScreen
import com.example.zhangwu.CommonSearchScreen
import com.example.zhangwu.model.WishItem
import com.example.zhangwu.WishScreen
import androidx.compose.ui.platform.LocalContext
// 深色模式适配：使用Material Design 3的ColorScheme体系，替代硬编码色值
import com.example.zhangwu.viewmodel.WishViewModel
import com.example.zhangwu.viewmodel.CategoryViewModel
import com.example.zhangwu.viewmodel.ThemeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import com.example.zhangwu.LocalSubScreenState

// ===================== 排序类型枚举 =====================
enum class AssetSortType(val displayName: String) {
    ADD_TIME("添加时间"),
    PURCHASE_TIME("购买时间"),
    DAILY_COST("日均成本"),
    STATUS("物品状态"),
    SERVICE_DURATION("服役时长"),
    VALUE("物品价值")
}

// ===================== 子页面类型枚举 =====================
enum class SubScreenType {
    ADD_ASSET,
    ADD_WISHLIST,
    EDIT_ASSET,
    EDIT_WISHLIST,
    CATEGORY_MANAGER,
    BACKUP_RESTORE,
    FEEDBACK,
    REWARD,
    SEARCH_ASSET,
    SEARCH_WISHLIST
}

@Composable
fun ZhangwuAppScreen(
    assetViewModel: AssetViewModel = viewModel()
) {
    val allAssets by assetViewModel.allAssets.collectAsState(initial = emptyList())
    ZhangwuAppScreenContent(
        allAssets = allAssets,
        onInsertAsset = { assetViewModel.insertAsset(it) }
    )
}

// 风格改造：横向可滑动柔和胶囊按钮分类栏组件
@Composable
fun HorizontalScrollableCategoryBar(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    servingColor: Color, // 改造：接收新的状态颜色
    retiredColor: Color, // 改造：接收新的状态颜色
    soldColor: Color // 改造：接收新的状态颜色
) {
    // 使用横向滚动布局
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category
            val chipColor = when (category) {
                "通勤代步" -> servingColor // 改造：使用新的状态颜色
                "文娱数码" -> retiredColor // 改造：使用新的状态颜色
                "穿戴配饰" -> soldColor // 改造：使用新的状态颜色
                else -> servingColor // 改造：使用新的状态颜色
            }
            
            // 修复：移除描边/边框，使用Material You动态取色
            Surface(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large) // 风格改造：使用大圆角
                    .clickable(onClick = { onCategorySelect(category) })
                    .animateContentSize(tween(200)),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, // 修复：选中态使用Material You动态主色，未选中态无背景
                border = null // 修复：移除所有描边/边框
            ) {
                PaddingValues(horizontal = 16.dp, vertical = 8.dp).let { padding ->
                    Text(
                        category,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, // 修复：选中态使用onPrimary，未选中态使用onSurfaceVariant
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

/** 资产统计数据，单次遍历产出 */
@Immutable
private data class AssetStats(
    val serving: Int,
    val retired: Int,
    val sold: Int,
    val totalValue: Double,
    val totalDailyCost: Double
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    asset: Asset,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false
) {
    // 性能优化：缓存 Asset 计算属性，用 System.currentTimeMillis() 一次性快照，避免每次重组都 new Date()
    val usedDays = remember(asset.id, asset.purchaseDate) { asset.usedDays() }
    val totalDays = remember(asset.id, asset.expectedYears) { asset.totalDays }
    val progress = remember(asset.id, asset.purchaseDate, asset.expectedYears) { asset.progress() }
    val dailyCost = remember(asset.id, asset.purchasePrice, asset.purchaseDate) { asset.dailyCost() }

    // 修复：根据资产状态设置固定颜色，确保状态信息明显可见
    val statusColor = when (asset.status) {
        "服役中" -> Color(0xFF4ADE80) // 淡绿色
        "已退役" -> Color(0xFFFDE68A) // 淡黄色
        "已售出" -> Color(0xFFFCA5A5) // 淡红色
        else -> Color(0xFF4ADE80) // 默认淡绿色
    }

    val categoryIcon = when (asset.category) {
        "通勤代步" -> Icons.Default.DirectionsCar
        "文娱数码" -> Icons.Default.PhoneAndroid
        "穿戴配饰" -> Icons.Default.Watch
        else -> Icons.AutoMirrored.Filled.List // 修复：将默认手机图标替换为通用物品图标（列表）
    }

    // 方案A：整宽横向大卡片，左图右文，高度 wrap content，永不裁切
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
                    if (!asset.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(asset.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = asset.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            categoryIcon,
                            contentDescription = asset.category,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // ========== 右侧：文字信息区 ==========
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 第一行：名称
                    Text(
                        asset.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 第二行：价格 + 已使用天数
                    Text(
                        "${asset.price} · 已用 ${usedDays} 天",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 第三行：日均成本（始终显示，基于实际使用天数）
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            dailyCost,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Text(
                            "/天",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    if (asset.hasExpectedYears) {
                        // ===== 设置了预期年限：显示进度条 + 总天数 =====
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.small),
                            color = statusColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "已用 ${usedDays}/${totalDays} 天",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    asset.status,
                                    fontSize = 12.sp,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // ===== 未设置年限：只显示状态 =====
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                asset.status,
                                fontSize = 12.sp,
                                color = statusColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ZhangwuAppScreenContent(
    allAssets: List<Asset>,
    onInsertAsset: (Asset) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("资产", "心愿单", "设置")
    val icons = listOf(Icons.AutoMirrored.Filled.List, Icons.Default.Favorite, Icons.Default.Settings)

    // ===================== 页面状态管理 =====================
    var currentSubScreen by remember { mutableStateOf<SubScreenType?>(null) }
    // 编辑页面状态
    var currentEditingAsset by remember { mutableStateOf<Asset?>(null) }
    var currentEditingWish by remember { mutableStateOf<WishItem?>(null) }
    
    // 修复：获取子页面状态
    val isSubScreenVisible = LocalSubScreenState.current
    
    // 修复：使用ViewModel来管理全局可观察的数据
    val assetViewModel: AssetViewModel = viewModel()
    val wishViewModel: WishViewModel = viewModel()
    // 心愿单数据从 Room Flow 订阅
    val wishes by wishViewModel.wishItems.collectAsState()
    // 修复：使用CategoryViewModel管理分类数据，实现双向同步
    val categoryViewModel: CategoryViewModel = viewModel()
    // 修复：使用ThemeViewModel管理主题状态
    val themeViewModel: ThemeViewModel = viewModel()
    
    // 添加心愿的逻辑
    fun addWishItem(item: WishItem) {
        wishViewModel.addWishItem(item)
    }

    // ===================== 排序状态管理（新增） =====================
    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showCategoryTransferDialog by remember { mutableStateOf(false) }
    var showBatchActions by remember { mutableStateOf(false) }
    var showWishBatchActions by remember { mutableStateOf(false) }
    var currentSortType by remember { mutableStateOf(AssetSortType.DAILY_COST) }
    var isAscending by remember { mutableStateOf(false) } // 默认倒序（日均成本高的在前）

    // ===================== 分类筛选逻辑 =====================
    val selectedCategory by categoryViewModel.selectedCategory.collectAsState()
    val categoryList by categoryViewModel.categoryList.collectAsState()

    var selectedAssets by remember { mutableStateOf(emptyList<Asset>()) }
    var selectedWishes by remember { mutableStateOf(emptyList<WishItem>()) }
    
    // 当切换Tab时，取消批量选择模式
    LaunchedEffect(selectedTab) {
        showBatchActions = false
        showWishBatchActions = false
        selectedAssets = emptyList()
        selectedWishes = emptyList()
    }
    var selectedTag by remember { mutableStateOf("全部") }
    var newTagName by remember { mutableStateOf("") }

    // ===================== 筛选+排序后的资产列表（核心逻辑） =====================
    // 修复：remember 显式加所有依赖 key，保证输入变化时重建 producer
    // processedAssets 是卡片实际显示的列表，之前因为 key 缺失导致依赖更新后不重算
    val processedAssets by remember(
        selectedCategory,
        selectedTag,
        currentSortType,
        isAscending,
        allAssets
    ) {
        derivedStateOf {
            // 1. 先分类筛选
            val categoryFiltered = if (selectedCategory == "全部") allAssets
            else allAssets.filter { it.category == selectedCategory }

            // 2. 再标签筛选
            val tagFiltered = if (selectedTag == "全部") categoryFiltered
            else categoryFiltered.filter { it.tags.contains(selectedTag) }

            // 3. 再排序（升序用 compareBy，降序用 compareByDescending，避免额外 reversed() 创建新 Comparator）
            val ascendingComparator = compareBy { asset: Asset ->
                when (currentSortType) {
                    AssetSortType.ADD_TIME -> asset.id
                    AssetSortType.PURCHASE_TIME -> asset.purchaseDate
                    AssetSortType.DAILY_COST -> {
                        val days = asset.usedDays()
                        if (days > 0) asset.purchasePrice / days else Double.MAX_VALUE
                    }
                    AssetSortType.STATUS -> when (asset.status) {
                        "服役中" -> 0
                        "已退役" -> 1
                        "已售出" -> 2
                        else -> 3
                    }
                    AssetSortType.SERVICE_DURATION -> asset.usedDays()
                    AssetSortType.VALUE -> asset.purchasePrice
                }
            }
            val descendingComparator = compareByDescending { asset: Asset ->
                when (currentSortType) {
                    AssetSortType.ADD_TIME -> asset.id
                    AssetSortType.PURCHASE_TIME -> asset.purchaseDate
                    AssetSortType.DAILY_COST -> {
                        val days = asset.usedDays()
                        if (days > 0) asset.purchasePrice / days else Double.MAX_VALUE
                    }
                    AssetSortType.STATUS -> when (asset.status) {
                        "服役中" -> 0
                        "已退役" -> 1
                        "已售出" -> 2
                        else -> 3
                    }
                    AssetSortType.SERVICE_DURATION -> asset.usedDays()
                    AssetSortType.VALUE -> asset.purchasePrice
                }
            }
            if (isAscending) tagFiltered.sortedWith(ascendingComparator)
            else tagFiltered.sortedWith(descendingComparator)
        }
    }

    // 统计数据 - 性能优化：单次遍历 allAssets，5 个统计值一起算出
    val displayAssets = processedAssets
    val totalAssets = allAssets.size
    val stats by remember(allAssets) {
        derivedStateOf {
            var serving = 0; var retired = 0; var sold = 0
            var valueSum = 0.0; var dailyCostSum = 0.0
            for (a in allAssets) {
                when (a.status) {
                    "服役中" -> serving++
                    "已退役" -> retired++
                    "已售出" -> sold++
                }
                valueSum += a.purchasePrice
                // 日均成本基于实际使用天数
                val days = a.usedDays()
                if (days > 0) {
                    dailyCostSum += a.purchasePrice / days
                }
            }
            AssetStats(serving, retired, sold, valueSum, dailyCostSum)
        }
    }
    val servingCount = stats.serving
    val retiredCount = stats.retired
    val soldCount = stats.sold
    val totalValue = stats.totalValue
    val totalDailyCost = stats.totalDailyCost

    // 修复：使用Material You动态取色，移除硬编码颜色
    val currentServingColor = MaterialTheme.colorScheme.primary // 服役中：使用动态主色
    val currentRetiredColor = MaterialTheme.colorScheme.secondary // 已退役：使用动态辅色
    val currentSoldColor = MaterialTheme.colorScheme.tertiary // 已售出：使用动态第三色

    // 批量操作栏组件
    @Composable
    fun BatchActionsBar() {
        // 资产页批量操作栏
        if (selectedTab == 0 && showBatchActions && selectedAssets.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "已选择 ${selectedAssets.size} 项",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                // 取消选择
                                showBatchActions = false
                                selectedAssets = emptyList()
                            }
                        ) {
                            Text("取消")
                        }
                        Button(
                            onClick = {
                                // 批量转移分类
                                showCategoryTransferDialog = true
                            }
                        ) {
                            Text("转移分类")
                        }
                    }
                }
            }
        }
        
        // 心愿单批量操作栏
        if (selectedTab == 1 && showWishBatchActions && selectedWishes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "已选择 ${selectedWishes.size} 项",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                // 取消选择
                                showWishBatchActions = false
                                selectedWishes = emptyList()
                            }
                        ) {
                            Text("取消")
                        }
                        Button(
                            onClick = {
                                // 批量删除
                                selectedWishes.forEach {
                                    wishViewModel.deleteWishItem(it)
                                }
                                showWishBatchActions = false
                                selectedWishes = emptyList()
                            }
                        ) {
                            Text("删除")
                        }
                    }
                }
            }
        }
    }

    // 胶囊悬浮式导航栏组件
    @Composable
    fun TabBar() {
        // 只在一级页面显示Tab栏
        if (currentSubScreen == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp), false),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTab == index
                        Column(
                            modifier = Modifier
                                .clickable {
                                    selectedTab = index
                                }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = tab,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = tab,
                                fontSize = 12.sp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // 深色模式适配：使用动态主题色
        bottomBar = { TabBar() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 主内容区域（带 padding）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .background(MaterialTheme.colorScheme.background) // 深色模式适配：使用动态主题色
            ) {
                // ===================== 主内容（资产/心愿/设置）放在下层 =====================
                // 资产主页面
                if (selectedTab == 0) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 160.dp)
                    ) {
                        // 页面标题（整宽）— horizontalPadding=0 因 Grid contentPadding 已提供 16dp
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CommonPageTitle(
                                title = "掌物",
                                showSearchIcon = true,
                                horizontalPadding = 0.dp,
                                onSearchClick = {
                                    currentSubScreen = SubScreenType.SEARCH_ASSET
                                }
                            )
                        }

                        // 资产总览卡片（整宽）
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            // 风格改造：资产总览卡片，使用大圆角和柔和的背景色
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = MaterialTheme.shapes.large,
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // 风格改造：增加卡片阴影
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) { // 风格改造：增加内边距
                                    Text("资产总览 ($servingCount/$totalAssets)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp), // 风格改造：增加垂直间距
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("总资产", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) // 风格改造：增大字体
                                            Text("¥ ${"%.1f".format(totalValue)}", color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold) // 风格改造：增大字体
                                        }
                                        Column {
                                            Text("日均成本", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) // 风格改造：增大字体
                                            Text("¥ ${"%.2f".format(totalDailyCost)}", color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold) // 风格改造：增大字体
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val servingRatio = if (totalAssets == 0) 0f else servingCount.toFloat() / totalAssets
                                        val retiredRatio = if (totalAssets == 0) 0f else retiredCount.toFloat() / totalAssets
                                        val soldRatio = if (totalAssets == 0) 0f else soldCount.toFloat() / totalAssets
                                        Box(modifier = Modifier.weight(maxOf(0.001f, servingRatio)).height(6.dp).clip(MaterialTheme.shapes.small).background(currentServingColor)) // 改造：使用新的状态颜色
                                        Spacer(modifier = Modifier.width(6.dp)) // 风格改造：增加间距
                                        Box(modifier = Modifier.weight(maxOf(0.001f, retiredRatio)).height(6.dp).clip(MaterialTheme.shapes.small).background(currentRetiredColor)) // 改造：使用新的状态颜色
                                        Spacer(modifier = Modifier.width(6.dp)) // 风格改造：增加间距
                                        Box(modifier = Modifier.weight(maxOf(0.001f, soldRatio)).height(6.dp).clip(MaterialTheme.shapes.small).background(currentSoldColor)) // 改造：使用新的状态颜色
                                    }
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { // 风格改造：增加间距
                                        Text("服役中 $servingCount", color = currentServingColor, fontSize = 12.sp) // 改造：使用新的状态颜色
                                        Text("已退役 $retiredCount", color = currentRetiredColor, fontSize = 12.sp) // 改造：使用新的状态颜色
                                        Text("已售出 $soldCount", color = currentSoldColor, fontSize = 12.sp) // 改造：使用新的状态颜色
                                    }
                                }
                            }
                        }

                        // 分类+排序切换（整宽）
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            // ===================== 分类+排序切换（新增排序按钮） =====================
                            // 修复：给分类标签和筛选图标做独立布局，预留足够空间，彻底解决重叠
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                // 排序和筛选按钮固定在右上角
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(start = 8.dp)
                            ) {
                                // 筛选按钮
                                IconButton(
                                    onClick = {
                                        showSortDialog = false // 关闭排序对话框
                                        showFilterDialog = true
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "筛选",
                                        tint = MaterialTheme.colorScheme.onSurface // 深色模式适配：使用动态主题色
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // 排序按钮
                                IconButton(
                                    onClick = {
                                        showFilterDialog = false // 关闭筛选对话框
                                        showSortDialog = true
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        Icons.Default.SwapVert,
                                        contentDescription = "排序",
                                        tint = MaterialTheme.colorScheme.onSurface // 深色模式适配：使用动态主题色
                                    )
                                }
                            }

                                // 修复：分类栏改为横向可滑动布局，解决图标遮挡问题
                                // 分类筛选（在左侧，避免与排序按钮重叠）
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(end = 56.dp) // 为排序按钮预留空间
                                ) {
                                    HorizontalScrollableCategoryBar(
                                        categories = categoryList,
                                        selectedCategory = selectedCategory,
                                        onCategorySelect = { category ->
                                            categoryViewModel.selectCategory(category)
                                        },
                                        servingColor = currentServingColor, // 改造：传递新的状态颜色
                                        retiredColor = currentRetiredColor, // 改造：传递新的状态颜色
                                        soldColor = currentSoldColor // 改造：传递新的状态颜色
                                    )
                                }
                            }
                        }

                        // 间距（整宽）
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 资产卡片列表 - 性能优化：每个卡片独立 key，滑动时 Compose 能正确跳过未变的卡片
                        items(
                            items = displayAssets,
                            // 用 asset.id 作为 key，避免滑动时全量重组
                            key = { asset -> asset.id }
                        ) { asset ->
                            ItemCard(
                                asset = asset,
                                onClick = {
                                    if (showBatchActions) {
                                        selectedAssets = if (selectedAssets.contains(asset)) {
                                            selectedAssets.filter { it.id != asset.id }
                                        } else {
                                            selectedAssets + asset
                                        }
                                        if (selectedAssets.isEmpty()) {
                                            showBatchActions = false
                                        }
                                    } else {
                                        currentEditingAsset = asset
                                        currentSubScreen = SubScreenType.EDIT_ASSET
                                    }
                                },
                                onLongClick = {
                                    showBatchActions = true
                                    selectedAssets = listOf(asset)
                                },
                                isSelected = selectedAssets.contains(asset)
                            )
                        }

                        // 修复：空状态提示，避免"看不到卡片"误判为数据丢失
                        if (displayAssets.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (selectedCategory == "全部" && selectedTag == "全部")
                                            "暂无资产"
                                        else
                                            "当前分类/标签下没有资产（分类=$selectedCategory，标签=$selectedTag）",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "共 ${allAssets.size} 条资产，已从数据库恢复",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
                // ===================== 心愿单页面 =====================
                else if (selectedTab == 1) {
                    WishScreen(
                        onSearchClick = {
                            currentSubScreen = SubScreenType.SEARCH_WISHLIST
                        },
                        onWishClick = { wish ->
                            // 修复：点击心愿卡片跳转到编辑页面
                            currentEditingWish = wish
                            currentSubScreen = SubScreenType.EDIT_WISHLIST
                        },
                        showBatchActions = showWishBatchActions,
                        selectedWishes = selectedWishes,
                        onBatchActionsChange = { showWishBatchActions = it },
                        onSelectedWishesChange = { selectedWishes = it },
                        // 新增：购买心愿所需的分类列表
                        categories = categoryList,
                        // 新增：购买心愿回调，将心愿转为资产
                        onPurchaseWish = { wish, category, purchaseDate, expectedYears ->
                            wishViewModel.purchaseWish(wish, category, purchaseDate, expectedYears)
                        }
                    )
                }
                // ===================== 设置页面 =====================
                else if (selectedTab == 2) {
                    SettingScreen(
                        assetsList = allAssets,
                        wishlistItems = wishes,
                        categoriesList = categoryList,
                        onOpenCategoryManager = { currentSubScreen = SubScreenType.CATEGORY_MANAGER },
                        onOpenBackupRestore = { currentSubScreen = SubScreenType.BACKUP_RESTORE },
                        onOpenFeedback = { currentSubScreen = SubScreenType.FEEDBACK },
                        onOpenReward = { currentSubScreen = SubScreenType.REWARD },
                        onRestoreSuccess = { restoredAssets, restoredWishes, restoredCategories ->
                            // 修复：真正写回数据库（之前是空实现，导入的备份根本没生效）
                            assetViewModel.restoreAllAssets(restoredAssets)
                            wishViewModel.insertAll(restoredWishes)
                            categoryViewModel.restoreCategories(restoredCategories)
                        }
                    )
                }
            }

            // ===================== 浮动操作按钮（FAB） =====================
            if (currentSubScreen == null) {
                if (selectedTab == 0) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        FloatingActionButton(
                            onClick = { currentSubScreen = SubScreenType.ADD_ASSET },
                            containerColor = MaterialTheme.colorScheme.primary, // 修复：使用Material You动态主色，移除硬编码绿色
                            contentColor = MaterialTheme.colorScheme.onPrimary, // 修复：使用MaterialTheme颜色方案
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 140.dp, end = 24.dp) // 强制修复：按钮再向上移动20dp，与之前移动长度相同，确保与Tab栏顶部有足够安全间距，无任何遮挡
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加资产")
                        }
                    }
                } else if (selectedTab == 1) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        FloatingActionButton(
                            onClick = { currentSubScreen = SubScreenType.ADD_WISHLIST },
                            containerColor = MaterialTheme.colorScheme.primary, // 深色模式适配：使用动态主题色
                            contentColor = MaterialTheme.colorScheme.onPrimary, // 深色模式适配：使用动态主题色
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 140.dp, end = 24.dp) // 与资产页保持一致
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加心愿")
                        }
                    }
                }
            }

            // 批量操作栏（显示在Tab栏上方）
            if (currentSubScreen == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    BatchActionsBar()
                }
            }
        }

        // 修复：当显示子页面时，更新子页面状态
        LaunchedEffect(currentSubScreen) {
            isSubScreenVisible.value = currentSubScreen != null
        }

        // ===================== 子页面（各功能独立界面）放在最上层 =====================
        when (currentSubScreen) {
            SubScreenType.ADD_ASSET -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    AddAssetScreen(
                        onBackClick = { currentSubScreen = null },
                        onSaveAsset = onInsertAsset
                    )
                    // 修复：添加BackHandler处理系统返回键
                    BackHandler {
                        currentSubScreen = null
                    }
                }
            }
            SubScreenType.ADD_WISHLIST -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    AddWishScreen(
                        onBackClick = { currentSubScreen = null },
                        onSaveWishlist = { addWishItem(it) }
                    )
                    // 修复：添加BackHandler处理系统返回键
                    BackHandler {
                        currentSubScreen = null
                    }
                }
            }
            SubScreenType.EDIT_ASSET -> {
                currentEditingAsset?.let {
                    Box(modifier = Modifier.fillMaxSize()) {
                        EditAssetScreen(
                            asset = it,
                            onBackClick = { currentSubScreen = null },
                            onSaveAsset = { assetViewModel.updateAsset(it) },
                            onDeleteAsset = { assetViewModel.deleteAsset(it) }
                        )
                        // 修复：添加BackHandler处理系统返回键
                        BackHandler {
                            currentSubScreen = null
                        }
                    }
                }
            }
            SubScreenType.EDIT_WISHLIST -> {
                currentEditingWish?.let {
                    Box(modifier = Modifier.fillMaxSize()) {
                        EditWishScreen(
                            wish = it,
                            onBackClick = { currentSubScreen = null },
                            onSaveWish = { wishViewModel.updateWishItem(it) },
                            onDeleteWish = { wishViewModel.deleteWishItem(it) },
                            // 新增：购买心愿相关参数
                            categories = categoryList,
                            onPurchaseWish = { wish, category, purchaseDate, expectedYears ->
                                wishViewModel.purchaseWish(wish, category, purchaseDate, expectedYears)
                            }
                        )
                        // 修复：添加BackHandler处理系统返回键
                        BackHandler {
                            currentSubScreen = null
                        }
                    }
                }
            }
            SubScreenType.SEARCH_ASSET -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CommonSearchScreen(
                        title = "搜索资产",
                        originalList = allAssets,
                        itemContent = { 
                            ItemCard(
                                asset = it, 
                                onClick = { currentEditingAsset = it; currentSubScreen = SubScreenType.EDIT_ASSET }
                            ) 
                        },
                        filterRule = { asset, query -> asset.name.contains(query, ignoreCase = true) },
                        onBackClick = { currentSubScreen = null }
                    )
                    // 修复：添加BackHandler处理系统返回键
                    BackHandler {
                        currentSubScreen = null
                    }
                }
            }
            SubScreenType.SEARCH_WISHLIST -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CommonSearchScreen(
                        title = "搜索心愿单",
                        originalList = wishes,
                        itemContent = { wish -> WishCard(
                            wish = wish,
                            onClick = { currentEditingWish = wish; currentSubScreen = SubScreenType.EDIT_WISHLIST },
                            onLongClick = {},
                            isSelected = false,
                            showPurchaseButton = false
                        ) },
                        filterRule = { item, query -> item.name.contains(query, ignoreCase = true) },
                        onBackClick = { currentSubScreen = null }
                    )
                    // 修复：添加BackHandler处理系统返回键
                    BackHandler {
                        currentSubScreen = null
                    }
                }
            }
            SubScreenType.CATEGORY_MANAGER -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CategoryManagerScreen(
                        onBackClick = { currentSubScreen = null }
                    )
                    // 修复：添加BackHandler处理系统返回键
                    BackHandler {
                        currentSubScreen = null
                    }
                }
            }
            SubScreenType.BACKUP_RESTORE -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    BackupRestoreScreen(
                        onBackClick = { currentSubScreen = null },
                        assetsList = allAssets,
                        wishlistItems = wishes,
                        categoriesList = categoryList,
                        onRestoreSuccess = { restoredAssets, restoredWishes, restoredCategories ->
                            // 修复：真正写回数据库
                            assetViewModel.restoreAllAssets(restoredAssets)
                            wishViewModel.insertAll(restoredWishes)
                            categoryViewModel.restoreCategories(restoredCategories)
                        }
                    )
                    // 修复：添加BackHandler处理系统返回键
                    BackHandler {
                        currentSubScreen = null
                    }
                }
            }
            SubScreenType.FEEDBACK -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    FeedbackScreen(
                        onBackClick = { currentSubScreen = null }
                    )
                    // 修复：添加BackHandler处理系统返回键
                    BackHandler {
                        currentSubScreen = null
                    }
                }
            }
            SubScreenType.REWARD -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    RewardScreen(
                        onBackClick = { currentSubScreen = null }
                    )
                    // 修复：添加BackHandler处理系统返回键
                    BackHandler {
                        currentSubScreen = null
                    }
                }
            }
            null -> {
                // 不显示任何子页面
            }
        }

        // ===================== 排序选择弹窗（新增） =====================
        if (showSortDialog) {
            ModalBottomSheet(
                onDismissRequest = { showSortDialog = false },
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
                        "资产排序",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 排序类型选择
                    Text(
                        "排序维度",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AssetSortType.values().forEach { sortType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { currentSortType = sortType }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentSortType == sortType,
                                onClick = { currentSortType = sortType },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = currentServingColor // 改造：使用服役中颜色
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                sortType.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // 正序/倒序切换
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "排序方式",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !isAscending,
                                onClick = { isAscending = false },
                                label = { Text("倒序") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = currentServingColor, // 改造：使用服役中颜色
                                    selectedLabelColor = Color.White
                                ),
                                border = null
                            )
                            FilterChip(
                                selected = isAscending,
                                onClick = { isAscending = true },
                                label = { Text("正序") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = currentServingColor, // 改造：使用服役中颜色
                                    selectedLabelColor = Color.White
                                ),
                                border = null
                            )
                        }
                    }

                    // 确定按钮
                    Button(
                        onClick = { showSortDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentServingColor, // 改造：使用服役中颜色
                            contentColor = Color.White
                        )
                    ) {
                        Text("确定", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ===================== 筛选对话框 =====================
        if (showFilterDialog) {
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                title = { Text("筛选") },
                text = {
                    Column {
                        Text("标签筛选")
                        Spacer(modifier = Modifier.height(8.dp))
                        // 提取所有唯一标签
                        val allTags = allAssets.flatMap { it.tags }.distinct().sorted()
                        val tagsWithAll = listOf("全部") + allTags
                        LazyColumn {
                            items(tagsWithAll) { tag ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTag = tag
                                            showFilterDialog = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedTag == tag,
                                        onClick = {
                                            selectedTag = tag
                                            showFilterDialog = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tag)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showFilterDialog = false }
                    ) {
                        Text("确定")
                    }
                }
            )
        }

        // ===================== 分类转移对话框 =====================
        if (showCategoryTransferDialog) {
            var selectedTargetCategory by remember { mutableStateOf("全部") }
            AlertDialog(
                onDismissRequest = { showCategoryTransferDialog = false },
                title = { Text("批量转移分类") },
                text = {
                    Column {
                        Text("选择目标分类")
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn {
                            items(categoryList) { category ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTargetCategory = category
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedTargetCategory == category,
                                        onClick = {
                                            selectedTargetCategory = category
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(category)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // 批量转移分类
                            selectedAssets.forEach { asset ->
                                val updatedAsset = asset.copy(category = selectedTargetCategory)
                                assetViewModel.updateAsset(updatedAsset)
                            }
                            showCategoryTransferDialog = false
                            showBatchActions = false
                            selectedAssets = emptyList()
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCategoryTransferDialog = false }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
    }
}
