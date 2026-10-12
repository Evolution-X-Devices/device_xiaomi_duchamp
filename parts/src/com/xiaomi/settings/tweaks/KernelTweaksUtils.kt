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
}
