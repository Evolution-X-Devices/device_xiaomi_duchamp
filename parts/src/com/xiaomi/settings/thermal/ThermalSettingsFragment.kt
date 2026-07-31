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

package com.xiaomi.settings.thermal                                                                                                                                                              
                                                                                                                                                                                                         
import android.os.Bundle                                                                                                                                                                         
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.xiaomi.settings.R

class ThermalSettingsFragment : SettingsBasePreferenceFragment() {                                                                                                                                     
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {                                                                                                                                                                                                                                                    
        setPreferencesFromResource(R.xml.thermal_settings, rootKey)                                                                                                                              
    }                                                                                                                                                                                            
} 