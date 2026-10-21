/*
 * ==============================================================
 *    __  __  _____   __    ___    ___   _____   _
 *   |  \/  ||_   _| / /_  ( _ )  / _ \ |___  | / \
 *   | |\/| |  | |  | '_ \ / _ \ | (_) |   / / / _ \
 *   | |  | |  | |  | (_) | (_) | \__, |  / / / ___ \
 *   |_|  |_|  |_|   \___/ \___/    /_/  /_/ /_/   \_\
 *
 *  SPDX-FileCopyrightText: 2026 MT6897A
 *  SPDX-License-Identifier: Apache-2.0
 * ==============================================================
 */

package com.xiaomi.settings.htsr

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.preference.PreferenceManager
import com.xiaomi.settings.R

class HTSRTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val currentState = prefs.getBoolean(PREF_HTSR_GLOBAL, false)
        val newState = !currentState

        prefs.edit().putBoolean(PREF_HTSR_GLOBAL, newState).apply()

        HTSRService.startService(this)
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isGlobal = prefs.getBoolean(PREF_HTSR_GLOBAL, false)

        tile.label = getString(R.string.htsr_tile_label)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_htsr)

        if (isGlobal) {
            tile.state = Tile.STATE_ACTIVE
            tile.subtitle = getString(R.string.htsr_tile_active)
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.subtitle = getString(R.string.htsr_tile_inactive)
        }

        tile.updateTile()
    }

    companion object {
        private const val TAG = "HTSRTileService"
        const val PREF_HTSR_GLOBAL = "htsr_global_enable"

        fun updateTile(context: Context) {
            try {
                requestListeningState(
                    context,
                    ComponentName(context, HTSRTileService::class.java)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request tile listening state: ${e.message}")
            }
        }
    }
}
