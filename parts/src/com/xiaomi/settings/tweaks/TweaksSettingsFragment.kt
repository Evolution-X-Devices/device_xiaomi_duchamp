/*
 * ==============================================================
 *    __  __  _____   __    ___    ___   _____   _
 *   |  \/  ||_   _| / /_  ( _ )  / _ \ |___  | / \
 *   | |\/| |  | |  | '_ \ / _ \ | (_) |   / / / _ \
 *   | |  | |  | |  | (_) | (_) | \__, |  / / / ___ \
 *   |_|  |_|  |_|   \___/ \___/    /_/  /_/ /_/   \_\
 *
 *  SPDX-FileCopyRightText: The LineageOS Project
 *  SPDX-FileCopyRightText: 2026 MT6897A
 *  SPDX-License-Identifier: Apache-2.0
 * ==============================================================
 */

package com.xiaomi.settings.tweaks

import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.xiaomi.settings.PartsActivity
import com.xiaomi.settings.R

class TweaksSettingsFragment : SettingsBasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.tweaks_settings, rootKey)

        val perAppPref = findPreference<Preference>(WakelockBlockerUtils.SECURE_KEY_WAKELOCK_BLOCKER_APPS)
        val kernelPref = findPreference<Preference>(WakelockBlockerUtils.SECURE_KEY_WAKELOCK_BLOCKER_KERNEL)

        perAppPref?.setOnPreferenceClickListener {
            val intent = Intent(requireContext(), PartsActivity::class.java).apply {
                putExtra(":settings:show_fragment", WakelockBlockerAppListFragment::class.java.name)
                putExtra(":settings:show_fragment_title", getString(R.string.wakelock_blocker_app_list_title))
            }
            startActivity(intent)
            true
        }

        kernelPref?.setOnPreferenceClickListener {
            val intent = Intent(requireContext(), PartsActivity::class.java).apply {
                putExtra(":settings:show_fragment", KernelWakelockBlockerFragment::class.java.name)
                putExtra(":settings:show_fragment_title", getString(R.string.wakelock_blocker_kernel_title))
            }
            startActivity(intent)
            true
        }

        setupTcpCongestion()
        updateSummaries()
    }

    private fun setupTcpCongestion() {
        val tcpPref = findPreference<ListPreference>(KernelTweaksUtils.KEY_TCP_CONGESTION_CONTROL) ?: return
        val available = KernelTweaksUtils.getAvailableTcpCongestion()
        val entries = available.map { algo ->
            when (algo) {
                "bbr3" -> getString(R.string.tcp_congestion_bbr3)
                "cubic" -> getString(R.string.tcp_congestion_cubic)
                "reno" -> getString(R.string.tcp_congestion_reno)
                else -> algo.replaceFirstChar { it.uppercase() }
            }
        }.toTypedArray()
        val entryValues = available.toTypedArray()

        tcpPref.entries = entries
        tcpPref.entryValues = entryValues

        val current = KernelTweaksUtils.getCurrentTcpCongestion()
        tcpPref.value = current
        tcpPref.summary = formatTcpSummary(current)

        tcpPref.setOnPreferenceChangeListener { _, newValue ->
            val algo = newValue.toString()
            KernelTweaksUtils.setTcpCongestion(algo)
            tcpPref.summary = formatTcpSummary(algo)
            true
        }
    }

    private fun formatTcpSummary(algo: String): String {
        val label = when (algo) {
            "bbr3" -> getString(R.string.tcp_congestion_bbr3)
            "cubic" -> getString(R.string.tcp_congestion_cubic)
            "reno" -> getString(R.string.tcp_congestion_reno)
            else -> algo.replaceFirstChar { it.uppercase() }
        }
        return getString(R.string.tcp_congestion_summary, label)
    }

    private fun updateSummaries() {
        val perAppPref = findPreference<Preference>(WakelockBlockerUtils.SECURE_KEY_WAKELOCK_BLOCKER_APPS)
        val isAppEnabled = WakelockBlockerUtils.isAppBlockerEnabled(requireContext())
        val appCount = WakelockBlockerUtils.getBlockedApps(requireContext()).size
        perAppPref?.summary = if (!isAppEnabled) {
            getString(R.string.wakelock_blocker_per_app_disabled_summary)
        } else if (appCount == 0) {
            getString(R.string.wakelock_blocker_none_configured)
        } else {
            getString(R.string.wakelock_blocker_count, appCount)
        }

        val kernelPref = findPreference<Preference>(WakelockBlockerUtils.SECURE_KEY_WAKELOCK_BLOCKER_KERNEL)
        val isKernelEnabled = WakelockBlockerUtils.isKernelBlockerEnabled(requireContext())
        val kernelCount = WakelockBlockerUtils.getBlockedKernelWakelocks(requireContext()).size
        val totalHits = WakelockBlockerUtils.getTotalHits()
        kernelPref?.summary = if (!isKernelEnabled) {
            getString(R.string.wakelock_blocker_kernel_disabled_summary)
        } else if (kernelCount == 0) {
            getString(R.string.wakelock_blocker_kernel_none_configured)
        } else if (totalHits > 0) {
            getString(R.string.wakelock_blocker_kernel_count_with_hits, kernelCount, totalHits)
        } else {
            getString(R.string.wakelock_blocker_kernel_count, kernelCount)
        }

        val tcpPref = findPreference<ListPreference>(KernelTweaksUtils.KEY_TCP_CONGESTION_CONTROL)
        if (tcpPref != null) {
            val current = KernelTweaksUtils.getCurrentTcpCongestion()
            tcpPref.value = current
            tcpPref.summary = formatTcpSummary(current)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.tweaks_button_title)
        updateSummaries()
    }
}