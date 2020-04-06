package ir.firoozehcorp.burglaralarm.listeners

import ir.firoozehcorp.burglaralarm.models.BoardResponse

interface BoardApiListener {
    fun onResponse(res: BoardResponse)
    fun onError(errMsg: String)
}