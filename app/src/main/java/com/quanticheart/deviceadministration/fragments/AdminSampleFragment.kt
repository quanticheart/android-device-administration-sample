@file:Suppress("KotlinConstantConditions")

package com.quanticheart.deviceadministration.fragments

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.quanticheart.deviceadministration.Constants.KEY_RESET_PASSWORD
import com.quanticheart.deviceadministration.Constants.KEY_SET_PASSWORD
import com.quanticheart.deviceadministration.Constants.alertIfMonkey
import com.quanticheart.deviceadministration.MainActivity
import com.quanticheart.deviceadministration.R

abstract class AdminSampleFragment : PreferenceFragmentCompat(), OnPreferenceChangeListener,
    OnPreferenceClickListener {
    // Useful instance variables
    protected var mActivity: MainActivity? = null
    protected var mDPM: DevicePolicyManager? = null
    protected var mDeviceAdminSample: ComponentName? = null
    protected var mAdminActive: Boolean = false

    // Optional shared UI
    private var mSetPassword: PreferenceScreen? = null
    private var mResetPassword: EditTextPreference? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Retrieve the useful instance variables
        mActivity = activity as MainActivity
        mDPM = mActivity!!.mDPM
        mDeviceAdminSample = mActivity!!.mDeviceAdminSample
        mAdminActive = mActivity!!.isActiveAdmin

        // Configure the shared UI elements (if they exist)
        mResetPassword = findPreference(KEY_RESET_PASSWORD) as? EditTextPreference
        mSetPassword = findPreference(KEY_SET_PASSWORD) as? PreferenceScreen

        if (mResetPassword != null) {
            mResetPassword!!.onPreferenceChangeListener = this
        }
        if (mSetPassword != null) {
            mSetPassword!!.onPreferenceClickListener = this
        }
    }

    override fun onResume() {
        super.onResume()
        mAdminActive = mActivity!!.isActiveAdmin
        reloadSummaries()
        // Resetting the password via API is available only to active admins
        if (mResetPassword != null) {
            mResetPassword!!.isEnabled = mAdminActive
        }
    }

    /**
     * Called automatically at every onResume.  Should also call explicitly any time a
     * policy changes that may affect other policy values.
     */
    protected open fun reloadSummaries() {
        if (mSetPassword != null) {
            if (mAdminActive) {
                // Show password-sufficient status under Set Password button
                val sufficient = mDPM!!.isActivePasswordSufficient
                mSetPassword!!.setSummary(if (sufficient) R.string.password_sufficient else R.string.password_insufficient)
            } else {
                mSetPassword!!.summary = null
            }
        }
    }

    protected fun postReloadSummaries() {
        view!!.post { reloadSummaries() }
    }


    /**
     * This is dangerous, so we prevent automated tests from doing it, and we
     * remind the user after we do it.
     */
    private fun doResetPassword(newPassword: String) {
        if (alertIfMonkey(mActivity, R.string.monkey_reset_password)) {
            return
        }
        mDPM!!.resetPassword(newPassword, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY)
        val builder = AlertDialog.Builder(mActivity)
        val message = mActivity!!.getString(R.string.reset_password_warning, newPassword)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.reset_password_ok, null)
        builder.show()
    }

    /**
     * Simple helper for summaries showing local & global (aggregate) policy settings
     */
    protected fun localGlobalSummary(local: Any?, global: Any?): String {
        return getString(R.string.status_local_global, local, global)
    }

    override fun onPreferenceClick(preference: androidx.preference.Preference): Boolean {
        if ((mSetPassword != null) && (preference == mSetPassword)) {
            val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
            startActivity(intent)
            return true
        }
        return false
    }

    override fun onPreferenceChange(
        preference: androidx.preference.Preference,
        newValue: Any?
    ): Boolean {
        if (mResetPassword != null && preference == mResetPassword) {
            doResetPassword(newValue as String)
            return true
        }
        return false
    }
}
