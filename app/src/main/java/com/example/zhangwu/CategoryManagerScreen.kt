package com.example.zhangwu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import com.example.zhangwu.ui.theme.ZhangwuTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.example.zhangwu.CommonPageTitle
import com.example.zhangwu.viewmodel.CategoryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 资产分类管理页面
 * 功能：拖拽排序 / 添加分类 / 删除分类（默认分类锁定）
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryManagerScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    // 修复：使用CategoryViewModel管理分类数据，实现双向同步
    val categoryViewModel: CategoryViewModel = viewModel()
    val categoryList by categoryViewModel.categoryList.collectAsState()
    // 添加分类弹窗
    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    // 拖拽排序状态
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            // 修复：使用ViewModel的reorderCategories方法进行排序
            val newOrder = categoryList.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            categoryViewModel.reorderCategories(newOrder)
        }
    )

    // 添加分类逻辑
    fun addCategory() {
        if (newCategoryName.isBlank()) {
            return
        }
        if (categoryList.contains(newCategoryName)) {
            return
        }
        // 修复：使用ViewModel的addCategory方法添加分类
        categoryViewModel.addCategory(newCategoryName)
        newCategoryName = ""
        showAddDialog = false
    }

    // 删除分类逻辑（默认分类不可删除）
    fun deleteCategory(category: String) {
        // 修复：使用ViewModel的deleteCategory方法删除分类
        categoryViewModel.deleteCategory(category)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        ) {
            CommonPageTitle(
                title = "分类管理",
                showBackButton = true,
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
            Text(
                text = "长按拖拽排序，默认分类不可删除",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 分类列表（支持拖拽排序）
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                itemsIndexed(categoryList, key = { index, category -> "$index-$category" }) { index, category ->
                    val isDefaultCategory = categoryViewModel.isDefaultCategory(category)
                    ReorderableItem(
                        state = reorderableState,
                        key = "$index-$category"
                    ) { isDragging ->
                        // 分类项卡片
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .scale(if (isDragging) 1.02f else 1.0f) // 修复：添加拖拽反馈动画，拖拽时轻微放大
                                    .shadow(if (isDragging) 12.dp else 2.dp, RoundedCornerShape(12.dp)), // 修复：添加拖拽反馈动画，拖拽时增加阴影
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 左侧拖拽图标 + 分类名称
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // 拖拽手柄（默认分类隐藏，使用Spacer占位）
                                    if (!isDefaultCategory) {
                                        Icon(
                                            Icons.Default.DragIndicator,
                                            contentDescription = "拖拽排序",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .draggableHandle(),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    } else {
                                        // 修复：为默认分类添加占位符，确保高度一致
                                        Spacer(modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }

                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )

                                    // 默认分类标记
                                    if (isDefaultCategory) {
                                        Text(
                                            text = "（默认）",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }

                                // 删除按钮（仅自定义分类显示，默认分类使用Spacer占位）
                                if (!isDefaultCategory) {
                                    IconButton(
                                        onClick = { deleteCategory(category) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "删除分类",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                } else {
                                    // 修复：为默认分类添加占位符，确保高度一致
                                    Spacer(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        // 添加分类按钮
        Box(modifier = Modifier.fillMaxSize()) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加分类")
            }
        }

        // 添加分类弹窗
        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "添加新分类",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("分类名称") },
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                showAddDialog = false
                                newCategoryName = ""
                            }) {
                                Text("取消")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { addCategory() }) {
                                Text("确定")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryManagerScreenPreview() {
    ZhangwuTheme {
        CategoryManagerScreen(onBackClick = {})
    }
}