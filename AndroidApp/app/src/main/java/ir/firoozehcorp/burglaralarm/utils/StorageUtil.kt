package ir.firoozehcorp.burglaralarm.utils

import android.content.Context
import android.provider.Settings

object StorageUtil {

    private const val PrefName = "data"

    fun isSetSettingsBefore(context: Context): Boolean {
        context
            .getSharedPreferences(PrefName, Context.MODE_PRIVATE)
            .apply {
                return getBoolean("set_before", false)
            }
    }

    fun setSettingsOk(isOk: Boolean, context: Context) {
        context
            .getSharedPreferences(PrefName, Context.MODE_PRIVATE)
            .edit()
            .apply {
                putBoolean("set_before", isOk)
                apply()
            }
    }


    fun isAlarmEnable(context: Context): Boolean {
        context
            .getSharedPreferences(PrefName, Context.MODE_PRIVATE)
            .apply {
                return getBoolean("is_enable", false)
            }
    }

    fun setAlarmStatus(isEnable: Boolean, context: Context) {
        context
            .getSharedPreferences(PrefName, Context.MODE_PRIVATE)
            .edit()
            .apply {
                putBoolean("is_enable", isEnable)
                apply()
            }
    }

    fun getDeviceID(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

}