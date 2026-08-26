/*
 * ==============================================================
 *    __  __  _____   __    ___    ___   _____   _
 *   |  \/  ||_   _| / /_  ( _ )  / _ \ |___  | / \
 *   | |\/| |  | |  | '_ \ / _ \ | (_) |   / / / _ \
 *   | |  | |  | |  | (_) | (_) | \__, |  / / / ___ \
 *   |_|  |_|  |_|   \___/ \___/    /_/  /_/ /_/   \_\
 *
 *  SPDX-FileCopyRightText: 2024 Paranoid Android
 *  SPDX-FileCopyRightText: 2026 MT6897A
 *  SPDX-License-Identifier: Apache-2.0
 * ==============================================================
 */
package com.xiaomi.settings.thermal

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import com.xiaomi.settings.R

class ThermalProfileTileService : TileService() {

    private fun isPartsEnabled(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getBoolean("main_switch", false)
    }

    private fun updateUI(profile: String) {
        val tile = qsTile ?: return
        
        tile.label = getString(R.string.thermal_qs_title)

        // disable tile when parts is disabled
        if (!isPartsEnabled()) {
            tile.subtitle = getString(R.string.thermal_profile_balanced) // Balanced
            tile.icon = Icon.createWithResource(this, R.drawable.ic_thermal_balance)
            tile.state = Tile.STATE_UNAVAILABLE
            tile.updateTile()
            return
        }

        // strings
        val subtitleRes = when (profile) {
            "1" -> R.string.thermal_profile_battery_saver
            "2" -> R.string.thermal_profile_balanced
            "6" -> R.string.thermal_profile_performance
            "19" -> R.string.thermal_profile_game
            "1001" -> R.string.thermal_profile_danger
            else -> R.string.thermal_profile_unknown
        }
        tile.subtitle = getString(subtitleRes)

        // icons
        val iconRes = when (profile) {
            "1" -> R.drawable.ic_thermal_battery
            "2" -> R.drawable.ic_thermal_balance
            "6" -> R.drawable.ic_thermal_performance
            "19" -> R.drawable.ic_thermal_game
            "1001" -> R.drawable.ic_thermal_danger
            else -> R.drawable.ic_thermal_balance
        }
        tile.icon = Icon.createWithResource(this, iconRes)
        
        tile.state = Tile.STATE_ACTIVE
        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateUI(ThermalUtils.getCurrentProfile())
    }

    override fun onClick() {
        super.onClick()
        if (!isPartsEnabled()) return

        val currentProfile = ThermalUtils.getCurrentProfile()
        
        // profiles switching
        val newProfile = when (currentProfile) {
            "2" -> "6" // Performance
            "6" -> "19" // Game
            "19" -> "1001" // Danger
            "1001" -> "1" // Battery Saver
            "1" -> "2" // Balanced
            else -> "2"
        }

        ThermalUtils.applyProfile(this, newProfile)
        
        // sync with ui
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putString("thermal_profile", newProfile).apply()
        
        // danger notification
        if (newProfile == "1001") {
            ThermalUtils.showDangerNotification(this)
        } else {
            ThermalUtils.clearDangerNotification(this)
        }

        updateUI(newProfile)
    }
}
