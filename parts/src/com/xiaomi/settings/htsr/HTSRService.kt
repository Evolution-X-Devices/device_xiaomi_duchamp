/*
 * SPDX-FileCopyrightText: 2026 MT6897A
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.htsr

import android.app.ActivityManager
import android.app.IProcessObserver
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.ContentObserver
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.preference.PreferenceManager
import java.io.File

class HTSRService : Service() {
    private lateinit var prefs: SharedPreferences
    private var isGlobalEnabled = false
    private var isPerAppEnabled = false
    private var htsrApps = emptySet<String>()
    
    private val htsrObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            updateAppsList()
            if (!isGlobalEnabled) {
                writeNode("0")
            }
        }
    }
    
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "htsr_global_enable", "htsr_enable" -> {
                updateSettings()
                if (key == "htsr_global_enable") {
                    HTSRTileService.updateTile(this)
                }
            }
        }
    }

    private val processObserver = object : IProcessObserver.Stub() {
        override fun onProcessStarted(pid: Int, processUid: Int, packageUid: Int, packageName: String?, processName: String?) {}
        override fun onForegroundServicesChanged(pid: Int, uid: Int, serviceTypes: Int) {}
        override fun onProcessDied(pid: Int, uid: Int) {}
        
        override fun onForegroundActivitiesChanged(pid: Int, uid: Int, foregroundActivities: Boolean) {
            if (isGlobalEnabled) {
                writeNode("1")
                return
            }

            if (!isPerAppEnabled || !foregroundActivities) return
            
            val packages = packageManager.getPackagesForUid(uid) ?: return
            val shouldEnable = packages.any { it in htsrApps }
            
            writeNode(if (shouldEnable) "1" else "0")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "HTSRService started")
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
        
        contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(HTSR_APPS_KEY),
            false,
            htsrObserver
        )
        updateAppsList()
        updateSettings()
        
        try {
            ActivityManager.getService().registerProcessObserver(processObserver)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register process observer", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateSettings()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "HTSRService stopped")
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        contentResolver.unregisterContentObserver(htsrObserver)
        
        try {
            ActivityManager.getService().unregisterProcessObserver(processObserver)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister process observer", e)
        }
        writeNode("0")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateSettings() {
        isGlobalEnabled = prefs.getBoolean("htsr_global_enable", false)
        isPerAppEnabled = prefs.getBoolean("htsr_enable", false)

        if (isGlobalEnabled) {
            writeNode("1")
        } else if (!isPerAppEnabled) {
            writeNode("0")
        } else {
            val am = getSystemService(ActivityManager::class.java)
            val topPackage = try {
                am?.getRunningTasks(1)?.firstOrNull()?.topActivity?.packageName
            } catch (_: Exception) {
                null
            }
            val shouldEnable = topPackage != null && topPackage in htsrApps
            writeNode(if (shouldEnable) "1" else "0")
        }
    }
    
    private fun updateAppsList() {
        val raw = Settings.Secure.getString(contentResolver, HTSR_APPS_KEY)
        htsrApps = if (raw.isNullOrBlank()) emptySet() else raw.split(",").filter { it.isNotBlank() }.toSet()
    }
    
    private fun writeNode(value: String) {
        try {
            val file = File(NODE_PATH)
            if (file.exists()) {
                file.writeText(value)
                Log.i(TAG, "Wrote $value to HTSR node")
            } else {
                Log.w(TAG, "HTSR node does not exist: $NODE_PATH")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write $value to HTSR node: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "HTSRService"
        private const val HTSR_APPS_KEY = "htsr_apps"
        private const val NODE_PATH = "/sys/devices/virtual/touch/touch_dev/bump_sample_rate"
        
        fun startService(context: Context) {
            try {
                context.startService(Intent(context, HTSRService::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start HTSRService", e)
            }
        }
        
        fun stopService(context: Context) {
            try {
                context.stopService(Intent(context, HTSRService::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop HTSRService", e)
            }
        }
    }
}
