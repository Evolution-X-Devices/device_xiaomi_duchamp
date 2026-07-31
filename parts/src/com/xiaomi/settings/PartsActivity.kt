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
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity
import com.google.android.material.color.DynamicColors

class PartsActivity : CollapsingToolbarBaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)

        handleIntent(intent, savedInstanceState == null)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent, true)
    }

    private fun handleIntent(intent: Intent, shouldSwitch: Boolean) {
        val fragmentClass = intent.getStringExtra(EXTRA_SHOW_FRAGMENT)
        val fragmentTitle = intent.getCharSequenceExtra(EXTRA_SHOW_FRAGMENT_TITLE)
        val fragmentTitleResId = intent.getIntExtra(EXTRA_SHOW_FRAGMENT_TITLE_RESID, -1)
        val fragmentArgs = intent.getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGS)

        if (fragmentTitleResId != -1 && fragmentTitleResId != 0) {
            setTitle(fragmentTitleResId)
        } else if (!fragmentTitle.isNullOrEmpty()) {
            title = fragmentTitle
        }

        if (shouldSwitch) {
            val fragment = if (!fragmentClass.isNullOrEmpty() && fragmentClass.startsWith("com.xiaomi.settings")) {
                try {
                    val target = Fragment.instantiate(this, fragmentClass, null)
                    if (fragmentArgs != null) {
                        target.arguments = fragmentArgs
                    }
                    target
                } catch (e: Exception) {
                    Log.e("PartsActivity", "Failed to instantiate fragment: $fragmentClass", e)
                    PartsFragment()
                }
            } else {
                PartsFragment()
            }

            supportFragmentManager
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                    com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                    fragment
                )
                .commit()
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val fragmentClass = pref.fragment ?: return false
        val intent = Intent(this, PartsActivity::class.java).apply {
            putExtra(EXTRA_SHOW_FRAGMENT, fragmentClass)
            putExtra(EXTRA_SHOW_FRAGMENT_TITLE, pref.title)
            putExtra(EXTRA_SHOW_FRAGMENT_ARGS, pref.extras)
        }
        startActivity(intent)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val EXTRA_SHOW_FRAGMENT = ":settings:show_fragment"
        private const val EXTRA_SHOW_FRAGMENT_TITLE = ":settings:show_fragment_title"
        private const val EXTRA_SHOW_FRAGMENT_TITLE_RESID = ":settings:show_fragment_title_resid"
        private const val EXTRA_SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args"
    }
}

