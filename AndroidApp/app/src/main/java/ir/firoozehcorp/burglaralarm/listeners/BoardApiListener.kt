package ir.firoozehcorp.burglaralarm.listeners

import ir.firoozehcorp.burglaralarm.models.ApiResponse

interface BoardApiListener {
    fun onResponse(res: ApiResponse)
    fun onError(errMsg: String)
}