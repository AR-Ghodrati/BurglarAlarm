package ir.firoozehcorp.burglaralarm.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.text.TextUtils
import android.util.Log


object NetworkUtil {

    fun getCurrentSsid(context: Context): String? {
        var ssid: String? = null
        val connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        networkInfo?.let {
            if (networkInfo.isConnected) {
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val connectionInfo = wifiManager.connectionInfo
                if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.ssid)) {
                    ssid = connectionInfo.ssid.removePrefix("\"").removeSuffix("\"")
                    Log.e(javaClass.name, "SSID : $ssid")
                }
            }
            return ssid
        }
        return null
    }
}