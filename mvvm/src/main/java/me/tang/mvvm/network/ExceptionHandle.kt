package me.tang.mvvm.network

import android.net.ParseException
import android.util.Log
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException

object ExceptionHandle {

    fun handleException(e: Throwable): ResponseThrowable {
        val ex: ResponseThrowable
        if (e is HttpException) {
            ex = ResponseThrowable(ERROR.HTTP_ERROR, e)
        } else if (e is JsonParseException
            || e is JSONException
            || e is ParseException || e is MalformedJsonException
        ) {
            ex = ResponseThrowable(ERROR.PARSE_ERROR, e)
        } else if (e is ConnectException) {
            ex = ResponseThrowable(ERROR.NETWORD_ERROR, e)
        } else if (e is javax.net.ssl.SSLException) {
            ex = ResponseThrowable(ERROR.SSL_ERROR, e)
        } else if (e is java.net.SocketTimeoutException) {
            ex = ResponseThrowable(ERROR.TIMEOUT_ERROR, e)
        } else if (e is java.net.UnknownHostException) {
            ex = ResponseThrowable(ERROR.TIMEOUT_ERROR, e)
        } else if (e is ResponseThrowable) {
            ex = e
        } else {
            ex = if (!e.message.isNullOrEmpty()) ResponseThrowable(ERROR.UNKNOWN.getCode(), e.message!!, e)
            else ResponseThrowable(ERROR.UNKNOWN, e)
        }
        stackTrackString(e)?.let { Log.d("ExceptionHandle", it) }
        return ex
    }

    fun stackTrackString(e: Throwable): String? {
        var msg: String? = e.message
        e.stackTrace.forEach {
            msg += "\n"
            msg += it.toString()
        }
        return msg
    }
}