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

package com.xiaomi.settings.battery

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import android.util.Log

class BatteryService : Service() {

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            Log.d(TAG, "onReceive action: $action")

            when (action) {
                Intent.ACTION_POWER_DISCONNECTED -> {
                    Log.i(TAG, "Power disconnected: resetting bypass")
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
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BatteryService started")
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(batteryReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "BatteryService stopped")
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister batteryReceiver: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "BatteryService"

        fun startService(context: Context) {
            try {
                context.startService(Intent(context, BatteryService::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start BatteryService: ${e.message}")
            }
        }

        fun stopService(context: Context) {
            try {
                context.stopService(Intent(context, BatteryService::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop BatteryService: ${e.message}")
            }
        }
    }
}
