/*
 * ==============================================================
 *    __  __  _____   __    ___    ___   _____   _
 *   |  \/  ||_   _| / /_  ( _ )  / _ \ |___  | / \
 *   | |\/| |  | |  | '_ \ / _ \ | (_) |   / / / _ \
 *   | |  | |  | |  | (_) | (_) | \__, |  / / / ___ \
 *   |_|  |_|  |_|   \___/ \___/    /_/  /_/ /_/   \_\
 *
 *  SPDX-FileCopyrightText: 2024 The Evolution X Project
 *  SPDX-FileCopyRightText: 2026 MT6897A
 *  SPDX-License-Identifier: Apache-2.0
 * ==============================================================
 */

package com.xiaomi.settings.tweaks

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.xiaomi.settings.R
import com.xiaomi.settings.htsr.ui.AppListAnimatedContent
import com.xiaomi.settings.htsr.ui.AppListEntry
import com.xiaomi.settings.htsr.ui.AppPickerItem
import com.xiaomi.settings.htsr.ui.AppPickerSearchField
import com.xiaomi.settings.htsr.ui.HTSRAnimatedVisibility
import com.xiaomi.settings.htsr.ui.HTSREmptyState
import com.xiaomi.settings.htsr.ui.HTSRHeaderCard
import com.xiaomi.settings.htsr.ui.filterInstalledApps
import com.xiaomi.settings.htsr.ui.targetedFirstComparator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WakelockBlockerAppListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = getString(R.string.wakelock_blocker_app_list_title)
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
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    WakelockBlockerAppsContent(context = requireContext())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WakelockBlockerAppsContent(context: Context) {
    val pm = context.packageManager
    val scope = rememberCoroutineScope()

    val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    var blockerEnabled by remember {
        mutableStateOf(WakelockBlockerUtils.isAppBlockerEnabled(context))
    }

    var searchQuery by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val allApps = remember { mutableStateListOf<AppListEntry>() }

    fun applyChange() {
        val current = allApps.filter { it.isSelected }.map { it.packageName }.toSet()
        WakelockBlockerUtils.setBlockedApps(context, current)
    }

    LaunchedEffect(showSystemApps) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val targetsSet = WakelockBlockerUtils.getBlockedApps(context)

            val installed = filterInstalledApps(
                pm = pm,
                showSystem = showSystemApps,
                targeted = targetsSet,
                hidden = emptySet(),
                extraFilter = { app ->
                    !app.packageName.contains("android.settings")
                },
            )
                .sortedWith(targetedFirstComparator(pm, targetsSet))
                .map { app ->
                    AppListEntry(
                        packageName = app.packageName,
                        label = pm.getApplicationLabel(app).toString(),
                        icon = runCatching { pm.getApplicationIcon(app) }.getOrNull(),
                        isSystem = app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0,
                        isSelected = app.packageName in targetsSet,
                    )
                }

            withContext(Dispatchers.Main) {
                allApps.clear()
                allApps.addAll(installed)
                isLoading = false
            }
        }
    }

    val filteredApps = remember(searchQuery, allApps.toList()) {
        val q = searchQuery.lowercase()
        allApps.filter { app ->
            q.isEmpty() ||
                app.label.lowercase().contains(q) ||
                app.packageName.lowercase().contains(q)
        }
    }

    val activeCount = allApps.count { it.isSelected }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            HTSRHeaderCard(
                title = stringResource(R.string.wakelock_blocker_per_app_title),
                subtitle = when {
                    !blockerEnabled -> stringResource(R.string.wakelock_blocker_per_app_disabled_summary)
                    activeCount == 0 -> stringResource(R.string.wakelock_blocker_none_configured)
                    else -> stringResource(R.string.wakelock_blocker_count, activeCount)
                },
                checked = blockerEnabled,
                onCheckedChange = { checked ->
                    blockerEnabled = checked
                    WakelockBlockerUtils.setAppBlockerEnabled(context, checked)
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

            HTSRAnimatedVisibility(visible = blockerEnabled) {
                Column {
                    AppPickerSearchField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        enabled = blockerEnabled,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = showSystemApps,
                            onClick = { if (blockerEnabled) showSystemApps = !showSystemApps },
                            label = { Text(stringResource(R.string.show_system_apps)) },
                            leadingIcon = if (showSystemApps) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            } else null,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(
                            onClick = {
                                filteredApps.forEach { app ->
                                    val i = allApps.indexOfFirst {
                                        it.packageName == app.packageName }
                                    if (i >= 0 && !allApps[i].isSelected) {
                                        allApps[i] = allApps[i].copy(isSelected = true)
                                    }
                                }
                                scope.launch(Dispatchers.IO) {
                                    applyChange()
                                }
                            },
                            enabled = blockerEnabled,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                stringResource(R.string.action_select_all),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                allApps.indices.forEach { i ->
                                    allApps[i] = allApps[i].copy(isSelected = false)
                                }
                                scope.launch(Dispatchers.IO) {
                                    applyChange()
                                }
                            },
                            enabled = blockerEnabled,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                stringResource(R.string.action_reset),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AppListAnimatedContent(
                        isLoading = isLoading,
                        isEmpty = filteredApps.isEmpty(),
                        hasSearchQuery = searchQuery.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        emptyContent = {
                            HTSREmptyState(
                                icon = ImageVector.vectorResource(R.drawable.ic_tweaks),
                                title = stringResource(R.string.wakelock_blocker_none_configured),
                                description = stringResource(R.string.wakelock_blocker_empty_description),
                                modifier = Modifier.fillMaxSize(),
                            )
                        },
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(filteredApps, key = { it.packageName }) { app ->
                                AppPickerItem(
                                    packageName = app.packageName,
                                    label = app.label,
                                    icon = app.icon,
                                    isSystem = app.isSystem,
                                    checked = app.isSelected,
                                    enabled = blockerEnabled,
                                    onToggle = { nowTargeted ->
                                        val index = allApps.indexOfFirst {
                                            it.packageName == app.packageName }
                                        if (index < 0) return@AppPickerItem
                                        allApps[index] = allApps[index].copy(isSelected = nowTargeted)
                                        scope.launch(Dispatchers.IO) {
                                            applyChange()
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
