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
package com.xiaomi.settings.thermal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.xiaomi.settings.R
import java.io.File

object ThermalUtils {
    private const val TAG = "ThermalUtils"
    private const val THERMAL_SCONFIG = "/sys/class/thermal/thermal_message/sconfig"
    private const val DANGER_NOTIF_ID = 1001

    fun getCurrentProfile(): String {
        return try {
            val value = File(THERMAL_SCONFIG).readText().trim()
            if (value == "0") "2" else value
        } catch (e: Exception) {
            "2"
        }
    }

    fun applyProfile(context: Context, profile: String) {
        val sconfigVal = if (profile == "2") "0" else profile
        writeNode(THERMAL_SCONFIG, sconfigVal)
    }

    fun applyDefaultProfile(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        applyProfile(context, prefs.getString("thermal_profile", "2") ?: "2")
    }

    fun showDangerNotification(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val channelId = "thermal_danger_channel"
        
        nm.createNotificationChannel(NotificationChannel(
            channelId,
            context.getString(R.string.thermal_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ))

        val text = context.getString(R.string.thermal_danger_toast)
        val notif = Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_thermal_danger)
            .setContentTitle(context.getString(R.string.thermal_danger_notification_title))
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()

        nm.notify(DANGER_NOTIF_ID, notif)
    }

    fun clearDangerNotification(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(DANGER_NOTIF_ID)
    }

    private fun writeNode(node: String, value: String) {
        try {
            File(node).writeText(value)
            Log.d(TAG, "Wrote $value to $node")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write $value to $node: ${e.message}")
        }
    }
}
