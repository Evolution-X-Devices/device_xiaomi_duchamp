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

package com.xiaomi.settings.corecontrol

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.xiaomi.settings.R
import java.io.File

// fragment
class CoreControlSettingsFragment : SettingsBasePreferenceFragment() {

    // number of cores
    private val numCores = 8

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // interface
        setPreferencesFromResource(R.xml.core_control_settings, rootKey)
        val mainSwitch = findPreference<MainSwitchPreference>("core_control_switch")
        val smallCores = findPreference<PreferenceCategory>("small_cores_category")
        val bigCores = findPreference<PreferenceCategory>("big_cores_category")
        val primeCore = findPreference<PreferenceCategory>("prime_cores_category")
        val isEnabled = mainSwitch?.isChecked ?: false

        smallCores?.isEnabled = isEnabled
        bigCores?.isEnabled = isEnabled
        primeCore?.isEnabled = isEnabled

        mainSwitch?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            smallCores?.isEnabled = enabled
            bigCores?.isEnabled = enabled
            primeCore?.isEnabled = enabled
            
            // if feature are disabled then enable all cores
            if (!enabled) {
                for (i in 0 until numCores) {
                    setCoreState(i, true)
                    findPreference<SwitchPreferenceCompat>("core${i + 1}")?.isChecked = true
                }
            }
            true
        }

        for (i in 0 until numCores) {
            findPreference<SwitchPreferenceCompat>("core${i + 1}")?.apply {
                // if feature are disabled then even dont read sysfs state
                isChecked = if (isEnabled) isCoreEnabled(i) else true
                
                setOnPreferenceChangeListener { _, newValue ->
                    if (!(mainSwitch?.isChecked ?: false)) return@setOnPreferenceChangeListener false
                    
                    val enable = newValue as Boolean
                    
                    // dont give chance to disable ALL cores
                    if (!enable) {
                        if (!canOffline(i)) {
                            Toast.makeText(context, R.string.core_control_toast, Toast.LENGTH_SHORT).show()
                            return@setOnPreferenceChangeListener false
                        }
                    }
                    
                    setCoreState(i, enable)
                    true
                }
            }
        }
    }

    // just path
    private fun getCorePath(core: Int) = "/sys/devices/system/cpu/cpu$core/online"

    // check if cores is enabled
    private fun isCoreEnabled(core: Int): Boolean {
        return try {
            val file = File(getCorePath(core))
            file.exists() && file.readText().trim() == "1"
        } catch (e: Exception) {
            false
        }
    }


    private fun setCoreState(core: Int, online: Boolean) {
        try {
            File(getCorePath(core)).writeText(if (online) "1" else "0")
        } catch (e: Exception) {
            Log.e("CoreControl", "Cant change $core state", e)
        }
    }

    // check how many cores are enabled
    private fun canOffline(core: Int): Boolean {
        val activeTotal = (0 until numCores).count {
            it != core && findPreference<SwitchPreferenceCompat>("core${it + 1}")?.isChecked == true
        }
        if (activeTotal < 2) return false

        if (core in 0..3) {
            val activeSmall = (0..3).count {
                it != core && findPreference<SwitchPreferenceCompat>("core${it + 1}")?.isChecked == true
            }
            if (activeSmall < 2) return false
        }
        
        return true
    }
}