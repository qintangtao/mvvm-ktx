package me.tang.mvvm.base

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.*
import me.tang.mvvm.event.Message
import me.tang.mvvm.event.SingleLiveEvent
import me.tang.mvvm.network.ExceptionHandle
import me.tang.mvvm.network.RESULT
import me.tang.mvvm.network.ResponseThrowable

// 继承AndroidViewModel(Utils.getApp()) 会出错，原因如下
// ViewModelProvider(viewModelStore, defaultViewModelProviderFactory).get(tClass)
// defaultViewModelProviderFactory 会用带有Application的构造函数实例化ViewModel
// 所以直接继承 ViewModel
open class BaseViewModel() :  ViewModel(), LifecycleObserver {

    val defUI: UIChange by lazy { UIChange() }

    fun launchUI(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch { block() }

    fun <T> launchFlow(block: suspend () -> T): Flow<T> = flow { emit(block()) }

    /**
     *  不过滤请求结果
     * @param block 请求体
     * @param error 失败回调
     * @param complete  完成回调（无论成功失败都会调用）
     */
    fun launchGo(
        block: suspend CoroutineScope.() -> Unit,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
            callError(Message(it.code, it.msg))
        },
        complete: suspend CoroutineScope.() -> Unit = {
            callComplete()
        },
        isNotify: Boolean = true
    ) {
        if (isNotify) callStart()
        launchUI {
            handleException(
                withContext(Dispatchers.IO) { block },
                { error(it) },
                { complete() }
            )
        }
    }

    /**
     * 过滤请求结果，其他全抛异常
     * @param block 请求体
     * @param success 成功回调
     * @param error 失败回调
     * @param complete  完成回调（无论成功失败都会调用）
     */
    fun <T> launchOnlyResult(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: suspend CoroutineScope.(T) -> Int,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
            callError(Message(it.code, it.msg))
        },
        complete: suspend CoroutineScope.() -> Unit = {
            callComplete()
        },
        isNotify: Boolean = true
    ) {
        if (isNotify) callStart()
        launchUI {
            handleException(
                { withContext(Dispatchers.IO) { block() } },
                { executeResponse(it) { callResult(success(it)) } },
                { error(it) },
                { complete() }
            )
        }
    }

    fun <T, R> launchSerialResult(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: suspend CoroutineScope.(T) -> Int,
        block2: suspend CoroutineScope.() -> IBaseResponse<R>,
        success2: suspend CoroutineScope.(R) -> Int,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
            callError(Message(it.code, it.msg))
        },
        complete: suspend CoroutineScope.() -> Unit = {
            callComplete()
        },
        isNotify: Boolean = true
    ) {
        if (isNotify) callStart()
        launchUI {
            handleException(
                { withContext(Dispatchers.IO) { block() } },
                { executeResponseResult(it) {
                    val code = success(it)
                    if (code != RESULT.SUCCESS.code)
                        callResult(code)
                    code == RESULT.SUCCESS.code
                } },
                { withContext(Dispatchers.IO) { block2() }},
                { executeResponse(it) { callResult(success2(it)) } },
                { error(it) },
                { complete() }
            )
        }
    }

    fun <T> launchFlowResult(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: suspend CoroutineScope.(T) -> Int,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
            callError(Message(it.code, it.msg))
        },
        complete: suspend CoroutineScope.() -> Unit = {
            callComplete()
        },
        retry: Long = 0,
        isNotify: Boolean = true
    ) {
        if (isNotify) callStart()
        launchUI {
            launchFlow { block() }
                .retry(retry)
                .onStart { if (isNotify) callStart() }
                .flowOn(Dispatchers.IO)
                .onEach { if (!it.isSuccess()) throw ResponseThrowable(it) }
                .catch { error(ExceptionHandle.handleException(it)) }
                .onCompletion { complete() }
                .collect { callResult(success(it.data())) }
        }
    }

    fun <T1, T2, R> launchFlowzipResult(
        block: suspend CoroutineScope.() -> IBaseResponse<T1>,
        block2: suspend CoroutineScope.() -> IBaseResponse<T2>,
        transform: suspend CoroutineScope.(T1, T2) -> R,
        success: suspend CoroutineScope.(R) -> Int,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
            callError(Message(it.code, it.msg))
        },
        complete: suspend CoroutineScope.() -> Unit = { callComplete() },
        isNotify: Boolean = true
    ) {
        launchUI {
            launchFlow { block() }
                .zip(launchFlow { block2() }) { l, r ->
                    executeResponseFlow(l, r) { ld, rd -> transform(ld, rd) } }
                .onStart { if (isNotify) callStart() }
                .flowOn(Dispatchers.IO)
                .catch { error(ExceptionHandle.handleException(it)) }
                .onCompletion { complete() }
                .collect { callResult(success(it)) }
        }
    }

    fun <T1, T2> launchFlowzipResult(
        block: suspend CoroutineScope.() -> IBaseResponse<T1>,
        block2: suspend CoroutineScope.() -> IBaseResponse<T2>,
        success: suspend CoroutineScope.(Array<Any?>) -> Int,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
            callError(Message(it.code, it.msg))
        },
        complete: suspend CoroutineScope.() -> Unit = { callComplete() },
        isNotify: Boolean = true
    ) {
        launchUI {
            launchFlow { block() }
                .zip(launchFlow { block2() }) { a, b ->
                    executeResponseFlow(a, b) { a, b ->
                        arrayOfNulls<Any>(2).apply {
                            set(0, a)
                            set(1, b)
                        }
                    }
                }
                .onStart { if (isNotify) callStart() }
                .flowOn(Dispatchers.IO)
                .catch { error(ExceptionHandle.handleException(it)) }
                .onCompletion { complete() }
                .collect { callResult(success(it)) }
        }
    }

    fun <T1, T2> launchFlowCombineResult(
        block: suspend CoroutineScope.() -> IBaseResponse<T1>,
        block2: suspend CoroutineScope.() -> IBaseResponse<T2>,
        success: suspend CoroutineScope.(Array<Any?>) -> Int,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
            callError(Message(it.code, it.msg))
        },
        complete: suspend CoroutineScope.() -> Unit = { callComplete() },
        isNotify: Boolean = true
    ) {
        launchUI {
            launchFlow { block() }
                .combine(launchFlow { block2() }) { a, b ->
                    executeResponseFlow(a, b) { a, b ->
                        arrayOfNulls<Any>(2).apply {
                            set(0, a)
                            set(1, b)
                        } } }
                .onStart { if (isNotify) callStart() }
                .flowOn(Dispatchers.IO)
                .catch { error(ExceptionHandle.handleException(it)) }
                .onCompletion { complete() }
                .collect { callResult(success(it)) }
        }
    }

    fun <T1, T2, T3> launchFlowCombine3Result(
        block: suspend CoroutineScope.() -> IBaseResponse<T1>,
        block2: suspend CoroutineScope.() -> IBaseResponse<T2>,
        block3: suspend CoroutineScope.() -> IBaseResponse<T3>,
        success: suspend CoroutineScope.(Array<Any?>) -> Int,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
            callError(Message(it.code, it.msg))
        },
        complete: suspend CoroutineScope.() -> Unit = { callComplete() },
        isNotify: Boolean = true
    ) {
        launchUI {
            combine(launchFlow { block() },
                launchFlow { block2() },
                launchFlow { block3() } ) { a, b, c ->
                    executeResponseFlow(a, b, c) { a, b, c ->
                        arrayOfNulls<Any>(3).apply {
                            set(0, a)
                            set(1, b)
                            set(2, c)
                        } } }
                .onStart { if (isNotify) callStart() }
                .flowOn(Dispatchers.IO)
                .catch { error(ExceptionHandle.handleException(it)) }
                .onCompletion { complete() }
                .collect { callResult(success(it)) }
        }
    }

    /*
    fun <T, R> launchFlowSerialResult(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: suspend CoroutineScope.(T) -> Int,
        block2: suspend CoroutineScope.() -> IBaseResponse<R>,
        success2: suspend CoroutineScope.(R) -> Int,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
            callError(Message(it.code, it.msg))
        },
        complete: suspend CoroutineScope.() -> Unit = { callComplete() },
        isNotify: Boolean = true
    ) {
        launchUI {
            launchFlow { block() }
                .flatMapConcat {
                    return@flatMapConcat executeResponseFlow(it) {
                        val code = success(it)
                        if (code != RESULT.SUCCESS.code) {
                            callResult(code)
                            flow{}
                        } else launchFlow { block2() } }
                }
                .onStart { if (isNotify) callStart() }
                .flowOn(Dispatchers.IO)
                .onEach { if (!it.isSuccess()) throw ResponseThrowable(it) }
                .catch { error(ExceptionHandle.handleException(it)) }
                .onCompletion { complete() }
                .collect { callResult(success2(it.data()))  }
        }
    }
    */

    /**
     * 请求结果过滤
     */
    private suspend fun <T> executeResponse(
        response: IBaseResponse<T>,
        success: suspend CoroutineScope.(T) -> Unit
    ) {
        coroutineScope {
            if (response.isSuccess()) success(response.data())
            else throw ResponseThrowable(response)
        }
    }

    private suspend fun <T> executeResponseResult(
        response: IBaseResponse<T>,
        success: suspend CoroutineScope.(T) -> Boolean
    ): Boolean {
        return coroutineScope {
            if (!response.isSuccess()) throw ResponseThrowable(response)
            success(response.data())
        }
    }

    /**
     * 请求结果过滤
     */
    private suspend fun <T1, T2, R> executeResponseFlow(
        response: IBaseResponse<T1>,
        response2: IBaseResponse<T2>,
        success: suspend CoroutineScope.(T1, T2) -> R
    ): R {
        return coroutineScope {
            if (!response.isSuccess()) throw ResponseThrowable(response)
            if (!response2.isSuccess()) throw ResponseThrowable(response2)
            success(response.data(), response2.data())
        }
    }

    /**
     * 请求结果过滤
     */
    private suspend fun <T1, T2, T3, R> executeResponseFlow(
        response: IBaseResponse<T1>,
        response2: IBaseResponse<T2>,
        response3: IBaseResponse<T3>,
        success: suspend CoroutineScope.(T1, T2, T3) -> R
    ): R {
        return coroutineScope {
            if (!response.isSuccess()) throw ResponseThrowable(response)
            if (!response2.isSuccess()) throw ResponseThrowable(response2)
            if (!response3.isSuccess()) throw ResponseThrowable(response3)
            success(response.data(), response2.data(), response3.data())
        }
    }

    /**
     * 请求结果过滤
     */
    private suspend fun <T, R> executeResponseFlow(
        response: IBaseResponse<T>,
        success: suspend CoroutineScope.(T) -> Flow<R>
    ): Flow<R> {
        return coroutineScope {
            if (response.isSuccess()) success(response.data())
            else throw ResponseThrowable(response)
        }
    }

    private suspend fun <T, R> handleException(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: suspend CoroutineScope.(IBaseResponse<T>) -> Boolean,
        block2: suspend CoroutineScope.() -> IBaseResponse<R>,
        success2: suspend CoroutineScope.(IBaseResponse<R>) -> Unit,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit,
        complete: suspend CoroutineScope.() -> Unit
    ) {
        coroutineScope {
            try {
                if (success(block()))
                    success2(block2())
            } catch (e: Throwable) {
                error(ExceptionHandle.handleException(e))
            } finally {
                complete()
            }
        }
    }

    private suspend fun <T> handleException(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: suspend CoroutineScope.(IBaseResponse<T>) -> Unit,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit,
        complete: suspend CoroutineScope.() -> Unit
    ) {
        coroutineScope {
            try {
                success(block())
            } catch (e: Throwable) {
                error(ExceptionHandle.handleException(e))
            } finally {
                complete()
            }
        }
    }

    private suspend fun handleException(
        block: suspend CoroutineScope.() -> Unit,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit,
        complete: suspend CoroutineScope.() -> Unit
    ) {
        coroutineScope {
            try {
                block()
            } catch (e: Throwable) {
                error(ExceptionHandle.handleException(e))
            } finally {
                complete()
            }
        }
    }

    inner class UIChange {
        val start by lazy { SingleLiveEvent<Void>() }
        val error by lazy { SingleLiveEvent<Message>() }
        val result by lazy { SingleLiveEvent<Int>() }
        val complete by lazy { SingleLiveEvent<Void>() }
    }

    private inline fun callStart() {
        defUI.start.call()
    }

    private inline fun callError(msg: Message) {
        defUI.error.call(msg)
    }

    private inline fun callResult(code: Int) {
        defUI.result.call(code)
    }

    private inline fun callComplete() {
        defUI.complete.call()
    }

    fun getString(resId: Int): String {
        //return getApplication<Application>().getString(resId)
        return Utils.getApp().getString(resId)
    }
}