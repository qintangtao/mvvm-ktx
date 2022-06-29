package me.tang.mvvm.state.base

import android.os.Parcel
import android.util.Log
import androidx.databinding.ViewDataBinding
import me.tang.mvvm.base.BaseFragment
import me.tang.mvvm.base.BaseViewModel
import me.tang.mvvm.event.Message
import me.tang.mvvm.network.ERROR
import me.tang.mvvm.network.RESULT
import me.tang.mvvm.state.view.StateLayout

abstract class BaseStateFragment<VM : BaseViewModel, DB : ViewDataBinding>() : BaseFragment<VM, DB>() {

    private val TAG = "StatusLayout"
    protected var isErrorLoaded = false
    protected var isEmptyLoaded = false

    constructor(parcel: Parcel) : this() {
        isErrorLoaded = parcel.readByte() != 0.toByte()
        isEmptyLoaded = parcel.readByte() != 0.toByte()
    }

    abstract fun stateLayout(): StateLayout

    /*
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        stateLayout().run {
            setEmptyClickListener{
                lazyLoadData()
            }
            setErrorClickListener{
                lazyLoadData()
            }
            setNetErrorClickListener{
                lazyLoadData()
            }
        }
    }
*/
    override fun onResume() {
        super.onResume()
        stateLayout().run {
            setEmptyClickListener{
                lazyLoadData()
            }
            setErrorClickListener{
                lazyLoadData()
            }
            setNetErrorClickListener{
                lazyLoadData()
            }
        }
    }

    override fun onLoadStart() {
        isErrorLoaded = false
        isEmptyLoaded = false
        stateLayout().showLoadingView()
    }

    /*
    * PARSE_ERROR(1001, "解析错误"),
    /**
     * 网络错误
     */
    NETWORD_ERROR(1002, "网络错误"),
    /**
     * 协议出错
     */
    HTTP_ERROR(1003, "协议出错"),

    /**
     * 证书出错
     */
    SSL_ERROR(1004, "证书出错"),

    /**
     * 连接超时
     */
    TIMEOUT_ERROR(1006, "连接超时");
    * */
    override fun onLoadEvent(msg: Message) {
        isErrorLoaded = true
        when(msg.code) {
            ERROR.PARSE_ERROR.getCode(),
            ERROR.NETWORD_ERROR.getCode(),
            ERROR.HTTP_ERROR.getCode(),
            ERROR.SSL_ERROR.getCode(),
            ERROR.TIMEOUT_ERROR.getCode() -> stateLayout().showNetErrorView()
            else -> stateLayout().showErrorView()
        }
        Log.d(TAG, "showErrorView")
    }

    override fun onLoadResult(code: Int) {
        when(code) {
            RESULT.EMPTY.code -> {
                isEmptyLoaded = true
                stateLayout().showEmptyView()
                Log.d(TAG, "showEmptyView")
            }
            else -> {}
        }
    }

    override fun onLoadCompleted() {
        if (!isErrorLoaded && !isEmptyLoaded) {
            stateLayout().showContentView()
            Log.d(TAG, "showContentView")
        }
    }

}