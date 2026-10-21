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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.service.quicksettings.TileService
import android.util.Log
import androidx.preference.PreferenceManager
import com.xiaomi.settings.PartsActivity
import com.xiaomi.settings.R
import java.io.File

object BatteryUtils {
    private const val TAG = "BatteryUtils"

    // nodes
    const val NODE_CONSTANT_CHARGE_CURRENT = "/sys/class/power_supply/battery/constant_charge_current"
    const val NODE_BYPASS_CMD = "/proc/mtk_battery_cmd/current_cmd"
    const val NODE_BATTERY_RESET = "/sys/class/power_supply/battery/reset"

    // keys
    const val PREF_CHARGING_SPEED = "charging_speed"
    const val PREF_BYPASS_CHARGING = "bypass_charging"
    const val PREF_SMART_CHARGE_LIMIT = "smart_charge_limit"
    const val PREF_SMART_BYPASS_ACTIVE = "smart_bypass_active"

    // charge values
    const val SPEED_67W = "22000000"
    const val SPEED_33W = "7000000"
    const val SPEED_18W = "4500000"
    const val CURRENT_CUTOFF = "0"

    const val CMD_BYPASS_ON = "0 1"
    const val CMD_BYPASS_OFF = "0 0"

    private const val NOTIFICATION_CHANNEL_ID = "bypass_charging_channel"
    private const val NOTIFICATION_ID = 1002
    const val ACTION_STOP_BYPASS = "com.xiaomi.settings.battery.ACTION_STOP_BYPASS"

    @Volatile
    private var isBypassActive = false

    fun isBypassActive(): Boolean = isBypassActive

    fun isChargerConnected(context: Context): Boolean {
        return try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
            plugged > 0
        } catch (_: Exception) {
            false
        }
    }

    fun getBatteryLevel(context: Context): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            50
        }
    }

    fun applyChargingSpeed(context: Context, speed: String) {
        if (isBypassActive) {
            Log.d(TAG, "Bypass is active, holding charging current at 0")
            writeNode(NODE_CONSTANT_CHARGE_CURRENT, CURRENT_CUTOFF)
            return
        }
        val targetSpeed = if (speed.isNotEmpty()) speed else SPEED_67W
        writeNode(NODE_CONSTANT_CHARGE_CURRENT, targetSpeed)
    }

    @Synchronized
    fun setBypassMode(context: Context, enable: Boolean, isSmartLimit: Boolean = false) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        isBypassActive = enable

        if (!isSmartLimit) {
            prefs.edit().putBoolean(PREF_BYPASS_CHARGING, enable).apply()
        }

        if (enable) {
            Log.i(TAG, "Enabling Bypass Charging (isSmartLimit=$isSmartLimit)")
            writeNode(NODE_BYPASS_CMD, CMD_BYPASS_ON)
            writeNode(NODE_CONSTANT_CHARGE_CURRENT, CURRENT_CUTOFF)
            showBypassNotification(context)
            BatteryService.startService(context)
        } else {
            Log.i(TAG, "Disabling Bypass Charging (isSmartLimit=$isSmartLimit)")
            writeNode(NODE_BYPASS_CMD, CMD_BYPASS_OFF)
            val savedSpeed = prefs.getString(PREF_CHARGING_SPEED, SPEED_67W) ?: SPEED_67W
            writeNode(NODE_CONSTANT_CHARGE_CURRENT, savedSpeed)
            dismissBypassNotification(context)

            val smartLimit = (prefs.getString(PREF_SMART_CHARGE_LIMIT, "0") ?: "0").toIntOrNull() ?: 0
            if (smartLimit == 0) {
                BatteryService.stopService(context)
            }
        }

        updateTile(context)
    }

    fun onChargerDisconnected(context: Context) {
        Log.i(TAG, "Charger disconnected: resetting bypass and restoring defaults")
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .putBoolean(PREF_BYPASS_CHARGING, false)
            .putBoolean(PREF_SMART_BYPASS_ACTIVE, false)
            .apply()

        setBypassMode(context, false)

        val smartLimit = (prefs.getString(PREF_SMART_CHARGE_LIMIT, "0") ?: "0").toIntOrNull() ?: 0
        if (smartLimit == 0) {
            BatteryService.stopService(context)
        }
    }

    fun onBatteryLevelChanged(context: Context, level: Int, isPlugged: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!isPlugged) {
            if (isBypassActive) {
                onChargerDisconnected(context)
            }
            return
        }

        val limitStr = prefs.getString(PREF_SMART_CHARGE_LIMIT, "0") ?: "0"
        val limit = limitStr.toIntOrNull() ?: 0
        val manualBypass = prefs.getBoolean(PREF_BYPASS_CHARGING, false)

        if (manualBypass) {
            if (!isBypassActive) {
                setBypassMode(context, true, isSmartLimit = false)
            }
            return
        }

        if (limit > 0) {
            val isSmartActive = prefs.getBoolean(PREF_SMART_BYPASS_ACTIVE, false)
            if (level >= limit && !isSmartActive) {
                Log.i(TAG, "Battery reached limit ($level >= $limit%), activating smart bypass")
                prefs.edit().putBoolean(PREF_SMART_BYPASS_ACTIVE, true).apply()
                setBypassMode(context, true, isSmartLimit = true)
            } else if (level <= (limit - 2) && isSmartActive) {
                Log.i(TAG, "Battery dropped below hysteresis ($level <= ${limit - 2}%), resuming charging")
                prefs.edit().putBoolean(PREF_SMART_BYPASS_ACTIVE, false).apply()
                setBypassMode(context, false, isSmartLimit = true)
            }
        }
    }

    fun resetBatteryGauge(): Boolean {
        Log.i(TAG, "Resetting battery fuel gauge Coulomb counter")
        return writeNode(NODE_BATTERY_RESET, "1")
    }

    fun syncOnBoot(context: Context) {
        Log.i(TAG, "syncOnBoot: Ensuring clean initial battery state")
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        prefs.edit()
            .putBoolean(PREF_BYPASS_CHARGING, false)
            .putBoolean(PREF_SMART_BYPASS_ACTIVE, false)
            .apply()

        setBypassMode(context, false)

        val savedSpeed = prefs.getString(PREF_CHARGING_SPEED, SPEED_67W) ?: SPEED_67W
        applyChargingSpeed(context, savedSpeed)

        if (isChargerConnected(context)) {
            val level = getBatteryLevel(context)
            onBatteryLevelChanged(context, level, true)
        }
    }

    private fun showBypassNotification(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.bypass_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.bypass_notification_text)
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)

        val contentIntent = Intent(context, PartsActivity::class.java).apply {
            putExtra(PartsActivity.EXTRA_SHOW_FRAGMENT, BatterySettingsFragment::class.java.name)
            putExtra(PartsActivity.EXTRA_SHOW_FRAGMENT_TITLE, context.getString(R.string.battery_button_title))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, BatteryReceiver::class.java).apply {
            action = ACTION_STOP_BYPASS
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bypass_charging)
            .setContentTitle(context.getString(R.string.bypass_notification_title))
            .setContentText(context.getString(R.string.bypass_notification_text))
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .addAction(
                Notification.Action.Builder(
                    null,
                    context.getString(R.string.bypass_notification_action_stop),
                    stopPendingIntent
                ).build()
            )
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun dismissBypassNotification(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm?.cancel(NOTIFICATION_ID)
    }

    fun updateTile(context: Context) {
        try {
            TileService.requestListeningState(
                context,
                ComponentName(context, BypassChargeTileService::class.java)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request tile listening state: ${e.message}")
        }
    }

    private fun writeNode(path: String, value: String): Boolean {
        return try {
            java.io.FileOutputStream(path).use { fos ->
                fos.write((value.trim() + "\n").toByteArray())
                fos.flush()
            }
            Log.d(TAG, "Wrote '$value' to $path")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write '$value' to $path: ${e.message}", e)
            false
        }
    }
}
