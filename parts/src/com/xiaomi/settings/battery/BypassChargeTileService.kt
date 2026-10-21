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

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.xiaomi.settings.R

class BypassChargeTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        if (!BatteryUtils.isChargerConnected(this)) {
            Toast.makeText(this, R.string.bypass_charger_required_toast, Toast.LENGTH_SHORT).show()
            updateTileState()
            return
        }

        val currentState = BatteryUtils.isBypassActive()
        val newState = !currentState

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putBoolean(BatteryUtils.PREF_BYPASS_CHARGING, newState).apply()

        BatteryUtils.setBypassMode(this, newState)
        updateTileState()
    }

    private fun updateTileState() {
        val qsTile = qsTile ?: return
        val isPlugged = BatteryUtils.isChargerConnected(this)
        val isBypass = BatteryUtils.isBypassActive() && isPlugged

        qsTile.label = getString(R.string.bypass_tile_label)
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_bypass_charging)

        if (isBypass) {
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
