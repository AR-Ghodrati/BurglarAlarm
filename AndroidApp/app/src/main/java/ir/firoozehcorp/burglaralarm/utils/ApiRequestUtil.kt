package ir.firoozehcorp.burglaralarm.utils

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import ir.firoozehcorp.burglaralarm.consts.Const
import ir.firoozehcorp.burglaralarm.listeners.BoardApiListener
import ir.firoozehcorp.burglaralarm.listeners.ServerApiListener
import ir.firoozehcorp.burglaralarm.models.ApiResponse
import ir.firoozehcorp.burglaralarm.models.board.BoardConfig
import ir.firoozehcorp.burglaralarm.models.server.AlarmStatus
import org.json.JSONObject


object ApiRequestUtil {

    fun sendBoardConfig(context: Context, config: BoardConfig, listener: BoardApiListener?) {
        val requestQueue = Volley.newRequestQueue(context)
        val body = Gson().toJson(config)

        val jsonobj: JsonObjectRequest =
            object : JsonObjectRequest(
                Method.POST, Const.BoardUrl + "/config", JSONObject(body),
                Response.Listener {
                    listener?.onResponse(Gson().fromJson(it.toString(), ApiResponse::class.java))
                },
                Response.ErrorListener {
                    listener?.onError(it.message.toString())
                }
            ) { //here I want to post data to sever
            }

        requestQueue.add(jsonobj)
    }

    fun sendAlarmStatus(context: Context, status: AlarmStatus, listener: ServerApiListener?) {
        val requestQueue = Volley.newRequestQueue(context)
        val body = Gson().toJson(status)

        val jsonobj: JsonObjectRequest =
            object : JsonObjectRequest(
                Method.POST, Const.ServerUrl + "/setAlarmStatus", JSONObject(body),
                Response.Listener {
                    listener?.onResponse(Gson().fromJson(it.toString(), ApiResponse::class.java))
                },
                Response.ErrorListener {
                    listener?.onError(it.message.toString())
                }
            ) { //here I want to post data to sever
            }

        requestQueue.add(jsonobj)
    }


    fun getAlarmStatus(context: Context, listener: ServerApiListener?) {
        val requestQueue = Volley.newRequestQueue(context)

        val jsonobj: JsonObjectRequest =
            object : JsonObjectRequest(
                Method.GET,
                Const.ServerUrl + "/getAlarmStatus?did=" + StorageUtil.getDeviceID(context),
                null,
                Response.Listener {
                    listener?.onResponse(Gson().fromJson(it.toString(), ApiResponse::class.java))
                },
                Response.ErrorListener {
                    listener?.onError(it.message.toString())
                }
            ) { //here I want to post data to sever
            }

        requestQueue.add(jsonobj)
    }

}