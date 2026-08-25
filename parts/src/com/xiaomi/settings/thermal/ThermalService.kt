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
package com.xiaomi.settings.thermal

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log

class ThermalService : Service() {
    // auto mode, just write sconfig 1 when screen is off and restore current profile when screen is on.
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "Screen off: switching to Battery Saver")
                    ThermalUtils.applyProfile(context, "1")
                }
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "Screen on: restoring thermal profile")
                    ThermalUtils.applyDefaultProfile(context)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ThermalService started")
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)
        ThermalUtils.applyDefaultProfile(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ThermalService stopped")
        unregisterReceiver(screenStateReceiver)
        ThermalUtils.applyDefaultProfile(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "ThermalService"
        
        fun startService(context: Context) {
            context.startService(Intent(context, ThermalService::class.java))
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, ThermalService::class.java))
        }
    }
}
