package com.quanticheart.deviceadministration.fragments

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.quanticheart.deviceadministration.Constants
import com.quanticheart.deviceadministration.R
import kotlin.math.abs

class ExpirationFragment : AdminSampleFragment(), OnPreferenceChangeListener,
    OnPreferenceClickListener {
    private var mExpirationCategory: PreferenceCategory? = null
    private var mHistory: EditTextPreference? = null
    private var mExpirationTimeout: EditTextPreference? = null
    private var mExpirationStatus: PreferenceScreen? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.device_admin_expiration)

        mExpirationCategory =
            findPreference(Constants.KEY_CATEGORY_EXPIRATION) as? PreferenceCategory
        mHistory = findPreference(Constants.KEY_HISTORY) as? EditTextPreference
        mExpirationTimeout = findPreference(Constants.KEY_EXPIRATION_TIMEOUT) as? EditTextPreference
        mExpirationStatus = findPreference(Constants.KEY_EXPIRATION_STATUS) as? PreferenceScreen

        mHistory!!.onPreferenceChangeListener = this
        mExpirationTimeout!!.onPreferenceChangeListener = this
        mExpirationStatus!!.onPreferenceClickListener = this
    }

    override fun onResume() {
        super.onResume()
        mExpirationCategory!!.isEnabled = mAdminActive
    }

    /**
     * Update the summaries of each item to show the local setting and the global setting.
     */
    override fun reloadSummaries() {
        super.reloadSummaries()
        val local = mDPM!!.getPasswordHistoryLength(mDeviceAdminSample)
        val global = mDPM!!.getPasswordHistoryLength(null)
        mHistory!!.summary = localGlobalSummary(local, global)
        val localLong = mDPM!!.getPasswordExpirationTimeout(mDeviceAdminSample)
        val globalLong = mDPM!!.getPasswordExpirationTimeout(null)
        mExpirationTimeout!!.summary = localGlobalSummary(
            localLong / Constants.MS_PER_MINUTE,
            globalLong / Constants.MS_PER_MINUTE
        )

        val expirationStatus = expirationStatus
        mExpirationStatus!!.summary = expirationStatus
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (super.onPreferenceChange(preference, newValue)) {
            return true
        }
        val valueString = newValue as String
        if (TextUtils.isEmpty(valueString)) {
            return false
        }
        var value = 0
        try {
            value = valueString.toInt()
        } catch (nfe: NumberFormatException) {
            val warning = mActivity!!.getString(R.string.number_format_warning, valueString)
            Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show()
        }
        if (preference === mHistory) {
            mDPM!!.setPasswordHistoryLength(mDeviceAdminSample!!, value)
        } else if (preference === mExpirationTimeout) {
            mDPM!!.setPasswordExpirationTimeout(mDeviceAdminSample, value * Constants.MS_PER_MINUTE)
        }
        // Delay update because the change is only applied after exiting this method.
        postReloadSummaries()
        return true
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (super.onPreferenceClick(preference)) {
            return true
        }
        if (preference === mExpirationStatus) {
            val expirationStatus = expirationStatus
            mExpirationStatus!!.summary = expirationStatus
            return true
        }
        return false
    }

    private val expirationStatus: String
        /**
         * Create a summary string describing the expiration status for the sample app,
         * as well as the global (aggregate) status.
         */
        get() {
            // expirations are absolute;  convert to relative for display
            var localExpiration = mDPM!!.getPasswordExpiration(mDeviceAdminSample)
            var globalExpiration = mDPM!!.getPasswordExpiration(null)
            val now = System.currentTimeMillis()

            // local expiration
            val local: String
            if (localExpiration == 0L) {
                local = mActivity!!.getString(R.string.expiration_status_none)
            } else {
                localExpiration -= now
                val dms = Constants.timeToDaysMinutesSeconds(
                    mActivity!!,
                    abs(localExpiration.toDouble()).toLong()
                )
                local = if (localExpiration >= 0) {
                    mActivity!!.getString(R.string.expiration_status_future, dms)
                } else {
                    mActivity!!.getString(R.string.expiration_status_past, dms)
                }
            }

            // global expiration
            val global: String
            if (globalExpiration == 0L) {
                global = mActivity!!.getString(R.string.expiration_status_none)
            } else {
                globalExpiration -= now
                val dms = Constants.timeToDaysMinutesSeconds(
                    mActivity!!,
                    abs(globalExpiration.toDouble()).toLong()
                )
                global = if (globalExpiration >= 0) {
                    mActivity!!.getString(R.string.expiration_status_future, dms)
                } else {
                    mActivity!!.getString(R.string.expiration_status_past, dms)
                }
            }
            return mActivity!!.getString(R.string.status_local_global, local, global)
        }
}
