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

package com.xiaomi.settings

import android.os.Bundle
import androidx.preference.PreferenceCategory
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment

class PartsFragment : SettingsBasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.parts_settings, rootKey)

        val mainSwitch = findPreference<MainSwitchPreference>("main_switch")
        val partsCategory = findPreference<PreferenceCategory>("parts_category")

        partsCategory?.isEnabled = mainSwitch?.isChecked ?: false

        mainSwitch?.setOnPreferenceChangeListener { _, newValue ->
            val isEnabled = newValue as Boolean

            partsCategory?.isEnabled = isEnabled
            
            // when parts is disabled, set profile to balanced
            if (!isEnabled) {
                val prefs = preferenceManager.sharedPreferences
                prefs?.edit()?.putString("thermal_profile", "2")?.apply()
                com.xiaomi.settings.thermal.ThermalUtils.applyProfile(requireContext(), "2")
                com.xiaomi.settings.thermal.ThermalUtils.clearDangerNotification(requireContext())
            }
            
            true
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.app_name)
    }
}
