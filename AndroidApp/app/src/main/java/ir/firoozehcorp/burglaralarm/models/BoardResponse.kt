package ir.firoozehcorp.burglaralarm.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class BoardResponse : Serializable {

    @SerializedName("status")
    @Expose
    var status: Boolean = false


    @SerializedName("msg")
    @Expose
    var message: String = ""
}