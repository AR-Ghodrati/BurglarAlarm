package ir.firoozehcorp.burglaralarm.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import ir.firoozehcorp.burglaralarm.listeners.ServerApiListener
import ir.firoozehcorp.burglaralarm.models.ApiResponse
import ir.firoozehcorp.burglaralarm.utils.ApiRequestUtil
import ir.firoozehcorp.burglaralarm.utils.NotificationUtil
import java.util.*
import kotlin.concurrent.timerTask


class AlarmService : IntentService(AlarmService::javaClass.name) {

    companion object {
        var isStarted = false
        var isAlarmNeedToActive = false
        var timer: Timer? = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int { // Let it continue running until it is stopped.
        if (!isStarted) {
            Toast.makeText(this, "Alarm Service Started", Toast.LENGTH_LONG).show()
            NotificationUtil.createNotification(this, false)
            startCheckerTimer()
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isStarted = false
        stopCheckerTimer()
        NotificationUtil.disableNotification(this)
        Toast.makeText(this, "Alarm Service Destroyed", Toast.LENGTH_LONG).show()
    }


    override fun onHandleIntent(p0: Intent?) {

    }

    private fun startCheckerTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(
            timerTask {
                ApiRequestUtil.getSensorStatus(this@AlarmService, object : ServerApiListener {
                    override fun onResponse(res: ApiResponse) {
                        isAlarmNeedToActive = res.status
                        if (isAlarmNeedToActive) NotificationUtil.createNotification(
                            this@AlarmService,
                            true
                        )
                        else NotificationUtil.createNotification(this@AlarmService, false)
                    }

                    override fun onError(errMsg: String) {
                        Log.e(this@AlarmService::javaClass.name, "startCheckerTimer err : $errMsg")
                    }
                })
            }
            , 0, 3000)
    }

    private fun stopCheckerTimer() {
        timer?.cancel()
        timer?.purge()
    }

}