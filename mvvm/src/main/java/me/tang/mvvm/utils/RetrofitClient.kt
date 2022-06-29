package me.tang.mvvm.utils

import me.tang.mvvm.BuildConfig
import me.tang.mvvm.network.interceptor.Level
import me.tang.mvvm.network.interceptor.LoggingInterceptor
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {

    companion object {
        fun getInstance() = SingletonHolder.INSTANCE
        private lateinit var retrofit: Retrofit
    }

    private object SingletonHolder {
        val INSTANCE by lazy { RetrofitClient() }
    }

    fun <T> create(url: String, service: Class<T>?): T = Retrofit.Builder()
        .client(mOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(url)
        .build()
        .create(service!!) ?: throw RuntimeException("Api service is null!")

    private val mOkHttpClient: OkHttpClient by lazy { getOkHttpClient() }

    private fun getOkHttpClient(): OkHttpClient =  OkHttpClient.Builder().apply {
        connectTimeout(20L, TimeUnit.SECONDS)
        addNetworkInterceptor(LoggingInterceptor().apply {
            isDebug = BuildConfig.DEBUG
            level = Level.BASIC
            type = Platform.INFO
            requestTag = "Request"
            requestTag = "Response"
        })
        writeTimeout(20L, TimeUnit.SECONDS)
        connectionPool(ConnectionPool(8, 15, TimeUnit.SECONDS))
    }.build()
}