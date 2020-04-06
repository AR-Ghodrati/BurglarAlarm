package ir.firoozehcorp.burglaralarm.activates

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ir.firoozehcorp.burglaralarm.R
import ir.firoozehcorp.burglaralarm.consts.Const
import ir.firoozehcorp.burglaralarm.utils.NetworkUtil
import ir.firoozehcorp.burglaralarm.utils.StorageUtil
import kotlinx.android.synthetic.main.intro_ac_layout.*
import java.util.*
import kotlin.concurrent.schedule

class IntroActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_ac_layout)


        if (StorageUtil.isSetSettingsBefore(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            if (checkConnectionToBoard()) {
                startActivity(Intent(this, SettingActivity::class.java))
                finish()
            } else {
                progressBar.visibility = View.GONE
                log.text = "Must Connect to WiFi with Name \"${Const.SSID}\""
                Timer("close", false).schedule(5000) {
                    finish()
                }
            }
        }


    }

    private fun checkConnectionToBoard(): Boolean {
        return NetworkUtil.getCurrentSsid(this) == Const.SSID
    }

}