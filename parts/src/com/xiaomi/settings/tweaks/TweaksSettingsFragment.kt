/*
 * ==============================================================
 *    __  __  _____   __    ___    ___   _____   _
 *   |  \/  ||_   _| / /_  ( _ )  / _ \ |___  | / \
 *   | |\/| |  | |  | '_ \ / _ \ | (_) |   / / / _ \
 *   | |  | |  | |  | (_) | (_) | \__, |  / / / ___ \
 *   |_|  |_|  |_|   \___/ \___/    /_/  /_/ /_/   \_\
 *
 *  SPDX-FileCopyrightText: The LineageOS Project
 *  SPDX-FileCopyrightText: 2026 MT6897A
 *  SPDX-License-Identifier: Apache-2.0
 * ==============================================================
 */

package com.xiaomi.settings.tweaks

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
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
        setupZram()
        setupWorkqueue()
        setupPrintk()
        setupNtsync()
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

    private fun setupZram() {
        val recompPref = findPreference<ListPreference>(KernelTweaksUtils.KEY_ZRAM_RECOMP_ALGORITHM)
        val recompressPref = findPreference<Preference>("zram_recompress")

        if (recompPref != null) {
            val available = KernelTweaksUtils.getAvailableZramAlgorithms()
            val entries = available.map { it.uppercase() }.toTypedArray()
            val entryValues = available.toTypedArray()

            recompPref.entries = entries
            recompPref.entryValues = entryValues

            val current = KernelTweaksUtils.getCurrentZramRecompAlgorithm()
            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val savedAlgo = prefs.getString(KernelTweaksUtils.KEY_ZRAM_RECOMP_ALGORITHM, null)

            val displayAlgo = if (!savedAlgo.isNullOrBlank()) savedAlgo else current
            recompPref.value = displayAlgo

            if (!savedAlgo.isNullOrBlank() && !savedAlgo.equals(current, ignoreCase = true)) {
                recompPref.summary = getString(
                    R.string.zram_recomp_algorithm_summary,
                    getString(R.string.zram_recomp_algorithm_reboot_pending, savedAlgo.uppercase())
                )
            } else {
                recompPref.summary = getString(R.string.zram_recomp_algorithm_summary, current.uppercase())
            }

            recompPref.setOnPreferenceChangeListener { _, newValue ->
                val algo = newValue.toString()
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit()
                    .putString(KernelTweaksUtils.KEY_ZRAM_RECOMP_ALGORITHM, algo)
                    .apply()

                if (KernelTweaksUtils.setZramRecompAlgorithm(algo)) {
                    recompPref.value = algo
                    recompPref.summary = getString(R.string.zram_recomp_algorithm_summary, algo.uppercase())
                } else {
                    recompPref.value = algo
                    recompPref.summary = getString(
                        R.string.zram_recomp_algorithm_summary,
                        getString(R.string.zram_recomp_algorithm_reboot_pending, algo.uppercase())
                    )
                    showRebootDialog(algo.uppercase())
                }
                true
            }
        }

        recompressPref?.setOnPreferenceClickListener {
            val success = KernelTweaksUtils.triggerZramRecompression()
            val messageRes = if (success) {
                R.string.zram_recompress_toast_success
            } else {
                R.string.zram_recompress_toast_failed
            }
            Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun showRebootDialog(algo: String) {
        val context = context ?: return
        AlertDialog.Builder(context)
            .setTitle(R.string.zram_reboot_dialog_title)
            .setMessage(getString(R.string.zram_reboot_dialog_message, algo))
            .setPositiveButton(R.string.zram_reboot_dialog_reboot_now) { _, _ ->
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                pm?.reboot(null)
            }
            .setNegativeButton(R.string.zram_reboot_dialog_later, null)
            .show()
    }

    private fun setupWorkqueue() {
        val wqPref = findPreference<SwitchPreferenceCompat>(KernelTweaksUtils.KEY_WQ_POWER_EFFICIENT) ?: return
        wqPref.isChecked = KernelTweaksUtils.isWorkqueuePowerEfficient()
        wqPref.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            KernelTweaksUtils.setWorkqueuePowerEfficient(enabled)
            true
        }
    }

    private fun setupPrintk() {
        val printkPref = findPreference<ListPreference>(KernelTweaksUtils.KEY_KERNEL_PRINTK_LEVEL) ?: return
        val entries = arrayOf(
            getString(R.string.kernel_printk_level_quiet),
            getString(R.string.kernel_printk_level_normal),
            getString(R.string.kernel_printk_level_verbose)
        )
        val entryValues = arrayOf("0", "4", "7")

        printkPref.entries = entries
        printkPref.entryValues = entryValues

        val current = KernelTweaksUtils.getCurrentPrintkLevel()
        printkPref.value = current
        printkPref.summary = formatPrintkSummary(current)

        printkPref.setOnPreferenceChangeListener { _, newValue ->
            val level = newValue.toString()
            if (KernelTweaksUtils.setPrintkLevel(level)) {
                printkPref.value = level
                printkPref.summary = formatPrintkSummary(level)
                true
            } else {
                false
            }
        }
    }

    private fun formatPrintkSummary(level: String): String {
        val label = when (level) {
            "0" -> getString(R.string.kernel_printk_level_quiet)
            "7" -> getString(R.string.kernel_printk_level_verbose)
            else -> getString(R.string.kernel_printk_level_normal)
        }
        return getString(R.string.kernel_printk_level_summary, label)
    }

    private fun setupNtsync() {
        val ntsyncPref = findPreference<Preference>("ntsync_driver_status") ?: return
        val isSupported = KernelTweaksUtils.isNtsyncSupported()
        ntsyncPref.summary = if (isSupported) {
            getString(R.string.ntsync_driver_active)
        } else {
            getString(R.string.ntsync_driver_not_detected)
        }
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

        val recompPref = findPreference<ListPreference>(KernelTweaksUtils.KEY_ZRAM_RECOMP_ALGORITHM)
        if (recompPref != null) {
            val current = KernelTweaksUtils.getCurrentZramRecompAlgorithm()
            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val savedAlgo = prefs.getString(KernelTweaksUtils.KEY_ZRAM_RECOMP_ALGORITHM, null)

            val displayAlgo = if (!savedAlgo.isNullOrBlank()) savedAlgo else current
            recompPref.value = displayAlgo

            if (!savedAlgo.isNullOrBlank() && !savedAlgo.equals(current, ignoreCase = true)) {
                recompPref.summary = getString(
                    R.string.zram_recomp_algorithm_summary,
                    getString(R.string.zram_recomp_algorithm_reboot_pending, savedAlgo.uppercase())
                )
            } else {
                recompPref.summary = getString(R.string.zram_recomp_algorithm_summary, current.uppercase())
            }
        }

        val wqPref = findPreference<SwitchPreferenceCompat>(KernelTweaksUtils.KEY_WQ_POWER_EFFICIENT)
        if (wqPref != null) {
            wqPref.isChecked = KernelTweaksUtils.isWorkqueuePowerEfficient()
        }

        val printkPref = findPreference<ListPreference>(KernelTweaksUtils.KEY_KERNEL_PRINTK_LEVEL)
        if (printkPref != null) {
            val current = KernelTweaksUtils.getCurrentPrintkLevel()
            printkPref.value = current
            printkPref.summary = formatPrintkSummary(current)
        }

        val ntsyncPref = findPreference<Preference>("ntsync_driver_status")
        if (ntsyncPref != null) {
            val isSupported = KernelTweaksUtils.isNtsyncSupported()
            ntsyncPref.summary = if (isSupported) {
                getString(R.string.ntsync_driver_active)
            } else {
                getString(R.string.ntsync_driver_not_detected)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.tweaks_button_title)
        updateSummaries()
    }
}