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

import android.app.AppOpsManager
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.xiaomi.settings.R
import java.io.File
import java.io.FileOutputStream

// wakelock severity level
enum class WakelockDangerLevel {
    SAFE,
    CAUTION,
    DANGER,
    UNKNOWN
}

data class KernelWakelockInfo(
    val name: String,
    val hitCount: Long = 0L,
    val isBlocked: Boolean = false,
    val level: WakelockDangerLevel = WakelockDangerLevel.UNKNOWN,
    @StringRes val descriptionRes: Int = R.string.kwl_generic_desc,
)

object WakelockBlockerUtils {
    private const val TAG = "WakelockBlockerUtils"

    const val PREF_APP_WAKELOCK_BLOCKER_ENABLE = "wakelock_blocker_app_enable"
    const val PREF_KERNEL_WAKELOCK_BLOCKER_ENABLE = "wakelock_blocker_kernel_enable"
    const val PREF_WAKELOCK_BLOCKER_ENABLE = "wakelock_blocker_enable"

    const val SECURE_KEY_WAKELOCK_BLOCKER_APPS = "wakelock_blocker_apps"
    const val SECURE_KEY_WAKELOCK_BLOCKER_KERNEL = "wakelock_blocker_kernel"

    private const val SYSFS_WAKELOCK_BLOCKER_ENABLED = "/sys/power/wakelock_blocker/enabled"
    private const val SYSFS_WAKELOCK_BLOCKER_LIST = "/sys/power/wakelock_blocker/list"
    private const val SYSFS_WAKELOCK_BLOCKER_TOTAL_HITS = "/sys/power/wakelock_blocker/total_hits"

    // known wakelocks
    val KNOWN_KERNEL_WAKELOCKS: Map<String, Pair<WakelockDangerLevel, Int>> = mapOf(
        // safe
        "batterysecret" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_batterysecret_desc
        ),
        "bms" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_bms_desc
        ),
        "mtk-gauge" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_mtk_gauge_desc
        ),
        "3-0028" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_3_0028_desc
        ),
        "gauge coulomb wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_gauge_coulomb_desc
        ),
        "primary_chg" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_primary_chg_desc
        ),
        "cp_master" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_cp_master_desc
        ),
        "mtk-mst-hvdiv-chg" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_mtk_mst_hvdiv_desc
        ),
        "mtk-slv-hvdiv-chg" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_mtk_slv_hvdiv_desc
        ),
        "PE+ suspend wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe_plus_desc
        ),
        "PE4.0 suspend wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe40_desc
        ),
        "PE4.5 suspend wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe45_desc
        ),
        "PE+20 suspend wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_pe20_desc
        ),
        "bp_notify_lock wakelock" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_bp_notify_desc
        ),
        "vbat0_ws" to Pair(
            WakelockDangerLevel.SAFE,
            R.string.kwl_vbat0_desc
        ),

        // caution
        "10001000.consys" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_10001000_consys_desc
        ),
        "bt_drv_io" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_bt_drv_io_desc
        ),
        "bt_psm" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_bt_psm_desc
        ),
        "wlan" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_wlan_desc
        ),
        "conn_power" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_conn_power_desc
        ),
        "tcpc_attach_wake_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_tcpc_attach_desc
        ),
        "tcpc_detach_wake_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_tcpc_detach_desc
        ),
        "type_c_port0" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_type_c_port0_desc
        ),
        "tcpc" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_tcpc_desc
        ),
        "usb" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_usb_desc
        ),
        "usb_boost" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_usb_boost_desc
        ),
        "0-005a" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_0_005a_desc
        ),
        "accdet_wake_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_accdet_desc
        ),
        "elliptic_wake_source" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_elliptic_desc
        ),
        "1e000000.adspsys" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_adspsys_desc
        ),
        "adsp wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_adsp_wakelock_desc
        ),
        "vow wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_vow_desc
        ),
        "1f800000.mmlsys-config" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_mmlsys_desc
        ),
        "mml_pm_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_mml_pm_lock_desc
        ),
        "mdp_pm_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_mdp_pm_lock_desc
        ),
        "ufs_wake_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_ufs_desc
        ),
        "scp wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_scp_desc
        ),
        "scp reset wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_scp_reset_desc
        ),
        "vcp reset wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_vcp_reset_desc
        ),
        "lpm_lock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_lpm_desc
        ),
        "isp_lock_wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_isp_desc
        ),
        "dip_lock_wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_dip_desc
        ),
        "WPE_lock_wakelock" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_wpe_desc
        ),
        "mt6989_apusys_rv" to Pair(
            WakelockDangerLevel.CAUTION,
            R.string.kwl_apu_rv_desc
        ),

        // danger
        "ccmni_md1" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccmni_md1_desc
        ),
        "ccci_fs" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_fs_desc
        ),
        "ccci_md_log" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ccci_md_log_desc
        ),
        "ttyCMIPC1" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_ttycmipc1_desc
        ),
        "radio-interface" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_radio_interface_desc
        ),
        "pmif_m wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pmif_m_desc
        ),
        "goodix_ts.0" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_goodix_ts_desc
        ),
        "odm:xiaomi_fingerprint" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_xiaomi_fp_desc
        ),
        "pwrkey wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pwrkey_desc
        ),
        "kpd wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_kpd_desc
        ),
        "alarmtimer.0.auto" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_alarmtimer_desc
        ),
        "mt6685-rtc" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_mt6685_rtc_desc
        ),
        "mt6685-rtc suspend wakelock" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_mt6685_rtc_suspend_desc
        ),
        "SuspendControl.TotalSuspendDelay" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_suspend_control_total_desc
        ),
        "SuspendControl.NormalSuspendDelay" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_suspend_control_normal_desc
        ),
        "SuspendControl.FailedSuspendDelay" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_suspend_control_failed_desc
        ),
        "userspace-abort" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_userspace_abort_desc
        ),
        "PowerManagerService.WakeLocks" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pms_wakelocks_desc
        ),
        "PowerManagerService.Display" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pms_display_desc
        ),
        "PowerManagerService.Broadcasts" to Pair(
            WakelockDangerLevel.DANGER,
            R.string.kwl_pms_broadcasts_desc
        )
    )

    fun isSupported(): Boolean {
        return File(SYSFS_WAKELOCK_BLOCKER_ENABLED).exists() || File(SYSFS_WAKELOCK_BLOCKER_LIST).exists()
    }

    fun isAppBlockerEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PREF_APP_WAKELOCK_BLOCKER_ENABLE, prefs.getBoolean(PREF_WAKELOCK_BLOCKER_ENABLE, false))
    }

    fun setAppBlockerEnabled(context: Context, enabled: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .putBoolean(PREF_APP_WAKELOCK_BLOCKER_ENABLE, enabled)
            .putBoolean(PREF_WAKELOCK_BLOCKER_ENABLE, enabled)
            .apply()
        syncApps(context, enabled)
    }

    fun isKernelBlockerEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PREF_KERNEL_WAKELOCK_BLOCKER_ENABLE, false)
    }

    fun setKernelBlockerEnabled(context: Context, enabled: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(PREF_KERNEL_WAKELOCK_BLOCKER_ENABLE, enabled).apply()
        syncKernel(context, enabled)
    }

    fun isEnabled(context: Context): Boolean {
        return isAppBlockerEnabled(context) || isKernelBlockerEnabled(context)
    }

    // app wakelocks

    fun getBlockedApps(context: Context): Set<String> {
        val raw = Settings.Secure.getString(context.contentResolver, SECURE_KEY_WAKELOCK_BLOCKER_APPS)
        if (raw.isNullOrEmpty()) return emptySet()
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun setBlockedApps(context: Context, apps: Set<String>) {
        val raw = apps.joinToString(",")
        Settings.Secure.putString(context.contentResolver, SECURE_KEY_WAKELOCK_BLOCKER_APPS, raw)
        syncApps(context)
    }

    // kernel wakelocks

    fun getBlockedKernelWakelocks(context: Context): Set<String> {
        val raw = Settings.Secure.getString(context.contentResolver, SECURE_KEY_WAKELOCK_BLOCKER_KERNEL)
        if (raw.isNullOrEmpty()) return emptySet()
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun setBlockedKernelWakelocks(context: Context, items: Set<String>) {
        val raw = items.joinToString(",")
        Settings.Secure.putString(context.contentResolver, SECURE_KEY_WAKELOCK_BLOCKER_KERNEL, raw)
        syncKernel(context)
    }

    fun toggleKernelWakelock(context: Context, name: String, block: Boolean) {
        val current = getBlockedKernelWakelocks(context).toMutableSet()
        if (block) {
            current.add(name)
        } else {
            current.remove(name)
        }
        val raw = current.joinToString(",")
        Settings.Secure.putString(context.contentResolver, SECURE_KEY_WAKELOCK_BLOCKER_KERNEL, raw)

        if (isKernelBlockerEnabled(context)) {
            val op = if (block) "+$name\n" else "-$name\n"
            writeNode(SYSFS_WAKELOCK_BLOCKER_LIST, op)
        }
    }

    fun applySafePreset(context: Context) {
        val safeItems = KNOWN_KERNEL_WAKELOCKS.filter { it.value.first == WakelockDangerLevel.SAFE }.keys
        setBlockedKernelWakelocks(context, safeItems)
    }

    fun clearBlockedKernelWakelocks(context: Context) {
        setBlockedKernelWakelocks(context, emptySet())
    }

    fun resetHitCounts() {
        writeNode(SYSFS_WAKELOCK_BLOCKER_LIST, "reset")
        writeNode(SYSFS_WAKELOCK_BLOCKER_TOTAL_HITS, "0")
    }

    fun getTotalHits(): Long {
        return try {
            val file = File(SYSFS_WAKELOCK_BLOCKER_TOTAL_HITS)
            if (file.exists() && file.canRead()) {
                file.readText().trim().toLongOrNull() ?: 0L
            } else 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun getKernelWakelockStats(): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        try {
            val file = File(SYSFS_WAKELOCK_BLOCKER_LIST)
            if (file.exists() && file.canRead()) {
                file.readLines().forEach { line ->
                    val parts = line.trim().split(Regex("\\s+"))
                    if (parts.size >= 2) {
                        val name = parts[0]
                        val hits = parts[1].toLongOrNull() ?: 0L
                        map[name] = hits
                    } else if (parts.size == 1 && parts[0].isNotEmpty()) {
                        map[parts[0]] = 0L
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading $SYSFS_WAKELOCK_BLOCKER_LIST: ${e.message}")
        }
        return map
    }

    fun isAppPackage(name: String, context: Context?): Boolean {
        if (KNOWN_KERNEL_WAKELOCKS.containsKey(name)) return false
        if (name.startsWith("PowerManagerService.") || name.startsWith("SuspendControl.")) return false
        if (name.contains(":") || name.contains("@")) return false
        if (name.matches(Regex("^[0-9].*")) || name.contains(".0.")) return false

        if (context != null) {
            val isInstalled = runCatching {
                context.packageManager.getPackageInfo(name, 0) != null
            }.getOrDefault(false)
            if (isInstalled) return true
        }

        // standart package format
        val packageRegex = Regex("^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+$")
        return packageRegex.matches(name)
    }

    fun getDiscoveredKernelWakelocks(context: Context): List<KernelWakelockInfo> {
        val stats = getKernelWakelockStats()
        val blockedSet = getBlockedKernelWakelocks(context)
        val discovered = mutableMapOf<String, Long>()

        KNOWN_KERNEL_WAKELOCKS.keys.forEach { name ->
            discovered[name] = stats[name] ?: 0L
        }

        try {
            val wakeupDir = File("/sys/class/wakeup")
            if (wakeupDir.exists() && wakeupDir.isDirectory) {
                wakeupDir.listFiles()?.forEach { dir ->
                    try {
                        val nameFile = File(dir, "name")
                        if (nameFile.exists() && nameFile.canRead()) {
                            val name = nameFile.readText().trim()
                            if (name.isNotEmpty() && !discovered.containsKey(name) && !isAppPackage(name, context)) {
                                discovered[name] = stats[name] ?: 0L
                            }
                        }
                    } catch (ignored: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot read /sys/class/wakeup: ${e.message}")
        }

        stats.forEach { (name, hits) ->
            if (!discovered.containsKey(name) && !isAppPackage(name, context)) {
                discovered[name] = hits
            }
        }

        blockedSet.forEach { name ->
            if (!discovered.containsKey(name) && !isAppPackage(name, context)) {
                discovered[name] = stats[name] ?: 0L
            }
        }

        return discovered
            .filter { (name, _) -> !isAppPackage(name, context) }
            .map { (name, hits) ->
                val known = KNOWN_KERNEL_WAKELOCKS[name]
                KernelWakelockInfo(
                    name = name,
                    hitCount = hits,
                    isBlocked = blockedSet.contains(name),
                    level = known?.first ?: WakelockDangerLevel.UNKNOWN,
                    descriptionRes = known?.second ?: R.string.kwl_generic_desc,
                )
            }.sortedWith(
                compareByDescending<KernelWakelockInfo> { it.isBlocked }
                    .thenByDescending { it.hitCount }
                    .thenBy { it.level.ordinal }
                    .thenBy { it.name }
            )
    }

    // some syncs

    fun syncKernel(context: Context, enabledOverride: Boolean? = null) {
        val enabled = enabledOverride ?: isKernelBlockerEnabled(context)
        val blockedKernel = getBlockedKernelWakelocks(context)

        Log.d(TAG, "Syncing kernel wakelocks: enabled=$enabled, count=${blockedKernel.size}")

        if (enabled) {
            writeNode(SYSFS_WAKELOCK_BLOCKER_ENABLED, "1")
            writeNode(SYSFS_WAKELOCK_BLOCKER_LIST, "clear")

            val listFile = File(SYSFS_WAKELOCK_BLOCKER_LIST)
            if (listFile.exists() && blockedKernel.isNotEmpty()) {
                try {
                    FileOutputStream(listFile).use { fos ->
                        for (kwl in blockedKernel) {
                            fos.write("+$kwl\n".toByteArray())
                        }
                        fos.flush()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error streaming to $SYSFS_WAKELOCK_BLOCKER_LIST: ${e.message}")
                }
            }
        } else {
            writeNode(SYSFS_WAKELOCK_BLOCKER_ENABLED, "0")
            writeNode(SYSFS_WAKELOCK_BLOCKER_LIST, "clear")
        }
    }

    fun syncApps(context: Context, enabledOverride: Boolean? = null) {
        val enabled = enabledOverride ?: isAppBlockerEnabled(context)
        val blockedApps = getBlockedApps(context)
        Log.d(TAG, "Syncing app wakelocks: enabled=$enabled, count=${blockedApps.size}")
        applyAppOps(context, enabled, blockedApps)
    }

    fun sync(context: Context) {
        syncKernel(context)
        syncApps(context)
    }

    private fun applyAppOps(context: Context, enabled: Boolean, blockedApps: Set<String>) {
        try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return
            val pm = context.packageManager

            for (pkg in blockedApps) {
                try {
                    val uid = pm.getPackageUid(pkg, 0)
                    val mode = if (enabled) AppOpsManager.MODE_IGNORED else AppOpsManager.MODE_ALLOWED
                    appOps.setMode(AppOpsManager.OP_WAKE_LOCK, uid, pkg, mode)
                } catch (e: Exception) {
                    // Package might be uninstalled or hidden
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to apply AppOps: ${e.message}")
        }
    }

    private fun writeNode(path: String, value: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                FileOutputStream(file).use { fos ->
                    fos.write(value.toByteArray())
                    fos.flush()
                }
                Log.d(TAG, "Wrote '$value' to $path")
            } else {
                Log.w(TAG, "Node does not exist: $path")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to $path: ${e.message}")
        }
    }
}
