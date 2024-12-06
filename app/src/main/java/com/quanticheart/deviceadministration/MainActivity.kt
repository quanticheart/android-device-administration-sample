package com.quanticheart.deviceadministration

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Bundle
import android.preference.PreferenceActivity
import com.quanticheart.deviceadministration.fragments.EncryptionFragment
import com.quanticheart.deviceadministration.fragments.ExpirationFragment
import com.quanticheart.deviceadministration.fragments.GeneralFragment
import com.quanticheart.deviceadministration.fragments.LockWipeFragment
import com.quanticheart.deviceadministration.fragments.QualityFragment

class MainActivity : PreferenceActivity() {
    // Interaction with the DevicePolicyManager
    var mDPM: DevicePolicyManager? = null
    var mDeviceAdminSample: ComponentName? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prepare to work with the DPM
        mDPM = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mDeviceAdminSample = ComponentName(this, DeviceAdminSampleReceiver::class.java)
    }

    /**
     * We override this method to provide PreferenceActivity with the top-level preference headers.
     */
    override fun onBuildHeaders(target: List<Header>) {
        loadHeadersFromResource(R.xml.device_admin_headers, target)
    }

    val isActiveAdmin: Boolean
        /**
         * Helper to determine if we are an active admin
         */
        get() = mDPM!!.isAdminActive(mDeviceAdminSample!!)

    override fun isValidFragment(fragmentName: String): Boolean {
        return GeneralFragment::class.java.name == fragmentName
                || QualityFragment::class.java.name == fragmentName
                || ExpirationFragment::class.java.name == fragmentName
                || LockWipeFragment::class.java.name == fragmentName
                || EncryptionFragment::class.java.name == fragmentName
    }

}