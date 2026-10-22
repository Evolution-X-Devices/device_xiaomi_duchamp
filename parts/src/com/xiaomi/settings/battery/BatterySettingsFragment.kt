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

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.xiaomi.settings.R

class BatterySettingsFragment : SettingsBasePreferenceFragment() {

    private var chargingSpeedPref: ListPreference? = null
    private var bypassChargingPref: SwitchPreferenceCompat? = null
    private var smartChargeLimitPref: ListPreference? = null
    private var calibrationPref: Preference? = null

    private val prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == BatteryUtils.PREF_BYPASS_CHARGING ||
            key == BatteryUtils.PREF_SMART_BYPASS_ACTIVE ||
            key == BatteryUtils.PREF_SMART_CHARGE_LIMIT) {
            activity?.runOnUiThread {
                updateUiState()
            }
        }
    }

    private val powerStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            when (action) {
                Intent.ACTION_POWER_DISCONNECTED -> {
                    BatteryUtils.onChargerDisconnected(context)
                    activity?.runOnUiThread {
                        updateUiState(isPluggedOverride = false)
                    }
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    activity?.runOnUiThread {
                        updateUiState(isPluggedOverride = true)
                    }
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    val plugged = intent.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, 0)
                    val isPlugged = plugged > 0
                    if (!isPlugged && BatteryUtils.isBypassActive()) {
                        BatteryUtils.onChargerDisconnected(context)
                    }
                    activity?.runOnUiThread {
                        updateUiState(isPluggedOverride = isPlugged)
                    }
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.battery_settings, rootKey)

        chargingSpeedPref = findPreference(BatteryUtils.PREF_CHARGING_SPEED)
        bypassChargingPref = findPreference(BatteryUtils.PREF_BYPASS_CHARGING)
        smartChargeLimitPref = findPreference(BatteryUtils.PREF_SMART_CHARGE_LIMIT)
        calibrationPref = findPreference("battery_calibration")

        setupChargingSpeed()
        setupBypassCharging()
        setupSmartChargeLimit()
        setupCalibration()
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        requireContext().registerReceiver(powerStateReceiver, filter, Context.RECEIVER_EXPORTED)

        updateUiState()
    }

    override fun onPause() {
        super.onPause()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.unregisterOnSharedPreferenceChangeListener(prefChangeListener)

        try {
            requireContext().unregisterReceiver(powerStateReceiver)
        } catch (_: Exception) {}
    }

    private fun setupChargingSpeed() {
        val pref = chargingSpeedPref ?: return
        pref.entries = arrayOf(
            getString(R.string.charging_speed_67w),
            getString(R.string.charging_speed_33w),
            getString(R.string.charging_speed_18w)
        )
        pref.entryValues = arrayOf(
            BatteryUtils.SPEED_67W,
            BatteryUtils.SPEED_33W,
            BatteryUtils.SPEED_18W
        )

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val currentSpeed = prefs.getString(BatteryUtils.PREF_CHARGING_SPEED, BatteryUtils.SPEED_67W) ?: BatteryUtils.SPEED_67W
        pref.value = currentSpeed

        pref.summaryProvider = Preference.SummaryProvider<ListPreference> { p ->
            if (BatteryUtils.isBypassActive()) {
                getString(R.string.charging_speed_disabled_summary)
            } else {
                val speed = p.value ?: BatteryUtils.SPEED_67W
                val label = when (speed) {
                    BatteryUtils.SPEED_33W -> getString(R.string.charging_speed_33w)
                    BatteryUtils.SPEED_18W -> getString(R.string.charging_speed_18w)
                    else -> getString(R.string.charging_speed_67w)
                }
                getString(R.string.charging_speed_summary, label)
            }
        }

        pref.setOnPreferenceChangeListener { _, newValue ->
            val speed = newValue as String
            BatteryUtils.applyChargingSpeed(requireContext(), speed)
            true
        }
    }

    private fun setupBypassCharging() {
        val pref = bypassChargingPref ?: return

        pref.setOnPreferenceChangeListener { _, newValue ->
            val enable = newValue as Boolean
            if (enable && !BatteryUtils.isChargerConnected(requireContext())) {
                Toast.makeText(requireContext(), R.string.bypass_charger_required_toast, Toast.LENGTH_SHORT).show()
                return@setOnPreferenceChangeListener false
            }

            BatteryUtils.setBypassMode(requireContext(), enable)
            updateChargingSpeedState(enable)
            true
        }
    }

    private fun setupSmartChargeLimit() {
        val pref = smartChargeLimitPref ?: return
        pref.entries = arrayOf(
            getString(R.string.smart_charge_limit_off),
            getString(R.string.smart_charge_limit_80),
            getString(R.string.smart_charge_limit_85),
            getString(R.string.smart_charge_limit_90)
        )
        pref.entryValues = arrayOf("0", "80", "85", "90")

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val currentLimit = prefs.getString(BatteryUtils.PREF_SMART_CHARGE_LIMIT, "0") ?: "0"
        pref.value = currentLimit

        pref.summaryProvider = Preference.SummaryProvider<ListPreference> { p ->
            val limit = p.value ?: "0"
            val label = when (limit) {
                "80" -> getString(R.string.smart_charge_limit_80)
                "85" -> getString(R.string.smart_charge_limit_85)
                "90" -> getString(R.string.smart_charge_limit_90)
                else -> getString(R.string.smart_charge_limit_off)
            }
            getString(R.string.smart_charge_limit_summary, label)
        }

        pref.setOnPreferenceChangeListener { _, newValue ->
            val limitStr = newValue as String
            val limit = limitStr.toIntOrNull() ?: 0

            prefs.edit().putString(BatteryUtils.PREF_SMART_CHARGE_LIMIT, limitStr).apply()

            if (limit > 0) {
                BatteryService.startService(requireContext())
            } else {
                if (prefs.getBoolean(BatteryUtils.PREF_SMART_BYPASS_ACTIVE, false)) {
                    prefs.edit().putBoolean(BatteryUtils.PREF_SMART_BYPASS_ACTIVE, false).apply()
                    BatteryUtils.setBypassMode(requireContext(), false, isSmartLimit = true)
                }
                if (!BatteryUtils.isBypassActive()) {
                    BatteryService.stopService(requireContext())
                }
            }

            val isPlugged = BatteryUtils.isChargerConnected(requireContext())
            val level = BatteryUtils.getBatteryLevel(requireContext())
            BatteryUtils.onBatteryLevelChanged(requireContext(), level, isPlugged, limitOverride = limit)
            updateUiState()
            true
        }
    }

    private fun setupCalibration() {
        val pref = calibrationPref ?: return
        pref.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.battery_calibration_dialog_title)
                .setMessage(R.string.battery_calibration_dialog_message)
                .setPositiveButton(R.string.battery_calibration_dialog_confirm) { _, _ ->
                    val success = BatteryUtils.resetBatteryGauge()
                    if (success) {
                        Toast.makeText(requireContext(), R.string.battery_calibration_toast_success, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), R.string.battery_calibration_toast_failed, Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }
    }

    private fun updateUiState(isPluggedOverride: Boolean? = null) {
        val context = context ?: return
        val isPlugged = isPluggedOverride ?: BatteryUtils.isChargerConnected(context)
        val isBypass = BatteryUtils.isBypassActive(context) && isPlugged

        bypassChargingPref?.apply {
            isEnabled = isPlugged
            isChecked = isBypass
            summary = if (isPlugged) {
                getString(R.string.bypass_charging_summary)
            } else {
                getString(R.string.bypass_charger_required_toast)
            }
        }

        updateChargingSpeedState(isBypass)
    }

    private fun updateChargingSpeedState(isBypass: Boolean) {
        val pref = chargingSpeedPref ?: return
        pref.isEnabled = !isBypass
        // Trigger summary refresh
        pref.summaryProvider = pref.summaryProvider
    }
}
