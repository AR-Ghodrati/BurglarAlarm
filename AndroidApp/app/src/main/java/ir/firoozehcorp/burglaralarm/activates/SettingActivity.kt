package ir.firoozehcorp.burglaralarm.activates

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.firoozehcorp.burglaralarm.R
import ir.firoozehcorp.burglaralarm.listeners.BoardApiListener
import ir.firoozehcorp.burglaralarm.models.ApiResponse
import ir.firoozehcorp.burglaralarm.models.board.BoardConfig
import ir.firoozehcorp.burglaralarm.utils.ApiRequestUtil
import ir.firoozehcorp.burglaralarm.utils.StorageUtil
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    var canBack = true
    var fromIntro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        fromIntro = intent.getBooleanExtra("fromIntro", false)

        submit.setOnClickListener {
            try {
                val _ssid = ssid.text.toString().trim()
                val pass = password.text.toString().trim()
                val _distance = distance.text.toString().trim().toInt()

                if (_ssid.isNotEmpty() && pass.isNotEmpty() && _distance < 120) {
                    submit.visibility = View.GONE
                    loading.visibility = View.VISIBLE

                    canBack = false
                    ApiRequestUtil.sendBoardConfig(
                        this,
                        BoardConfig(
                            StorageUtil.getDeviceID(this),
                            _ssid,
                            pass,
                            _distance
                        ),
                        object : BoardApiListener {
                            override fun onResponse(res: ApiResponse) {
                                submit.visibility = View.VISIBLE
                                loading.visibility = View.GONE

                                Toast.makeText(
                                    this@SettingActivity,
                                    "Data Saved Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                StorageUtil.setSettingsOk(true, this@SettingActivity)
                                canBack = true
                            }

                            override fun onError(errMsg: String) {
                                submit.visibility = View.VISIBLE
                                loading.visibility = View.GONE

                                Toast.makeText(
                                    this@SettingActivity,
                                    "Error happened : $errMsg",
                                    Toast.LENGTH_LONG
                                ).show()
                                canBack = false
                            }
                        })
                } else {
                    if (_ssid.isEmpty()) ssid.error = "SSID cant be empty!"
                    if (pass.isEmpty()) password.error = "SSID cant be empty!"
                    if (_distance >= 120) distance.error = "distance cant be grater than 120 cm!"
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error happened : " + e.message, Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onBackPressed() {
        if (canBack) {
            if (fromIntro) startActivity(Intent(this, IntroActivity::class.java))
            super.onBackPressed()
        }
    }

}