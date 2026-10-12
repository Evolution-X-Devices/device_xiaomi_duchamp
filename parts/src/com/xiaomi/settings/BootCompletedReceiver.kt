/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder;
import android.hardware.display.DisplayManager;
import android.util.Log
import android.view.Display;
import android.view.Display.HdrCapabilities;
import com.xiaomi.settings.display.ColorService

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (DEBUG) Log.d(TAG, "Received boot completed intent: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> onBootCompleted(context)
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> onLockedBootCompleted(context)
        }

        // Override HDR types
        val displayManager = context.getSystemService(DisplayManager::class.java)
        displayManager.overrideHdrTypes(
            Display.DEFAULT_DISPLAY,
            intArrayOf(
                HdrCapabilities.HDR_TYPE_DOLBY_VISION,
                HdrCapabilities.HDR_TYPE_HDR10,
                HdrCapabilities.HDR_TYPE_HLG,
                HdrCapabilities.HDR_TYPE_HDR10_PLUS
            )
        )
    }

    private fun onBootCompleted(context: Context) {
    }

    private fun onLockedBootCompleted(context: Context) {
        // Display
        // HTSR
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.getBoolean("htsr_global_enable", false) || prefs.getBoolean("htsr_enable", false)) {
            com.xiaomi.settings.htsr.HTSRService.startService(context)
        }

        ColorService.startService(context)

        // Thermals
        prefs.edit().putString("thermal_profile", com.xiaomi.settings.thermal.ThermalUtils.getCurrentProfile()).apply()

        if (prefs.getBoolean("thermal_auto_mode", false)) {
            com.xiaomi.settings.thermal.ThermalService.startService(context)
        }

        // Wakelock Blocker
        if (com.xiaomi.settings.tweaks.WakelockBlockerUtils.isEnabled(context)) {
            com.xiaomi.settings.tweaks.WakelockBlockerUtils.sync(context)
        }

        // Kernel Tweaks
        com.xiaomi.settings.tweaks.KernelTweaksUtils.syncTcpCongestion(context)
        com.xiaomi.settings.tweaks.KernelTweaksUtils.syncZram(context)
    }

    companion object {
        private const val TAG = "BootReceiver"
        private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)
    }
}
