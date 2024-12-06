package com.quanticheart.deviceadministration.fragments

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.quanticheart.deviceadministration.Constants.KEY_CATEGORY_LOCK_WIPE
import com.quanticheart.deviceadministration.Constants.KEY_LOCK_SCREEN
import com.quanticheart.deviceadministration.Constants.KEY_MAX_FAILS_BEFORE_WIPE
import com.quanticheart.deviceadministration.Constants.KEY_MAX_TIME_SCREEN_LOCK
import com.quanticheart.deviceadministration.Constants.KEY_WIPE_DATA
import com.quanticheart.deviceadministration.Constants.KEY_WIP_DATA_ALL
import com.quanticheart.deviceadministration.Constants.MS_PER_MINUTE
import com.quanticheart.deviceadministration.Constants.alertIfMonkey
import com.quanticheart.deviceadministration.R

class LockWipeFragment : AdminSampleFragment(), OnPreferenceChangeListener,
    OnPreferenceClickListener {
    private var mLockWipeCategory: PreferenceCategory? = null
    private var mMaxTimeScreenLock: EditTextPreference? = null
    private var mMaxFailures: EditTextPreference? = null
    private var mLockScreen: PreferenceScreen? = null
    private var mWipeData: PreferenceScreen? = null
    private var mWipeAppData: PreferenceScreen? = null


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.device_admin_lock_wipe)

        mLockWipeCategory = findPreference(KEY_CATEGORY_LOCK_WIPE) as? PreferenceCategory
        mMaxTimeScreenLock = findPreference(KEY_MAX_TIME_SCREEN_LOCK) as? EditTextPreference
        mMaxFailures = findPreference(KEY_MAX_FAILS_BEFORE_WIPE) as? EditTextPreference
        mLockScreen = findPreference(KEY_LOCK_SCREEN) as? PreferenceScreen
        mWipeData = findPreference(KEY_WIPE_DATA) as? PreferenceScreen
        mWipeAppData = findPreference(KEY_WIP_DATA_ALL) as? PreferenceScreen

        mMaxTimeScreenLock!!.onPreferenceChangeListener = this
        mMaxFailures!!.onPreferenceChangeListener = this
        mLockScreen!!.onPreferenceClickListener = this
        mWipeData!!.onPreferenceClickListener = this
        mWipeAppData!!.onPreferenceClickListener = this
    }

    override fun onResume() {
        super.onResume()
        mLockWipeCategory!!.isEnabled = mAdminActive
    }

    /**
     * Update the summaries of each item to show the local setting and the global setting.
     */
    override fun reloadSummaries() {
        super.reloadSummaries()
        val localLong = mDPM!!.getMaximumTimeToLock(mDeviceAdminSample)
        val globalLong = mDPM!!.getMaximumTimeToLock(null)
        mMaxTimeScreenLock!!.summary = localGlobalSummary(
            localLong / MS_PER_MINUTE,
            globalLong / MS_PER_MINUTE
        )
        val local = mDPM!!.getMaximumFailedPasswordsForWipe(mDeviceAdminSample)
        val global = mDPM!!.getMaximumFailedPasswordsForWipe(null)
        mMaxFailures!!.summary = localGlobalSummary(local, global)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (super.onPreferenceChange(preference, newValue)) {
            return true
        }
        val valueString = newValue as? String
        if (TextUtils.isEmpty(valueString)) {
            return false
        }
        var value = 0
        try {
            value = valueString?.toInt() ?: 0
        } catch (nfe: NumberFormatException) {
            val warning = mActivity!!.getString(R.string.number_format_warning, valueString)
            Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show()
        }
        if (preference === mMaxTimeScreenLock) {
            mDPM!!.setMaximumTimeToLock(mDeviceAdminSample, value * MS_PER_MINUTE)
        } else if (preference === mMaxFailures) {
            if (alertIfMonkey(mActivity, R.string.monkey_wipe_data)) {
                return true
            }
            mDPM!!.setMaximumFailedPasswordsForWipe(mDeviceAdminSample, value)
        }
        // Delay update because the change is only applied after exiting this method.
        postReloadSummaries()
        return true
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (super.onPreferenceClick(preference)) {
            return true
        }
        if (preference === mLockScreen) {
            if (alertIfMonkey(mActivity, R.string.monkey_lock_screen)) {
                return true
            }
            mDPM!!.lockNow()
            return true
        } else if (preference === mWipeData || preference === mWipeAppData) {
            if (alertIfMonkey(mActivity, R.string.monkey_wipe_data)) {
                return true
            }
            promptForRealDeviceWipe(preference === mWipeAppData)
            return true
        }
        return false
    }

    /**
     * Wiping data is real, so we don't want it to be easy.  Show two alerts before wiping.
     */
    private fun promptForRealDeviceWipe(wipeAllData: Boolean) {
        val activity = mActivity

        val builderRaw = AlertDialog.Builder(activity)
        builderRaw.setMessage(R.string.wipe_warning_first)
        builderRaw.setPositiveButton(
            R.string.wipe_warning_first_ok
        ) { _, _ ->
            val builder = AlertDialog.Builder(activity)
            if (wipeAllData) {
                builder.setMessage(R.string.wipe_warning_second_full)
            } else {
                builder.setMessage(R.string.wipe_warning_second)
            }
            builder.setPositiveButton(
                R.string.wipe_warning_second_ok
            ) { _, _ ->
                val stillActive = mActivity!!.isActiveAdmin
                if (stillActive) {
                    mDPM!!.wipeData(
                        if (wipeAllData)
                            DevicePolicyManager.WIPE_EXTERNAL_STORAGE
                        else
                            0
                    )
                }
            }
            builder.setNegativeButton(R.string.wipe_warning_second_no, null)
            builder.show()
        }
        builderRaw.setNegativeButton(R.string.wipe_warning_first_no, null)
        builderRaw.show()
    }
}
