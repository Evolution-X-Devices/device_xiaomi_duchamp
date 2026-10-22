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
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.xiaomi.settings.R

class BypassChargeTileService : TileService() {

    private var isReceiverRegistered = false
    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateFromCurrentState()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(Intent.ACTION_BATTERY_CHANGED)
            }
            registerReceiver(powerReceiver, filter, Context.RECEIVER_EXPORTED)
            isReceiverRegistered = true
        }
        updateFromCurrentState()
    }

    override fun onStopListening() {
        super.onStopListening()
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(powerReceiver)
            } catch (_: Exception) {}
            isReceiverRegistered = false
        }
    }

    private fun updateFromCurrentState() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isBypass = prefs.getBoolean(BatteryUtils.PREF_BYPASS_CHARGING, false)
        val isPlugged = BatteryUtils.isChargerConnected(this)
        applyTileState(isBypass, isPlugged)
    }

    override fun onClick() {
        super.onClick()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val currentState = prefs.getBoolean(BatteryUtils.PREF_BYPASS_CHARGING, false)
        val newState = !currentState

        val isPlugged = BatteryUtils.isChargerConnected(this)

        if (newState && !isPlugged) {
            Toast.makeText(this, R.string.bypass_charger_required_toast, Toast.LENGTH_SHORT).show()
            applyTileState(isBypass = false, isPlugged = false)
            return
        }

        // Optimistic instant UI update
        applyTileState(isBypass = newState, isPlugged = isPlugged)

        prefs.edit().putBoolean(BatteryUtils.PREF_BYPASS_CHARGING, newState).apply()

        Thread {
            BatteryUtils.setBypassMode(this, newState, skipTileRequest = true)
        }.start()
    }

    private fun applyTileState(isBypass: Boolean, isPlugged: Boolean) {
        val qsTile = qsTile ?: return
        qsTile.label = getString(R.string.bypass_tile_label)
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_bypass_charging)

        if (isBypass && isPlugged) {
            qsTile.state = Tile.STATE_ACTIVE
            qsTile.subtitle = getString(R.string.bypass_notification_title)
        } else {
            qsTile.state = if (isPlugged) Tile.STATE_INACTIVE else Tile.STATE_UNAVAILABLE
            qsTile.subtitle = if (isPlugged) {
                getString(R.string.smart_charge_limit_off)
            } else {
                getString(R.string.bypass_charger_required_toast)
            }
        }

        qsTile.updateTile()
    }
}
