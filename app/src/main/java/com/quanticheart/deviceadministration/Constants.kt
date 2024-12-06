package com.quanticheart.deviceadministration

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context

object Constants  {
        // Miscellaneous utilities and definitions
       const val TAG = "DeviceAdminSample"

       const val REQUEST_CODE_ENABLE_ADMIN = 1
       const val REQUEST_CODE_START_ENCRYPTION = 2

       const val MS_PER_MINUTE = (60 * 1000).toLong()
       const val MS_PER_HOUR = 60 * MS_PER_MINUTE
       const val MS_PER_DAY = 24 * MS_PER_HOUR

        // The following keys are used to find each preference item
       const val KEY_ENABLE_ADMIN = "key_enable_admin"
       const val KEY_DISABLE_CAMERA = "key_disable_camera"
       const val KEY_DISABLE_NOTIFICATIONS = "key_disable_notifications"
       const val KEY_DISABLE_UNREDACTED = "key_disable_unredacted"
       const val KEY_DISABLE_TRUST_AGENTS = "key_disable_trust_agents"
       const val KEY_TRUST_AGENT_COMPONENT = "key_trust_agent_component"
       const val KEY_TRUST_AGENT_FEATURES = "key_trust_agent_features"
       const val KEY_DISABLE_KEYGUARD_WIDGETS = "key_disable_keyguard_widgets"
       const val KEY_DISABLE_KEYGUARD_SECURE_CAMERA = "key_disable_keyguard_secure_camera"

       const val KEY_CATEGORY_QUALITY = "key_category_quality"
       const val KEY_SET_PASSWORD = "key_set_password"
       const val KEY_RESET_PASSWORD = "key_reset_password"
       const val KEY_QUALITY = "key_quality"
       const val KEY_MIN_LENGTH = "key_minimum_length"
       const val KEY_MIN_LETTERS = "key_minimum_letters"
       const val KEY_MIN_NUMERIC = "key_minimum_numeric"
       const val KEY_MIN_LOWER_CASE = "key_minimum_lower_case"
       const val KEY_MIN_UPPER_CASE = "key_minimum_upper_case"
       const val KEY_MIN_SYMBOLS = "key_minimum_symbols"
       const val KEY_MIN_NON_LETTER = "key_minimum_non_letter"

       const val KEY_CATEGORY_EXPIRATION = "key_category_expiration"
       const val KEY_HISTORY = "key_history"
       const val KEY_EXPIRATION_TIMEOUT = "key_expiration_timeout"
       const val KEY_EXPIRATION_STATUS = "key_expiration_status"

       const val KEY_CATEGORY_LOCK_WIPE = "key_category_lock_wipe"
       const val KEY_MAX_TIME_SCREEN_LOCK = "key_max_time_screen_lock"
       const val KEY_MAX_FAILS_BEFORE_WIPE = "key_max_fails_before_wipe"
       const val KEY_LOCK_SCREEN = "key_lock_screen"
       const val KEY_WIPE_DATA = "key_wipe_data"
       const val KEY_WIP_DATA_ALL = "key_wipe_data_all"

       const val KEY_CATEGORY_ENCRYPTION = "key_category_encryption"
       const val KEY_REQUIRE_ENCRYPTION = "key_require_encryption"
       const val KEY_ACTIVATE_ENCRYPTION = "key_activate_encryption"

        /**
         * Simple converter used for long expiration times reported in mSec.
         */
       fun timeToDaysMinutesSeconds(context: Context, time: Long): String {
            val days = time / MS_PER_DAY
            val hours = (time / MS_PER_HOUR) % 24
            val minutes = (time / MS_PER_MINUTE) % 60
            return context.getString(R.string.status_days_hours_minutes, days, hours, minutes)
        }

        /**
         * If the "user" is a monkey, post an alert and notify the caller.  This prevents automated
         * test frameworks from stumbling into annoying or dangerous operations.
         */
       fun alertIfMonkey(context: Context?, stringId: Int): Boolean {
            if (ActivityManager.isUserAMonkey()) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage(stringId)
                builder.setPositiveButton(R.string.monkey_ok, null)
                builder.show()
                return true
            } else {
                return false
            }
        }
    }