package ir.firoozehcorp.burglaralarm.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import ir.firoozehcorp.burglaralarm.R


object NotificationUtil {

    private var mManager: NotificationManager? = null
    private const val ANDROID_CHANNEL_ID = "ir.firoozehcorp.burglaralarm.ANDROID"
    private const val ANDROID_CHANNEL_NAME = "Burglar Alarm Notification"
    private const val NOTI_ID = 12121213


    fun createNotification(context: Context, isEnable: Boolean) {

        mManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ANDROID_CHANNEL_ID,
                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.enableLights(true)
            channel.importance = NotificationManager.IMPORTANCE_HIGH
            channel.enableVibration(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            mManager?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Burglar Alarm")
            .setPriority(Notification.PRIORITY_MAX)
            .setChannelId(ANDROID_CHANNEL_ID)
            .setOnlyAlertOnce(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))

        val noti: Notification
        if (isEnable) {
            builder.setContentText("Danger : Activity Detected")
            noti = builder.build()

            noti.sound = Uri.parse(
                "android.resource://"
                        + context.packageName + "/" + R.raw.alarm
            )
        } else {
            builder.setContentText("Burglar Alarm Activated")
            noti = builder.build()
            noti.defaults = noti.defaults or Notification.DEFAULT_SOUND
            noti.defaults = noti.defaults or Notification.DEFAULT_VIBRATE
        }

        mManager?.notify(NOTI_ID, noti)
    }

    fun disableNotification(context: Context) {
        mManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mManager?.cancelAll()
    }

}