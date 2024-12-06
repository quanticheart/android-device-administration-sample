package com.quanticheart.deviceadministration.fragments

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.quanticheart.deviceadministration.Constants.KEY_ACTIVATE_ENCRYPTION
import com.quanticheart.deviceadministration.Constants.KEY_CATEGORY_ENCRYPTION
import com.quanticheart.deviceadministration.Constants.KEY_REQUIRE_ENCRYPTION
import com.quanticheart.deviceadministration.Constants.REQUEST_CODE_START_ENCRYPTION
import com.quanticheart.deviceadministration.Constants.alertIfMonkey
import com.quanticheart.deviceadministration.R

class EncryptionFragment : AdminSampleFragment(), OnPreferenceChangeListener,
    OnPreferenceClickListener {
    private var mEncryptionCategory: PreferenceCategory? = null
    private var mRequireEncryption: CheckBoxPreference? = null
    private var mActivateEncryption: PreferenceScreen? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.device_admin_encryption)

        mEncryptionCategory = findPreference(KEY_CATEGORY_ENCRYPTION) as? PreferenceCategory
        mRequireEncryption = findPreference(KEY_REQUIRE_ENCRYPTION) as? CheckBoxPreference
        mActivateEncryption = findPreference(KEY_ACTIVATE_ENCRYPTION) as? PreferenceScreen

        mRequireEncryption!!.onPreferenceChangeListener = this
        mActivateEncryption!!.onPreferenceClickListener = this
    }

    override fun onResume() {
        super.onResume()
        mEncryptionCategory!!.isEnabled = mAdminActive
        mRequireEncryption!!.isChecked = mDPM!!.getStorageEncryption(mDeviceAdminSample)
    }

    /**
     * Update the summaries of each item to show the local setting and the global setting.
     */
    override fun reloadSummaries() {
        super.reloadSummaries()
        val local = mDPM!!.getStorageEncryption(mDeviceAdminSample)
        val global = mDPM!!.getStorageEncryption(null)
        mRequireEncryption!!.summary = localGlobalSummary(local, global)

        val deviceStatusCode = mDPM!!.storageEncryptionStatus
        val deviceStatus = statusCodeToString(deviceStatusCode)
        val status = mActivity!!.getString(R.string.status_device_encryption, deviceStatus)
        mActivateEncryption!!.summary = status
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (super.onPreferenceChange(preference, newValue)) {
            return true
        }
        if (preference === mRequireEncryption) {
            val newActive = newValue as Boolean
            mDPM!!.setStorageEncryption(mDeviceAdminSample!!, newActive)
            // Delay update because the change is only applied after exiting this method.
            postReloadSummaries()
            return true
        }
        return true
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (super.onPreferenceClick(preference)) {
            return true
        }
        if (preference === mActivateEncryption) {
            if (alertIfMonkey(mActivity, R.string.monkey_encryption)) {
                return true
            }
            // Check to see if encryption is even supported on this device (it's optional).
            if (mDPM!!.storageEncryptionStatus ==
                DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED
            ) {
                val builder = AlertDialog.Builder(mActivity)
                builder.setMessage(R.string.encryption_not_supported)
                builder.setPositiveButton(R.string.encryption_not_supported_ok, null)
                builder.show()
                return true
            }
            // Launch the activity to activate encryption.  May or may not return!
            val intent = Intent(DevicePolicyManager.ACTION_START_ENCRYPTION)
            startActivityForResult(intent, REQUEST_CODE_START_ENCRYPTION)
            return true
        }
        return false
    }

    private fun statusCodeToString(newStatusCode: Int): String {
        var newStatus = R.string.encryption_status_unknown
        when (newStatusCode) {
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> newStatus =
                R.string.encryption_status_unsupported

            DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE -> newStatus =
                R.string.encryption_status_inactive

            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING -> newStatus =
                R.string.encryption_status_activating

            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE -> newStatus =
                R.string.encryption_status_active
        }
        return mActivity!!.getString(newStatus)
    }
}
