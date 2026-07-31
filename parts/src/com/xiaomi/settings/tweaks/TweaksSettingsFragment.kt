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

package com.xiaomi.settings.tweaks                                                                                                                                                         
                                                                                                                                                                                                         
import android.os.Bundle                                                                                                                                                                         
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.xiaomi.settings.R

class TweaksSettingsFragment : SettingsBasePreferenceFragment() {                                                                                                                                     
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {                                                                                                                                                                                                                                                    
        setPreferencesFromResource(R.xml.tweaks_settings, rootKey)                                                                                                                              
    }                                                                                                                                                                                            
} 