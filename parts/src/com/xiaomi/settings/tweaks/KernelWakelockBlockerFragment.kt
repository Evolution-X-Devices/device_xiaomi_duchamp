/*
 * ==============================================================
 *    __  __  _____   __    ___    ___   _____   _
 *   |  \/  ||_   _| / /_  ( _ )  / _ \ |___  | / \
 *   | |\/| |  | |  | '_ \ / _ \ | (_) |   / / / _ \
 *   | |  | |  | |  | (_) | (_) | \__, |  / / / ___ \
 *   |_|  |_|  |_|   \___/ \___/    /_/  /_/ /_/   \_\
 *
 *  SPDX-FileCopyrightText: 2024 The Evolution X Project
 *  SPDX-FileCopyrightText: 2026 MT6897A
 *  SPDX-License-Identifier: Apache-2.0
 * ==============================================================
 */

package com.xiaomi.settings.tweaks

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.xiaomi.settings.R
import com.xiaomi.settings.htsr.ui.AppListAnimatedContent
import com.xiaomi.settings.htsr.ui.CheckCloseSwitch
import com.xiaomi.settings.htsr.ui.HTSRAnimatedVisibility
import com.xiaomi.settings.htsr.ui.HTSREmptyState
import com.xiaomi.settings.htsr.ui.HTSRHeaderCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KernelWakelockBlockerFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = getString(R.string.wakelock_blocker_kernel_title)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val isDark = isSystemInDarkTheme()
            val colors = if (isDark) dynamicDarkColorScheme(requireContext()) else dynamicLightColorScheme(requireContext())
            MaterialTheme(colorScheme = colors) {
                Surface(
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    KernelWakelockBlockerContent(context = requireContext())
                }
            }
        }
    }
}

private enum class FilterCategory {
    ALL, SAFE, CAUTION, DANGER
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun KernelWakelockBlockerContent(context: Context) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var blockerEnabled by remember {
        mutableStateOf(WakelockBlockerUtils.isKernelBlockerEnabled(context))
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FilterCategory.ALL) }
    var totalHits by remember { mutableStateOf(WakelockBlockerUtils.getTotalHits()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var customWakelockName by remember { mutableStateOf("") }
    val wakelocks = remember { mutableStateListOf<KernelWakelockInfo>() }

    fun refreshData() {
        isLoading = true
        scope.launch(Dispatchers.IO) {
            val list = WakelockBlockerUtils.getDiscoveredKernelWakelocks(context)
            val hits = WakelockBlockerUtils.getTotalHits()
            withContext(Dispatchers.Main) {
                wakelocks.clear()
                wakelocks.addAll(list)
                totalHits = hits
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    val activeBlockedCount = wakelocks.count { it.isBlocked }

    val filteredList = remember(searchQuery, selectedFilter, wakelocks.toList()) {
        val q = searchQuery.trim().lowercase()
        wakelocks.filter { item ->
            val itemDesc = context.getString(item.descriptionRes).lowercase()
            val matchesSearch = q.isEmpty() ||
                item.name.lowercase().contains(q) ||
                itemDesc.contains(q)
            val matchesFilter = when (selectedFilter) {
                FilterCategory.ALL -> true
                FilterCategory.SAFE -> item.level == WakelockDangerLevel.SAFE
                FilterCategory.CAUTION -> item.level == WakelockDangerLevel.CAUTION
                FilterCategory.DANGER -> item.level == WakelockDangerLevel.DANGER
            }
            matchesSearch && matchesFilter
        }
    }

    val hasDangerousBlocked = wakelocks.any { it.isBlocked && it.level == WakelockDangerLevel.DANGER }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Master Header Card — unique title "Kernel Wakelocks"
            HTSRHeaderCard(
                title = stringResource(R.string.wakelock_blocker_kernel_title),
                subtitle = when {
                    !blockerEnabled -> stringResource(R.string.wakelock_blocker_kernel_disabled_summary)
                    activeBlockedCount == 0 -> stringResource(R.string.wakelock_blocker_kernel_none_configured)
                    totalHits > 0 -> stringResource(R.string.wakelock_blocker_kernel_count_with_hits, activeBlockedCount, totalHits)
                    else -> stringResource(R.string.wakelock_blocker_kernel_count, activeBlockedCount)
                },
                checked = blockerEnabled,
                onCheckedChange = { checked ->
                    blockerEnabled = checked
                    if (checked && WakelockBlockerUtils.getBlockedKernelWakelocks(context).isEmpty()) {
                        WakelockBlockerUtils.applySafePreset(context)
                    }
                    WakelockBlockerUtils.setKernelBlockerEnabled(context, checked)
                    refreshData()
                },
            ) {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_tweaks),
                    contentDescription = null,
                    tint = if (blockerEnabled) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smooth animated collapse for the entire screen below the header card
            HTSRAnimatedVisibility(visible = blockerEnabled) {
                Column {
                    // Search Field and Add Custom Wakelock button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(R.string.wakelock_blocker_kernel_search_hint)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_clear_search))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            enabled = blockerEnabled,
                        )

                        FilledTonalIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showAddDialog = true
                            },
                            enabled = blockerEnabled,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(52.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = stringResource(R.string.wakelock_blocker_kernel_add_custom),
                            )
                        }
                    }

                    if (showAddDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showAddDialog = false
                                customWakelockName = ""
                            },
                            title = { Text(stringResource(R.string.wakelock_blocker_kernel_add_dialog_title)) },
                            text = {
                                OutlinedTextField(
                                    value = customWakelockName,
                                    onValueChange = { customWakelockName = it },
                                    placeholder = { Text(stringResource(R.string.wakelock_blocker_kernel_add_dialog_hint)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val name = customWakelockName.trim()
                                        if (name.isNotEmpty()) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            WakelockBlockerUtils.toggleKernelWakelock(context, name, true)
                                            refreshData()
                                        }
                                        showAddDialog = false
                                        customWakelockName = ""
                                    },
                                    enabled = customWakelockName.isNotBlank(),
                                ) {
                                    Text(stringResource(R.string.wakelock_blocker_kernel_add_dialog_confirm))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showAddDialog = false
                                        customWakelockName = ""
                                    }
                                ) {
                                    Text(stringResource(R.string.wakelock_blocker_kernel_add_dialog_cancel))
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons: Safe Preset, Reset Hits, Clear All
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                WakelockBlockerUtils.applySafePreset(context)
                                refreshData()
                            },
                            enabled = blockerEnabled,
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(Icons.Rounded.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.wakelock_blocker_kernel_safe_preset), maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                WakelockBlockerUtils.resetHitCounts()
                                refreshData()
                            },
                            enabled = blockerEnabled,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.wakelock_blocker_kernel_reset_hits), maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                WakelockBlockerUtils.clearBlockedKernelWakelocks(context)
                                refreshData()
                            },
                            enabled = blockerEnabled,
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.wakelock_blocker_kernel_clear_all), maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Chips: All / Safe / Caution / Risky
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilterChip(
                            selected = selectedFilter == FilterCategory.ALL,
                            onClick = { selectedFilter = FilterCategory.ALL },
                            label = { Text(stringResource(R.string.wakelock_blocker_filter_all)) },
                            modifier = Modifier.weight(1f),
                        )
                        FilterChip(
                            selected = selectedFilter == FilterCategory.SAFE,
                            onClick = { selectedFilter = FilterCategory.SAFE },
                            label = { Text(stringResource(R.string.wakelock_blocker_filter_safe)) },
                            modifier = Modifier.weight(1f),
                        )
                        FilterChip(
                            selected = selectedFilter == FilterCategory.CAUTION,
                            onClick = { selectedFilter = FilterCategory.CAUTION },
                            label = { Text(stringResource(R.string.wakelock_blocker_filter_caution)) },
                            modifier = Modifier.weight(1f),
                        )
                        FilterChip(
                            selected = selectedFilter == FilterCategory.DANGER,
                            onClick = { selectedFilter = FilterCategory.DANGER },
                            label = { Text(stringResource(R.string.wakelock_blocker_filter_danger)) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Warning Banner when dangerous wakelocks are active
                    AnimatedVisibility(
                        visible = hasDangerousBlocked,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.wakelock_blocker_warning_title),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                    Text(
                                        text = stringResource(R.string.wakelock_blocker_warning_message),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Smooth animated content transitions matching Per-App
                    AppListAnimatedContent(
                        isLoading = isLoading,
                        isEmpty = filteredList.isEmpty(),
                        hasSearchQuery = searchQuery.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        emptyContent = {
                            HTSREmptyState(
                                icon = ImageVector.vectorResource(R.drawable.ic_tweaks),
                                title = stringResource(R.string.wakelock_blocker_empty_kernel),
                                description = searchQuery.ifEmpty { stringResource(R.string.wakelock_blocker_kernel_summary) },
                                modifier = Modifier.fillMaxSize(),
                            )
                        },
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(filteredList, key = { it.name }) { item ->
                                KernelWakelockCard(
                                    item = item,
                                    enabled = blockerEnabled,
                                    onToggle = { willBlock ->
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        WakelockBlockerUtils.toggleKernelWakelock(context, item.name, willBlock)
                                        val index = wakelocks.indexOfFirst { it.name == item.name }
                                        if (index != -1) {
                                            wakelocks[index] = item.copy(isBlocked = willBlock)
                                        }
                                    },
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun KernelWakelockCard(
    item: KernelWakelockInfo,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val isDark = isSystemInDarkTheme()

    val (badgeBg, badgeText, badgeLabelRes, badgeIcon) = when (item.level) {
        WakelockDangerLevel.SAFE -> Quadruple(
            if (isDark) Color(0xFF1B5E20).copy(alpha = 0.35f) else Color(0xFFE8F5E9),
            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
            R.string.wakelock_blocker_badge_safe,
            Icons.Rounded.CheckCircle,
        )
        WakelockDangerLevel.CAUTION -> Quadruple(
            if (isDark) Color(0xFFE65100).copy(alpha = 0.35f) else Color(0xFFFFF3E0),
            if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100),
            R.string.wakelock_blocker_badge_caution,
            Icons.Rounded.Info,
        )
        WakelockDangerLevel.DANGER -> Quadruple(
            if (isDark) Color(0xFFB71C1C).copy(alpha = 0.35f) else Color(0xFFFFEBEE),
            if (isDark) Color(0xFFE57373) else Color(0xFFC62828),
            R.string.wakelock_blocker_badge_danger,
            Icons.Rounded.Warning,
        )
        WakelockDangerLevel.UNKNOWN -> Quadruple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            R.string.wakelock_blocker_badge_driver,
            Icons.Rounded.Memory,
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (item.isBlocked) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onToggle(!item.isBlocked) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon Pill
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeText,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Information Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Category Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg,
                    ) {
                        Text(
                            text = stringResource(badgeLabelRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                // Hit counter pill
                if (item.hitCount > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.wakelock_blocker_hits_blocked, item.hitCount),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(item.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // CheckCloseSwitch — matching Per-App style
            CheckCloseSwitch(
                checked = item.isBlocked,
                onCheckedChange = { onToggle(it) },
                enabled = enabled,
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
