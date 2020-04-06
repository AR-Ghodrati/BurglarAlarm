package ir.firoozehcorp.burglaralarm.activates

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.firoozehcorp.burglaralarm.R
import ir.firoozehcorp.burglaralarm.listeners.BoardApiListener
import ir.firoozehcorp.burglaralarm.models.BoardConfig
import ir.firoozehcorp.burglaralarm.models.BoardResponse
import ir.firoozehcorp.burglaralarm.utils.ApiRequestUtil
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        submit.setOnClickListener {
            try {
                val _ssid = ssid.text.toString().trim()
                val pass = password.text.toString().trim()
                val _distance = distance.text.toString().trim().toInt()

                if (_ssid.isNotEmpty() && pass.isNotEmpty() && _distance < 120) {
                    submit.visibility = View.GONE
                    ApiRequestUtil.sendBoardConfig(
                        this,
                        BoardConfig(_ssid, pass, _distance),
                        object : BoardApiListener {
                            override fun onResponse(res: BoardResponse) {
                                submit.visibility = View.VISIBLE
                                Toast.makeText(
                                    this@SettingActivity,
                                    "Data Saved Successfully With Data : " + res.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onError(errMsg: String) {
                                submit.visibility = View.VISIBLE
                                Toast.makeText(
                                    this@SettingActivity,
                                    "Error happened : $errMsg",
                                    Toast.LENGTH_LONG
                                ).show()
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

}