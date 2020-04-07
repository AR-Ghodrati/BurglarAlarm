package ir.firoozehcorp.burglaralarm.activates

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.firoozehcorp.burglaralarm.R
import ir.firoozehcorp.burglaralarm.listeners.ServerApiListener
import ir.firoozehcorp.burglaralarm.models.ApiResponse
import ir.firoozehcorp.burglaralarm.models.server.AlarmStatus
import ir.firoozehcorp.burglaralarm.services.AlarmService
import ir.firoozehcorp.burglaralarm.utils.ApiRequestUtil
import ir.firoozehcorp.burglaralarm.utils.StorageUtil
import kotlinx.android.synthetic.main.intro_ac_layout.view.*
import kotlinx.android.synthetic.main.main_ac_layout.*
import java.util.*
import kotlin.concurrent.timerTask


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_ac_layout)

        setSupportActionBar(toolbar)

        if (StorageUtil.isAlarmEnable(this)) {
            swith.background = resources.getDrawable(R.drawable.round_button_disable)
            swith.text = "Disable Alarm"
            startService(Intent(this@MainActivity, AlarmService::class.java))
        }

        swith.setOnClickListener {
            if (StorageUtil.isAlarmEnable(this)) {
                swith.isClickable = false
                ApiRequestUtil.sendAlarmStatus(this
                    , AlarmStatus(StorageUtil.getDeviceID(this), false)
                    , object : ServerApiListener {
                        override fun onResponse(res: ApiResponse) {
                            swith.isClickable = true
                            swith.background = resources.getDrawable(R.drawable.round_button_enable)
                            swith.text = "Enable Alarm"

                            StorageUtil.setAlarmStatus(false, this@MainActivity)
                            // stop alarm service
                            stopService(Intent(this@MainActivity, AlarmService::class.java))
                        }

                        override fun onError(errMsg: String) {
                            swith.isClickable = true
                            Toast.makeText(
                                this@MainActivity,
                                "change status Failed! >> $errMsg",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    })
            }
            else
                ApiRequestUtil.sendAlarmStatus(this
                    , AlarmStatus(StorageUtil.getDeviceID(this), true)
                    , object : ServerApiListener {
                        override fun onResponse(res: ApiResponse) {
                            swith.isClickable = true
                            swith.background =
                                resources.getDrawable(R.drawable.round_button_disable)
                            StorageUtil.setAlarmStatus(true, this@MainActivity)
                            swith.text = "Disable Alarm"


                            // start alarm service
                            startService(Intent(this@MainActivity, AlarmService::class.java))
                        }

                        override fun onError(errMsg: String) {
                            swith.isClickable = true
                            Toast.makeText(
                                this@MainActivity,
                                "change status Failed! >> $errMsg",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    })

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settingMenu)
            startActivity(Intent(this, SettingActivity::class.java)
                .apply {
                    putExtra("fromIntro", false)
                })

        return super.onOptionsItemSelected(item)
    }
}
