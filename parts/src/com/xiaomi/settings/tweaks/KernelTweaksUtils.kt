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

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object KernelTweaksUtils {

    private const val TAG = "KernelTweaksUtils"
    private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)

    const val KEY_TCP_CONGESTION_CONTROL = "tcp_congestion_control"
    private const val NODE_TCP_CONGESTION_CONTROL = "/proc/sys/net/ipv4/tcp_congestion_control"
    private const val NODE_TCP_AVAILABLE_CONGESTION = "/proc/sys/net/ipv4/tcp_available_congestion_control"

    // ZRAM
    const val KEY_ZRAM_RECOMP_ALGORITHM = "zram_recomp_algorithm"
    private const val NODE_ZRAM_COMP_ALGORITHM = "/sys/block/zram0/comp_algorithm"
    private const val NODE_ZRAM_RECOMP_ALGORITHM = "/sys/block/zram0/recomp_algorithm"
    private const val NODE_ZRAM_RECOMPRESS = "/sys/block/zram0/recompress"

    // Power-Efficient Workqueues
    const val KEY_WQ_POWER_EFFICIENT = "wq_power_efficient"
    private const val NODE_WQ_POWER_EFFICIENT = "/sys/module/workqueue/parameters/power_efficient"

    // Kernel Log Level (Printk)
    const val KEY_KERNEL_PRINTK_LEVEL = "kernel_printk_level"
    private const val NODE_KERNEL_PRINTK = "/proc/sys/kernel/printk"

    // NTSync
    private const val NODE_NTSYNC = "/dev/ntsync"

    fun readNode(path: String): String? {
        val file = File(path)
        if (!file.exists() || !file.canRead()) {
            return null
        }
        return try {
            file.readText().trim()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read node $path", e)
            null
        }
    }

    fun writeNode(path: String, value: String): Boolean {
        return try {
            FileOutputStream(path).use { fos ->
                fos.write((value.trim() + "\n").toByteArray())
                fos.flush()
            }
            if (DEBUG) Log.d(TAG, "Wrote '$value' to $path")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write '$value' to $path", e)
            false
        }
    }

    fun getAvailableTcpCongestion(): List<String> {
        val raw = readNode(NODE_TCP_AVAILABLE_CONGESTION)
        return if (!raw.isNullOrBlank()) {
            raw.split("\\s+".toRegex()).filter { it.isNotBlank() }
        } else {
            listOf("bbr3", "cubic", "reno")
        }
    }

    fun getCurrentTcpCongestion(): String {
        return readNode(NODE_TCP_CONGESTION_CONTROL) ?: "bbr3"
    }

    fun setTcpCongestion(algo: String): Boolean {
        return writeNode(NODE_TCP_CONGESTION_CONTROL, algo)
    }

    fun syncTcpCongestion(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val savedAlgo = prefs.getString(KEY_TCP_CONGESTION_CONTROL, null)
        if (!savedAlgo.isNullOrBlank()) {
            setTcpCongestion(savedAlgo)
        }
    }

    fun isZramSupported(): Boolean {
        return File(NODE_ZRAM_COMP_ALGORITHM).exists()
    }

    fun isZramRecompSupported(): Boolean {
        return File(NODE_ZRAM_RECOMP_ALGORITHM).exists()
    }

    fun getAvailableZramAlgorithms(): List<String> {
        val raw = readNode(NODE_ZRAM_COMP_ALGORITHM)
        if (!raw.isNullOrBlank()) {
            val list = raw.replace("[", "").replace("]", "")
                .split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .distinct()
            if (list.isNotEmpty()) return list
        }
        return listOf("zstd", "lz4", "lzo-rle", "lzo")
    }

    fun getCurrentZramRecompAlgorithm(): String {
        val raw = readNode(NODE_ZRAM_RECOMP_ALGORITHM)
        if (!raw.isNullOrBlank()) {
            val match = Regex("\\[([^\\]]+)\\]").find(raw)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return "zstd"
    }

    fun setZramRecompAlgorithm(algo: String): Boolean {
        return writeNode(NODE_ZRAM_RECOMP_ALGORITHM, "algo=$algo priority=1")
    }

    fun triggerZramRecompression(): Boolean {
        return writeNode(NODE_ZRAM_RECOMPRESS, "type=all")
    }

    fun syncZram(context: Context) {
        if (!isZramRecompSupported()) return
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val savedAlgo = prefs.getString(KEY_ZRAM_RECOMP_ALGORITHM, null)
        if (!savedAlgo.isNullOrBlank()) {
            setZramRecompAlgorithm(savedAlgo)
        }
    }

    fun isWorkqueuePowerEfficientSupported(): Boolean {
        return File(NODE_WQ_POWER_EFFICIENT).exists()
    }

    fun isWorkqueuePowerEfficient(): Boolean {
        val raw = readNode(NODE_WQ_POWER_EFFICIENT)
        return raw.equals("Y", ignoreCase = true) || raw == "1"
    }

    fun setWorkqueuePowerEfficient(enabled: Boolean): Boolean {
        return writeNode(NODE_WQ_POWER_EFFICIENT, if (enabled) "Y" else "N")
    }

    fun syncWorkqueue(context: Context) {
        if (!isWorkqueuePowerEfficientSupported()) return
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.contains(KEY_WQ_POWER_EFFICIENT)) {
            val enabled = prefs.getBoolean(KEY_WQ_POWER_EFFICIENT, true)
            setWorkqueuePowerEfficient(enabled)
        }
    }

    fun isPrintkSupported(): Boolean {
        return File(NODE_KERNEL_PRINTK).exists()
    }

    fun getCurrentPrintkLevel(): String {
        val raw = readNode(NODE_KERNEL_PRINTK)
        if (!raw.isNullOrBlank()) {
            val firstToken = raw.split("\\s+".toRegex()).firstOrNull()
            if (firstToken != null) {
                return firstToken
            }
        }
        return "4"
    }

    fun setPrintkLevel(level: String): Boolean {
        return writeNode(NODE_KERNEL_PRINTK, "$level 4 1 7")
    }

    fun syncPrintk(context: Context) {
        if (!isPrintkSupported()) return
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val savedLevel = prefs.getString(KEY_KERNEL_PRINTK_LEVEL, null)
        if (!savedLevel.isNullOrBlank()) {
            setPrintkLevel(savedLevel)
        }
    }

    fun isNtsyncSupported(): Boolean {
        return File(NODE_NTSYNC).exists()
    }
}
