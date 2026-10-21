/*
 * ==============================================================
 *    __  __  _____   __    ___    ___   _____   _
 *   |  \/  ||_   _| / /_  ( _ )  / _ \ |___  | / \
 *   | |\/| |  | |  | '_ \ / _ \ | (_) |   / / / _ \
 *   | |  | |  | |  | (_) | (_) | \__, |  / / / ___ \
 *   |_|  |_|  |_|   \___/ \___/    /_/  /_/ /_/   \_\
 *
 *  SPDX-FileCopyrightText: 2024 The Evolution X Project
 *  SPDX-FileCopyRightText: 2026 MT6897A
 *  SPDX-License-Identifier: Apache-2.0
 * ==============================================================
 */

package com.xiaomi.settings.htsr

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.xiaomi.settings.R

class HTSRSettingsFragment : SettingsBasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.htsr_settings, rootKey)

        val globalSwitch = findPreference<MainSwitchPreference>("htsr_global_enable")
        val perAppPref = findPreference<Preference>("htsr_per_app")

        fun updatePerAppState(isGlobalActive: Boolean) {
            perAppPref?.isEnabled = !isGlobalActive
            perAppPref?.summary = if (isGlobalActive) {
                getString(R.string.htsr_per_app_disabled_summary)
            } else {
                getString(R.string.htsr_per_app_summary)
            }
        }

        globalSwitch?.setOnPreferenceChangeListener { _, newValue ->
            val isGlobal = newValue as Boolean
            updatePerAppState(isGlobal)
            HTSRService.startService(requireContext())
            HTSRTileService.updateTile(requireContext())
            true
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        updatePerAppState(prefs.getBoolean("htsr_global_enable", false))
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.htsr_button_title)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val isGlobal = prefs.getBoolean("htsr_global_enable", false)
        val isPerApp = prefs.getBoolean("htsr_enable", false)

        val perAppPref = findPreference<Preference>("htsr_per_app")
        perAppPref?.isEnabled = !isGlobal
        perAppPref?.summary = if (isGlobal) {
            getString(R.string.htsr_per_app_disabled_summary)
        } else {
            getString(R.string.htsr_per_app_summary)
        }

        if (isGlobal || isPerApp) {
            HTSRService.startService(requireContext())
        }
    }
}
