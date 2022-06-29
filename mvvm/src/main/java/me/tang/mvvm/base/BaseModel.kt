package me.tang.mvvm.base

import me.tang.mvvm.network.ResponseThrowable


abstract class BaseModel {

    suspend fun <T> cacheNetCall(
        remoto: suspend () -> IBaseResponse<T>,
        local: suspend () -> T?,
        save: suspend (T) -> Unit,
        isUseCache: (T?) -> Boolean = { false }
    ) : T {
        val localData = local.invoke()
        if (isUseCache(localData)) return localData!!
        else {
            val net = remoto()
            if (net.isSuccess()) {
                return net.data()!!.also { save(it) }
            }
            throw ResponseThrowable(net)
        }
    }

}