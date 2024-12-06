package com.quanticheart.deviceadministration.fragments

import android.app.admin.DevicePolicyManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceCategory
import com.quanticheart.deviceadministration.Constants.KEY_CATEGORY_QUALITY
import com.quanticheart.deviceadministration.Constants.KEY_MIN_LENGTH
import com.quanticheart.deviceadministration.Constants.KEY_MIN_LETTERS
import com.quanticheart.deviceadministration.Constants.KEY_MIN_LOWER_CASE
import com.quanticheart.deviceadministration.Constants.KEY_MIN_NON_LETTER
import com.quanticheart.deviceadministration.Constants.KEY_MIN_NUMERIC
import com.quanticheart.deviceadministration.Constants.KEY_MIN_SYMBOLS
import com.quanticheart.deviceadministration.Constants.KEY_MIN_UPPER_CASE
import com.quanticheart.deviceadministration.Constants.KEY_QUALITY
import com.quanticheart.deviceadministration.R

class QualityFragment : AdminSampleFragment(), OnPreferenceChangeListener {
    // UI elements
    private var mQualityCategory: PreferenceCategory? = null
    private var mPasswordQuality: ListPreference? = null
    private var mMinLength: EditTextPreference? = null
    private var mMinLetters: EditTextPreference? = null
    private var mMinNumeric: EditTextPreference? = null
    private var mMinLowerCase: EditTextPreference? = null
    private var mMinUpperCase: EditTextPreference? = null
    private var mMinSymbols: EditTextPreference? = null
    private var mMinNonLetter: EditTextPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.device_admin_quality)

        mQualityCategory = findPreference(KEY_CATEGORY_QUALITY) as? PreferenceCategory
        mPasswordQuality = findPreference(KEY_QUALITY) as? ListPreference
        mMinLength = findPreference(KEY_MIN_LENGTH) as? EditTextPreference
        mMinLetters = findPreference(KEY_MIN_LETTERS) as? EditTextPreference
        mMinNumeric = findPreference(KEY_MIN_NUMERIC) as? EditTextPreference
        mMinLowerCase = findPreference(KEY_MIN_LOWER_CASE) as? EditTextPreference
        mMinUpperCase = findPreference(KEY_MIN_UPPER_CASE) as? EditTextPreference
        mMinSymbols = findPreference(KEY_MIN_SYMBOLS) as? EditTextPreference
        mMinNonLetter = findPreference(KEY_MIN_NON_LETTER) as? EditTextPreference

        mPasswordQuality!!.onPreferenceChangeListener = this
        mMinLength!!.onPreferenceChangeListener = this
        mMinLetters!!.onPreferenceChangeListener = this
        mMinNumeric!!.onPreferenceChangeListener = this
        mMinLowerCase!!.onPreferenceChangeListener = this
        mMinUpperCase!!.onPreferenceChangeListener = this
        mMinSymbols!!.onPreferenceChangeListener = this
        mMinNonLetter!!.onPreferenceChangeListener = this

        // Finish setup of the quality dropdown
        mPasswordQuality!!.entryValues = mPasswordQualityValueStrings
    }

    override fun onResume() {
        super.onResume()
        mQualityCategory!!.isEnabled = mAdminActive
    }

    /**
     * Update the summaries of each item to show the local setting and the global setting.
     */
    override fun reloadSummaries() {
        super.reloadSummaries()
        // Show numeric settings for each policy API
        var local = mDPM!!.getPasswordQuality(mDeviceAdminSample)
        var global = mDPM!!.getPasswordQuality(null)
        mPasswordQuality!!.summary =
            localGlobalSummary(qualityValueToString(local), qualityValueToString(global))
        local = mDPM!!.getPasswordMinimumLength(mDeviceAdminSample)
        global = mDPM!!.getPasswordMinimumLength(null)
        mMinLength!!.summary = localGlobalSummary(local, global)
        local = mDPM!!.getPasswordMinimumLetters(mDeviceAdminSample)
        global = mDPM!!.getPasswordMinimumLetters(null)
        mMinLetters!!.summary = localGlobalSummary(local, global)
        local = mDPM!!.getPasswordMinimumNumeric(mDeviceAdminSample)
        global = mDPM!!.getPasswordMinimumNumeric(null)
        mMinNumeric!!.summary = localGlobalSummary(local, global)
        local = mDPM!!.getPasswordMinimumLowerCase(mDeviceAdminSample)
        global = mDPM!!.getPasswordMinimumLowerCase(null)
        mMinLowerCase!!.summary = localGlobalSummary(local, global)
        local = mDPM!!.getPasswordMinimumUpperCase(mDeviceAdminSample)
        global = mDPM!!.getPasswordMinimumUpperCase(null)
        mMinUpperCase!!.summary = localGlobalSummary(local, global)
        local = mDPM!!.getPasswordMinimumSymbols(mDeviceAdminSample)
        global = mDPM!!.getPasswordMinimumSymbols(null)
        mMinSymbols!!.summary = localGlobalSummary(local, global)
        local = mDPM!!.getPasswordMinimumNonLetter(mDeviceAdminSample)
        global = mDPM!!.getPasswordMinimumNonLetter(null)
        mMinNonLetter!!.summary = localGlobalSummary(local, global)
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
        if (preference === mPasswordQuality) {
            mDPM!!.setPasswordQuality(mDeviceAdminSample!!, value)
        } else if (preference === mMinLength) {
            mDPM!!.setPasswordMinimumLength(mDeviceAdminSample!!, value)
        } else if (preference === mMinLetters) {
            mDPM!!.setPasswordMinimumLetters(mDeviceAdminSample!!, value)
        } else if (preference === mMinNumeric) {
            mDPM!!.setPasswordMinimumNumeric(mDeviceAdminSample!!, value)
        } else if (preference === mMinLowerCase) {
            mDPM!!.setPasswordMinimumLowerCase(mDeviceAdminSample!!, value)
        } else if (preference === mMinUpperCase) {
            mDPM!!.setPasswordMinimumUpperCase(mDeviceAdminSample!!, value)
        } else if (preference === mMinSymbols) {
            mDPM!!.setPasswordMinimumSymbols(mDeviceAdminSample!!, value)
        } else if (preference === mMinNonLetter) {
            mDPM!!.setPasswordMinimumNonLetter(mDeviceAdminSample!!, value)
        }
        // Delay update because the change is only applied after exiting this method.
        postReloadSummaries()
        return true
    }

    private fun qualityValueToString(quality: Int): String {
        for (i in mPasswordQualityValues.indices) {
            if (mPasswordQualityValues[i] == quality) {
                val qualities =
                    mActivity!!.resources.getStringArray(R.array.password_qualities)
                return qualities[i]
            }
        }
        return "(0x" + quality.toString(16) + ")"
    }

    companion object {
        // Password quality values
        // This list must match the list found in samples/ApiDemos/res/values/arrays.xml
        val mPasswordQualityValues: IntArray = intArrayOf(
            DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED,
            DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX,
            DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC,
            DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC,
            DevicePolicyManager.PASSWORD_QUALITY_COMPLEX
        )

        // Password quality values (as? strings, for the ListPreference entryValues)
        // This list must match the list found in samples/ApiDemos/res/values/arrays.xml
        val mPasswordQualityValueStrings: Array<String> = arrayOf(
            DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED.toString(),
            DevicePolicyManager.PASSWORD_QUALITY_SOMETHING.toString(),
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC.toString(),
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX.toString(),
            DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC.toString(),
            DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC.toString(),
            DevicePolicyManager.PASSWORD_QUALITY_COMPLEX.toString()
        )
    }
}
