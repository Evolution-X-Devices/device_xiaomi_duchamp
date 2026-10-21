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

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.LayoutPreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.xiaomi.settings.R

class ThermalSettingsFragment : SettingsBasePreferenceFragment() {

    private lateinit var headerPref: LayoutPreference
    private lateinit var autoModePref: SwitchPreferenceCompat
    private var activeProfile: String = "2"

    private val profileKeys = mapOf(
        "thermal_profile_battery_saver" to "1",
        "thermal_profile_balance" to "2",
        "thermal_profile_performance" to "6",
        "thermal_profile_game" to "19",
        "thermal_profile_danger" to "1001"
    )

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.thermal_settings, rootKey)

        headerPref = findPreference("thermal_header")!!
        autoModePref = findPreference("thermal_auto_mode")!!

        val prefs = preferenceManager.sharedPreferences
        activeProfile = prefs?.getString("thermal_profile", "2") ?: "2"

        for ((prefKey, profileValue) in profileKeys) {
            val radioPref = findPreference<RadioButtonPreference>(prefKey)
            radioPref?.isChecked = (profileValue == activeProfile)
            
            radioPref?.setOnPreferenceClickListener {
                if (activeProfile == profileValue) {
                    bumpHeaderIcon(profileValue)
                    return@setOnPreferenceClickListener true
                }
                
                activeProfile = profileValue

                profileKeys.keys.filter { it != prefKey }.forEach { key ->
                    findPreference<RadioButtonPreference>(key)?.isChecked = false
                }
                radioPref.isChecked = true
                
                prefs?.edit()?.putString("thermal_profile", profileValue)?.apply()
                updateHeader(profileValue, true)
                ThermalUtils.applyProfile(requireContext(), profileValue)
                
                if (profileValue == "1001") {
                    ThermalUtils.showDangerNotification(requireContext())
                } else {
                    ThermalUtils.clearDangerNotification(requireContext())
                }

                true
            }
        }

        autoModePref.isChecked = prefs?.getBoolean("thermal_auto_mode", false) ?: false
        autoModePref.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            prefs?.edit()?.putBoolean("thermal_auto_mode", enabled)?.apply()
            if (enabled) {
                ThermalService.startService(requireContext())
            } else {
                ThermalService.stopService(requireContext())
            }
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            val prefs = preferenceManager.sharedPreferences
            
            // sync with ui
            activeProfile = ThermalUtils.getCurrentProfile()
            prefs?.edit()?.putString("thermal_profile", activeProfile)?.apply()
            
            updateHeader(activeProfile)

            for ((prefKey, profileValue) in profileKeys) {
                val radioPref = findPreference<RadioButtonPreference>(prefKey)
                radioPref?.isChecked = (profileValue == activeProfile)
            }
        }
    }
    // colors 
    private fun getProfileColor(profile: String): Int {
        val isDark = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        return try {
            Color.parseColor(when (profile) {
                "1" -> if (isDark) "#8FD09A" else "#146C2E" // Battery Saver
                "2" -> if (isDark) "#A8C7FA" else "#0B57D0" // Balanced
                "6" -> if (isDark) "#FFB49C" else "#8F4C38" // Performance
                "19" -> if (isDark) "#D0BCFF" else "#6750A4" // Game
                "1001" -> if (isDark) "#F2B8B5" else "#B3261E" // Danger
                else -> if (isDark) "#A8C7FA" else "#0B57D0"
            })
        } catch (e: Exception) {
            Color.GRAY
        }
    }
    
    // names
    private fun getProfileNameRes(profile: String): Int = when (profile) {
        "1" -> R.string.thermal_profile_battery_saver
        "2" -> R.string.thermal_profile_balanced
        "6" -> R.string.thermal_profile_performance
        "19" -> R.string.thermal_profile_game
        "1001" -> R.string.thermal_profile_danger
        else -> R.string.thermal_profile_unknown
    }

    // icons
    private fun getProfileIconRes(profile: String): Int = when (profile) {
        "1" -> R.drawable.ic_thermal_battery
        "2" -> R.drawable.ic_thermal_balance
        "6" -> R.drawable.ic_thermal_performance
        "19" -> R.drawable.ic_thermal_game
        "1001" -> R.drawable.ic_thermal_danger
        else -> R.drawable.ic_thermal_balance
    }

    private fun bumpHeaderIcon(profile: String) {
        val iconView = headerPref.findViewById(R.id.thermal_header_icon) as? ImageView ?: return
        
        updateHeader(profile, animate = false)
        
        iconView.animate()
            .scaleX(1.15f).scaleY(1.15f)
            .setDuration(100)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                iconView.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(android.view.animation.OvershootInterpolator(2.5f))
                    .start()
            }
            .start()
    }

    private fun updateHeader(profile: String, animate: Boolean = false) {
        val titleView = headerPref.findViewById(R.id.thermal_header_title) as? TextView ?: return
        val iconView = headerPref.findViewById(R.id.thermal_header_icon) as? ImageView ?: return

        val color = getProfileColor(profile)
        val textRes = getProfileNameRes(profile)
        val iconRes = getProfileIconRes(profile)

        iconView.animate().cancel()
        titleView.animate().cancel()

        if (!animate) {
            iconView.apply {
                scaleX = 1f
                scaleY = 1f
                alpha = 1f
                setImageResource(iconRes)
                setColorFilter(color)
                background?.mutate()?.apply {
                    setTint(color)
                    alpha = 38
                }
            }
            titleView.apply {
                alpha = 1f
                setText(textRes)
            }
            return
        }
        
        // if user spams with profiles, prevent the icon from fading out
        iconView.alpha = 1f
        iconView.scaleX = 1f
        iconView.scaleY = 1f

        // animations
        iconView.animate()
            .scaleX(0.7f).scaleY(0.7f).alpha(0f)
            .setDuration(150)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                // profile switch
                titleView.setText(textRes)
                iconView.setImageResource(iconRes)
                iconView.setColorFilter(color)
                iconView.background?.mutate()?.apply {
                    setTint(color)
                    alpha = 38
                }

                // same profile
                iconView.animate()
                    .scaleX(1f).scaleY(1f).alpha(1f)
                    .setDuration(300)
                    .setInterpolator(android.view.animation.OvershootInterpolator(1.5f))
                    .start()
            }
            .start()

        // title animation
        titleView.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                titleView.animate()
                    .alpha(1f)
                    .setDuration(250)
                    .start()
            }
            .start()
    }
}

// switch buttons
class RadioButtonPreference(context: android.content.Context, attrs: android.util.AttributeSet?) : androidx.preference.CheckBoxPreference(context, attrs) {
    init {
        widgetLayoutResource = context.resources.getIdentifier("settingslib_preference_widget_radiobutton", "layout", context.packageName)
        if (widgetLayoutResource == 0) {
            widgetLayoutResource = context.resources.getIdentifier("preference_widget_radiobutton", "layout", "android")
        }
    }

    override fun onClick() {
        if (isChecked) {
            return
        }
        super.onClick()
    }
}
