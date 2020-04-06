package ir.firoozehcorp.burglaralarm.utils

import android.content.Context

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

}