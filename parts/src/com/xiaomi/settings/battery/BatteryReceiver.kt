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

package com.xiaomi.settings.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import androidx.preference.PreferenceManager

class BatteryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "onReceive action: $action")

        when (action) {
            Intent.ACTION_POWER_DISCONNECTED -> {
                BatteryUtils.onChargerDisconnected(context)
            }
            Intent.ACTION_POWER_CONNECTED -> {
                val level = BatteryUtils.getBatteryLevel(context)
                BatteryUtils.onBatteryLevelChanged(context, level, true)
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val isPlugged = plugged > 0

                val batteryPct = if (level >= 0 && scale > 0) {
                    (level * 100) / scale
                } else {
                    50
                }

                if (!isPlugged && BatteryUtils.isBypassActive()) {
                    BatteryUtils.onChargerDisconnected(context)
                } else {
                    BatteryUtils.onBatteryLevelChanged(context, batteryPct, isPlugged)
                }
            }
            BatteryUtils.ACTION_STOP_BYPASS -> {
                Log.i(TAG, "ACTION_STOP_BYPASS received from notification")
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                prefs.edit()
                    .putBoolean(BatteryUtils.PREF_BYPASS_CHARGING, false)
                    .putBoolean(BatteryUtils.PREF_SMART_BYPASS_ACTIVE, false)
                    .apply()
                BatteryUtils.setBypassMode(context, false)
            }
        }
    }

    companion object {
        private const val TAG = "BatteryReceiver"
    }
}
