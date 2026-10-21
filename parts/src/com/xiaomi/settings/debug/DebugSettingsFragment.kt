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

package com.xiaomi.settings.debug

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.xiaomi.settings.R
import com.xiaomi.settings.battery.BatteryUtils
import com.xiaomi.settings.tweaks.KernelTweaksUtils
import com.xiaomi.settings.tweaks.WakelockBlockerUtils
import java.io.File

class DebugSettingsFragment : SettingsBasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.debug_settings, rootKey)

        findPreference<Preference>("debug_action_refresh")?.setOnPreferenceClickListener {
            refreshDebugData()
            Toast.makeText(context, R.string.parts_debug_refresh_toast, Toast.LENGTH_SHORT).show()
            true
        }

        findPreference<Preference>("debug_action_lock")?.setOnPreferenceClickListener {
            showLockConfirmDialog()
            true
        }

        setupClickDialogs()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.parts_debug_button_title)
        refreshDebugData()
    }

    private fun refreshDebugData() {
        val prefs = preferenceManager.sharedPreferences
        val cr = requireContext().contentResolver

        // Thermal
        val sconfigVal = readNodeSafely("/sys/class/thermal/thermal_message/sconfig")
        val thermalProf = prefs?.getString("thermal_profile", "2") ?: "2"
        val thermalAuto = prefs?.getBoolean("thermal_auto_mode", false) ?: false
        updatePref(
            "debug_thermal_sconfig",
            "Path: /sys/class/thermal/thermal_message/sconfig\nValue: $sconfigVal"
        )
        updatePref(
            "debug_thermal_prefs",
            "Saved Profile: $thermalProf • Auto Mode Screen-Off: $thermalAuto"
        )

        // Core Control
        val coreStates = (0..7).joinToString(" ") { i ->
            val path = "/sys/devices/system/cpu/cpu$i/online"
            val state = if (i == 0 && !File(path).exists()) "1 (fixed)" else readNodeSafely(path)
            "cpu$i:$state"
        }
        val coreSwitch = prefs?.getBoolean("core_control_switch", false) ?: false
        val corePrefs = (1..8).joinToString(", ") { "c$it:${prefs?.getBoolean("core$it", true) ?: true}" }
        updatePref(
            "debug_core_online",
            "Hardware Online: $coreStates"
        )
        updatePref(
            "debug_core_prefs",
            "Master Enabled: $coreSwitch\nConfig: $corePrefs"
        )

        // HTSR
        val htsrNode = readNodeSafely("/sys/devices/virtual/touch/touch_dev/bump_sample_rate")
        val htsrGlobal = prefs?.getBoolean("htsr_global_enable", false) ?: false
        val htsrPerApp = prefs?.getBoolean("htsr_enable", false) ?: false
        val htsrAppsRaw = Settings.Secure.getString(cr, "htsr_apps") ?: ""
        val htsrCount = if (htsrAppsRaw.isBlank()) 0 else htsrAppsRaw.split(",").filter { it.isNotBlank() }.size
        updatePref(
            "debug_htsr_node",
            "Path: /sys/devices/virtual/touch/touch_dev/bump_sample_rate\nValue: $htsrNode"
        )
        updatePref(
            "debug_htsr_prefs",
            "Global: $htsrGlobal • Per-App: $htsrPerApp • Whitelist: $htsrCount apps"
        )

        // Battery
        val chargeCurrent = readNodeSafely(BatteryUtils.NODE_CONSTANT_CHARGE_CURRENT)
        val bypassCmd = readNodeSafely(BatteryUtils.NODE_BYPASS_CMD)
        val batCap = readNodeSafely("/sys/class/power_supply/battery/capacity")
        val batVolt = readNodeSafely("/sys/class/power_supply/battery/voltage_now")
        val batStatus = readNodeSafely("/sys/class/power_supply/battery/status")
        val batPlugged = BatteryUtils.isChargerConnected(requireContext())
        val prefSpeed = prefs?.getString(BatteryUtils.PREF_CHARGING_SPEED, BatteryUtils.SPEED_67W)
        val prefBypass = prefs?.getBoolean(BatteryUtils.PREF_BYPASS_CHARGING, false) ?: false
        val prefSmart = prefs?.getString(BatteryUtils.PREF_SMART_CHARGE_LIMIT, "0")
        val prefSmartActive = prefs?.getBoolean(BatteryUtils.PREF_SMART_BYPASS_ACTIVE, false) ?: false

        updatePref(
            "debug_battery_current",
            "constant_charge_current: $chargeCurrent µA\nConfigured Target: $prefSpeed µA"
        )
        updatePref(
            "debug_battery_bypass",
            "mtk_battery_cmd/current_cmd: $bypassCmd\nBypass Active (in-memory): ${BatteryUtils.isBypassActive()}"
        )
        updatePref(
            "debug_battery_status",
            "Level: $batCap% • Voltage: $batVolt µV • Status: $batStatus • Charger Connected: $batPlugged"
        )
        updatePref(
            "debug_battery_prefs",
            "Manual Bypass: $prefBypass • Smart Limit: $prefSmart% • Smart Active: $prefSmartActive"
        )

        // Wakelock Blocker
        val wlEnabled = readNodeSafely("/sys/power/wakelock_blocker/enabled")
        val wlHits = WakelockBlockerUtils.getTotalHits()
        val wlStats = WakelockBlockerUtils.getKernelWakelockStats()
        val blockedKernel = WakelockBlockerUtils.getBlockedKernelWakelocks(requireContext())
        val blockedApps = WakelockBlockerUtils.getBlockedApps(requireContext())
        val prefKernelWl = prefs?.getBoolean(WakelockBlockerUtils.PREF_KERNEL_WAKELOCK_BLOCKER_ENABLE, false) ?: false
        val prefAppWl = prefs?.getBoolean(WakelockBlockerUtils.PREF_APP_WAKELOCK_BLOCKER_ENABLE, false) ?: false

        updatePref(
            "debug_wakelock_enabled",
            "Driver Node: $wlEnabled • Total Wakeups Blocked: $wlHits"
        )
        updatePref(
            "debug_wakelock_list",
            "Active in Driver: ${wlStats.size} items • Blocked Kernel Setting: ${blockedKernel.size} items"
        )
        updatePref(
            "debug_wakelock_prefs",
            "Kernel Blocker Enabled: $prefKernelWl\nApp Blocker Enabled: $prefAppWl (${blockedApps.size} apps)"
        )

        // Kernel Tweaks
        val tcpCur = KernelTweaksUtils.getCurrentTcpCongestion()
        val tcpAvail = KernelTweaksUtils.getAvailableTcpCongestion().joinToString(", ")
        val zramComp = readNodeSafely("/sys/block/zram0/comp_algorithm")
        val zramRecomp = readNodeSafely("/sys/block/zram0/recomp_algorithm")
        val wqVal = readNodeSafely("/sys/module/workqueue/parameters/power_efficient")
        val printkVal = readNodeSafely("/proc/sys/kernel/printk")
        val ntsyncExists = File("/dev/ntsync").exists()

        updatePref(
            "debug_tweak_tcp",
            "Active: $tcpCur\nAvailable: $tcpAvail"
        )
        updatePref(
            "debug_tweak_zram",
            "comp_algorithm: $zramComp\nrecomp_algorithm: $zramRecomp"
        )
        updatePref(
            "debug_tweak_wq",
            "power_efficient: $wqVal (${if (wqVal == "Y") "Enabled" else "Disabled"})"
        )
        updatePref(
            "debug_tweak_printk",
            "printk: $printkVal"
        )
        updatePref(
            "debug_tweak_ntsync",
            "/dev/ntsync: ${if (ntsyncExists) "Available (Active)" else "Not detected"}"
        )
    }

    private fun updatePref(key: String, summaryText: String) {
        findPreference<Preference>(key)?.summary = summaryText
    }

    private fun setupClickDialogs() {
        val keys = listOf(
            "debug_thermal_sconfig", "debug_thermal_prefs",
            "debug_core_online", "debug_core_prefs",
            "debug_htsr_node", "debug_htsr_prefs",
            "debug_battery_current", "debug_battery_bypass", "debug_battery_status", "debug_battery_prefs",
            "debug_wakelock_enabled", "debug_wakelock_list", "debug_wakelock_prefs",
            "debug_tweak_tcp", "debug_tweak_zram", "debug_tweak_wq", "debug_tweak_printk", "debug_tweak_ntsync"
        )

        keys.forEach { key ->
            findPreference<Preference>(key)?.setOnPreferenceClickListener { pref ->
                showDetailsDialog(pref.title?.toString() ?: key, pref.summary?.toString() ?: "")
                true
            }
        }
    }

    private fun showDetailsDialog(title: String, details: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(details)
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.parts_debug_copy_action) { _, _ ->
                val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                cm?.setPrimaryClip(ClipData.newPlainText(title, details))
                Toast.makeText(context, R.string.parts_debug_copied_toast, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showLockConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.parts_debug_lock_title)
            .setMessage(R.string.parts_debug_lock_confirm_message)
            .setPositiveButton(R.string.parts_debug_lock_action) { _, _ ->
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit()
                    .putBoolean(PREF_DEBUG_UNLOCKED, false)
                    .apply()
                Toast.makeText(context, R.string.parts_debug_locked_toast, Toast.LENGTH_SHORT).show()
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun readNodeSafely(path: String): String {
        return try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                file.readText().trim()
            } else if (file.exists()) {
                "[Permission Denied]"
            } else {
                "[Node Missing]"
            }
        } catch (e: Exception) {
            "[Error: ${e.message}]"
        }
    }

    companion object {
        const val PREF_DEBUG_UNLOCKED = "parts_debug_unlocked"
    }
}
