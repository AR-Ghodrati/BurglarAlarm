package ir.firoozehcorp.burglaralarm.utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import ir.firoozehcorp.burglaralarm.consts.Const
import ir.firoozehcorp.burglaralarm.listeners.BoardApiListener
import ir.firoozehcorp.burglaralarm.models.BoardConfig
import ir.firoozehcorp.burglaralarm.models.BoardResponse
import org.json.JSONObject


object ApiRequestUtil {

    fun sendBoardConfig(context: Context, config: BoardConfig, listener: BoardApiListener?) {
        val requestQueue = Volley.newRequestQueue(context)
        val body = Gson().toJson(config)

        val jsonobj: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.POST, Const.BoardUrl + "/config", JSONObject(body),
                Response.Listener<JSONObject?> {
                    listener?.onResponse(Gson().fromJson(it.toString(), BoardResponse::class.java))
                },
                Response.ErrorListener {
                    listener?.onError(it.message.toString())
                }
            ) { //here I want to post data to sever
            }

        requestQueue.add(jsonobj)
    }
}