package com.quanticheart.deviceadministration.fragments

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import com.quanticheart.deviceadministration.Constants.KEY_DISABLE_CAMERA
import com.quanticheart.deviceadministration.Constants.KEY_DISABLE_KEYGUARD_SECURE_CAMERA
import com.quanticheart.deviceadministration.Constants.KEY_DISABLE_KEYGUARD_WIDGETS
import com.quanticheart.deviceadministration.Constants.KEY_DISABLE_NOTIFICATIONS
import com.quanticheart.deviceadministration.Constants.KEY_DISABLE_TRUST_AGENTS
import com.quanticheart.deviceadministration.Constants.KEY_DISABLE_UNREDACTED
import com.quanticheart.deviceadministration.Constants.KEY_ENABLE_ADMIN
import com.quanticheart.deviceadministration.Constants.KEY_TRUST_AGENT_COMPONENT
import com.quanticheart.deviceadministration.Constants.KEY_TRUST_AGENT_FEATURES
import com.quanticheart.deviceadministration.Constants.REQUEST_CODE_ENABLE_ADMIN
import com.quanticheart.deviceadministration.Constants.TAG
import com.quanticheart.deviceadministration.R

class GeneralFragment : AdminSampleFragment(), OnPreferenceChangeListener {
    // UI elements
    private var mEnableCheckbox: CheckBoxPreference? = null
    private var mDisableCameraCheckbox: CheckBoxPreference? = null
    private var mDisableKeyguardWidgetsCheckbox: CheckBoxPreference? = null
    private var mDisableKeyguardSecureCameraCheckbox: CheckBoxPreference? = null
    private var mDisableKeyguardNotificationCheckbox: CheckBoxPreference? = null
    private var mDisableKeyguardTrustAgentCheckbox: CheckBoxPreference? = null
    private var mDisableKeyguardUnredactedCheckbox: CheckBoxPreference? = null
    private var mTrustAgentComponent: EditTextPreference? = null
    private var mTrustAgentFeatures: EditTextPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.device_admin_general)
        mEnableCheckbox = findPreference(KEY_ENABLE_ADMIN) as? CheckBoxPreference
        mEnableCheckbox!!.onPreferenceChangeListener = this

        mDisableCameraCheckbox = findPreference(KEY_DISABLE_CAMERA) as? CheckBoxPreference
        mDisableCameraCheckbox!!.onPreferenceChangeListener = this

        mDisableKeyguardWidgetsCheckbox =
            findPreference(KEY_DISABLE_KEYGUARD_WIDGETS) as? CheckBoxPreference
        mDisableKeyguardWidgetsCheckbox!!.onPreferenceChangeListener = this

        mDisableKeyguardSecureCameraCheckbox =
            findPreference(KEY_DISABLE_KEYGUARD_SECURE_CAMERA) as? CheckBoxPreference
        mDisableKeyguardSecureCameraCheckbox!!.onPreferenceChangeListener = this

        mDisableKeyguardNotificationCheckbox =
            findPreference(KEY_DISABLE_NOTIFICATIONS) as? CheckBoxPreference
        mDisableKeyguardNotificationCheckbox!!.onPreferenceChangeListener = this

        mDisableKeyguardUnredactedCheckbox =
            findPreference(KEY_DISABLE_UNREDACTED) as? CheckBoxPreference
        mDisableKeyguardUnredactedCheckbox!!.onPreferenceChangeListener = this

        mDisableKeyguardTrustAgentCheckbox =
            findPreference(KEY_DISABLE_TRUST_AGENTS) as? CheckBoxPreference
        mDisableKeyguardTrustAgentCheckbox!!.onPreferenceChangeListener = this

        mTrustAgentComponent = findPreference(KEY_TRUST_AGENT_COMPONENT) as? EditTextPreference
        mTrustAgentComponent!!.onPreferenceChangeListener = this

        mTrustAgentFeatures = findPreference(KEY_TRUST_AGENT_FEATURES) as? EditTextPreference
        mTrustAgentFeatures!!.onPreferenceChangeListener = this
    }

    // At onResume time, reload UI with current values as? required
    override fun onResume() {
        super.onResume()
        mEnableCheckbox!!.isChecked = mAdminActive
        enableDeviceCapabilitiesArea(mAdminActive)

        if (mAdminActive) {
            mDPM!!.setCameraDisabled(mDeviceAdminSample, mDisableCameraCheckbox!!.isChecked)
            mDPM!!.setKeyguardDisabledFeatures(mDeviceAdminSample, createKeyguardDisabledFlag())
            reloadSummaries()
        }
    }

    fun createKeyguardDisabledFlag(): Int {
        var flags = DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE
        flags =
            flags or if (mDisableKeyguardWidgetsCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL else 0
        flags =
            flags or if (mDisableKeyguardSecureCameraCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA else 0
        flags =
            flags or if (mDisableKeyguardNotificationCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS else 0
        flags =
            flags or if (mDisableKeyguardUnredactedCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS else 0
        flags =
            flags or if (mDisableKeyguardTrustAgentCheckbox!!.isChecked) DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS else 0
        return flags
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (super.onPreferenceChange(preference, newValue)) {
            return true
        }
        if (preference === mEnableCheckbox) {
            val value = newValue as? Boolean
            if (value != mAdminActive) {
                if (value == true) {
                    // Launch the activity to have the user enable our admin.
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample)
                    intent.putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        mActivity!!.getString(R.string.add_admin_extra_app_text)
                    )
                    startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
                    // return false - don't update checkbox until we're really active
                    return false
                } else {
                    mDPM!!.removeActiveAdmin(mDeviceAdminSample!!)
                    enableDeviceCapabilitiesArea(false)
                    mAdminActive = false
                }
            }
        } else if (preference === mDisableCameraCheckbox) {
            val value = newValue as? Boolean
            mDPM!!.setCameraDisabled(mDeviceAdminSample, value == true)
            // Delay update because the change is only applied after exiting this method.
            postReloadSummaries()
        } else if (preference === mDisableKeyguardWidgetsCheckbox || preference === mDisableKeyguardSecureCameraCheckbox || preference === mDisableKeyguardNotificationCheckbox || preference === mDisableKeyguardUnredactedCheckbox || preference === mDisableKeyguardTrustAgentCheckbox || preference === mTrustAgentComponent || preference === mTrustAgentFeatures) {
            postUpdateDpmDisableFeatures()
            postReloadSummaries()
        }
        return true
    }

    private fun postUpdateDpmDisableFeatures() {
        requireView().post {
            mDPM!!.setKeyguardDisabledFeatures(
                mDeviceAdminSample, createKeyguardDisabledFlag()
            )
            val component = mTrustAgentComponent!!.text
            if (component != null) {
                val agent = ComponentName.unflattenFromString(component)
                if (agent != null) {
                    val featureString = mTrustAgentFeatures!!.text
                    if (featureString != null) {
                        val bundle = PersistableBundle()
                        bundle.putStringArray("features",
                            featureString.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray())
                        mDPM!!.setTrustAgentConfiguration(mDeviceAdminSample, agent, bundle)
                    }
                } else {
                    Log.w(
                        TAG, "Invalid component: $component"
                    )
                }
            }
        }
    }

    override fun reloadSummaries() {
        super.reloadSummaries()
        val cameraSummary = getString(
            if (mDPM!!.getCameraDisabled(mDeviceAdminSample)) R.string.camera_disabled
            else R.string.camera_enabled
        )
        mDisableCameraCheckbox!!.summary = cameraSummary

        val disabled = mDPM!!.getKeyguardDisabledFeatures(mDeviceAdminSample)

        val keyguardWidgetSummary = getString(
            if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL) != 0) R.string.keyguard_widgets_disabled else R.string.keyguard_widgets_enabled
        )
        mDisableKeyguardWidgetsCheckbox!!.summary = keyguardWidgetSummary

        val keyguardSecureCameraSummary = getString(
            if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA) != 0) R.string.keyguard_secure_camera_disabled else R.string.keyguard_secure_camera_enabled
        )
        mDisableKeyguardSecureCameraCheckbox!!.summary = keyguardSecureCameraSummary

        val keyguardSecureNotificationsSummary = getString(
            if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS) != 0) R.string.keyguard_secure_notifications_disabled
            else R.string.keyguard_secure_notifications_enabled
        )
        mDisableKeyguardNotificationCheckbox!!.summary = keyguardSecureNotificationsSummary

        val keyguardUnredactedSummary = getString(
            if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS) != 0) R.string.keyguard_unredacted_notifications_disabled
            else R.string.keyguard_unredacted_notifications_enabled
        )
        mDisableKeyguardUnredactedCheckbox!!.summary = keyguardUnredactedSummary

        val keyguardEnableTrustAgentSummary = getString(
            if ((disabled and DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS) != 0) R.string.keyguard_trust_agents_disabled
            else R.string.keyguard_trust_agents_enabled
        )
        mDisableKeyguardTrustAgentCheckbox!!.summary = keyguardEnableTrustAgentSummary

        val prefs = preferenceManager.sharedPreferences
        val trustDisabled = (disabled and DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS) != 0
        val component = prefs?.getString(mTrustAgentComponent!!.key, null)
        mTrustAgentComponent!!.summary = component
        mTrustAgentComponent!!.isEnabled = trustDisabled

        val features = prefs?.getString(mTrustAgentFeatures!!.key, null)
        mTrustAgentFeatures!!.summary = features
        mTrustAgentFeatures!!.isEnabled = trustDisabled
    }

    /** Updates the device capabilities area (dis/enabling) as? the admin is (de)activated  */
    private fun enableDeviceCapabilitiesArea(enabled: Boolean) {
        mDisableCameraCheckbox!!.isEnabled = enabled
        mDisableKeyguardWidgetsCheckbox!!.isEnabled = enabled
        mDisableKeyguardSecureCameraCheckbox!!.isEnabled = enabled
        mDisableKeyguardNotificationCheckbox!!.isEnabled = enabled
        mDisableKeyguardUnredactedCheckbox!!.isEnabled = enabled
        mDisableKeyguardTrustAgentCheckbox!!.isEnabled = enabled
        mTrustAgentComponent!!.isEnabled = enabled
        mTrustAgentFeatures!!.isEnabled = enabled
    }
}
