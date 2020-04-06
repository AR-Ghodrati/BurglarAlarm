package ir.firoozehcorp.burglaralarm.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BoardConfig
    (
    @SerializedName("ssid") @Expose var ssid: String,
    @SerializedName("pass") @Expose var pass: String,
    @SerializedName("max_distance") @Expose var maxDistance: Int
)