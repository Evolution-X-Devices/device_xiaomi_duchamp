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

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.android.settingslib.widget.FooterPreference
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.xiaomi.settings.debug.DebugSettingsFragment

class PartsFragment : SettingsBasePreferenceFragment() {

    private var clickCount = 0
    private var lastClickTime = 0L

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

        updateDebugVisibility()
        setupFooterEasterEgg()
    }

    private fun setupFooterEasterEgg() {
        val footer = findPreference<FooterPreference>("parts_footer") ?: return
        footer.isSelectable = true

        footer.setOnPreferenceClickListener {
            val currentTime = SystemClock.uptimeMillis()
            if (currentTime - lastClickTime > 2500L) {
                clickCount = 0
            }
            lastClickTime = currentTime
            clickCount++

            val prefs = preferenceManager.sharedPreferences
            val alreadyUnlocked = prefs?.getBoolean(DebugSettingsFragment.PREF_DEBUG_UNLOCKED, false) ?: false

            if (alreadyUnlocked) {
                openDebugScreen()
                return@setOnPreferenceClickListener true
            }

            val remaining = 5 - clickCount
            if (remaining in 1..3) {
                Toast.makeText(
                    context,
                    getString(R.string.parts_debug_countdown_toast, remaining),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (remaining <= 0) {
                prefs?.edit()?.putBoolean(DebugSettingsFragment.PREF_DEBUG_UNLOCKED, true)?.apply()
                findPreference<Preference>("debug_info")?.isVisible = true
                Toast.makeText(context, R.string.parts_debug_unlocked_toast, Toast.LENGTH_SHORT).show()
                openDebugScreen()
                clickCount = 0
            }

            true
        }
    }

    private fun updateDebugVisibility() {
        val prefs = preferenceManager.sharedPreferences
        val isDebugUnlocked = prefs?.getBoolean(DebugSettingsFragment.PREF_DEBUG_UNLOCKED, false) ?: false
        findPreference<Preference>("debug_info")?.isVisible = isDebugUnlocked
    }

    private fun openDebugScreen() {
        val intent = Intent(requireContext(), PartsActivity::class.java).apply {
            putExtra(":settings:show_fragment", DebugSettingsFragment::class.java.name)
            putExtra(":settings:show_fragment_title", getString(R.string.parts_debug_button_title))
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.app_name)
        updateDebugVisibility()
    }
}
