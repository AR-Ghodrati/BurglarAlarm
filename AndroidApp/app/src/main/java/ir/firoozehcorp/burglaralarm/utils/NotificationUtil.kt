package ir.firoozehcorp.burglaralarm.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import ir.firoozehcorp.burglaralarm.R


object NotificationUtil {

    private var mManager: NotificationManager? = null
    private const val ANDROID_CHANNEL_ID = "ir.firoozehcorp.burglaralarm.ANDROID"
    private const val ANDROID_CHANNEL_NAME = "Burglar Alarm Notification"
    private const val NOTI_IDEL_ID = 12121213
    private const val NOTI_ALARM_ID = 12121214


    fun createNotification(context: Context, isEnable: Boolean) {

        mManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ANDROID_CHANNEL_ID,
                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            )

            // Creating an Audio Attribute
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            channel.enableLights(true)
            channel.setSound(
                Uri.parse("android.resource://" + context.packageName.toString() + "/raw/alarm.mp3"),
                audioAttributes
            )
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
            .setOngoing(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))

        val noti: Notification
        if (isEnable) {
            builder.setContentText("Danger : Activity Detected")
            noti = builder.build()

            noti.sound =
                Uri.parse("android.resource://" + context.packageName.toString() + "/" + R.raw.alarm)

            noti.defaults =
                Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE


            disableNotification(context, true)
            mManager?.notify(NOTI_ALARM_ID, noti)
        } else {
            builder.setContentText("Burglar Alarm Activated")
            noti = builder.build()
            noti.defaults = noti.defaults or Notification.DEFAULT_SOUND
            noti.defaults = noti.defaults or Notification.DEFAULT_VIBRATE

            disableNotification(context, false)
            mManager?.notify(NOTI_IDEL_ID, noti)
        }

    }

    fun disableNotification(context: Context, isAlarm: Boolean) {
        mManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (isAlarm) mManager?.cancel(NOTI_IDEL_ID)
        else mManager?.cancel(NOTI_ALARM_ID)
    }

    fun disableNotification(context: Context) {
        mManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mManager?.cancelAll()
    }

}