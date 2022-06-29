package me.tang.mvvm.network

import me.tang.mvvm.base.IBaseResponse


class ResponseThrowable : Exception {
    var code: Int
    var msg: String

    constructor(error: ERROR, e: Throwable? = null): super(e) {
        this.code = error.getCode()
        this.msg = error.getMsg()
    }

    constructor(code: Int, msg: String, e: Throwable? = null) : super(e) {
        this.code = code
        this.msg = msg
    }

    constructor(base: IBaseResponse<*>, e: Throwable? = null) : super(e) {
        this.code = base.code()
        this.msg = base.msg()
    }
}