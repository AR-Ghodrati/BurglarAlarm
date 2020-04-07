package ir.firoozehcorp.burglaralarm.models.server

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AlarmStatus(
    @SerializedName("device_id") @Expose var did: String,
    @SerializedName("status") @Expose var status: Boolean
)
