package me.tang.mvvm.bus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.jeremyliao.liveeventbus.LiveEventBus

object Bus {

    inline fun <reified T> post(key: String, value: T) =
        LiveEventBus.get(key, T::class.java).post(value)

    inline fun <reified T> postDelay(
        key: String,
        owner: LifecycleOwner,
        value: T,
        delay: Long
    ) = LiveEventBus.get(key, T::class.java).postDelay(owner, value, delay)

    inline fun <reified T> observe(
        key: String,
        owner: LifecycleOwner,
        crossinline observer: (T) -> Unit
    ) = LiveEventBus.get(key, T::class.java).observe(owner, Observer { observer(it) })

    inline fun <reified T> observeSticky(
        key: String,
        owner: LifecycleOwner,
        crossinline observer: (T) -> Unit
    ) = LiveEventBus.get(key, T::class.java).observeSticky(owner, Observer { observer(it) })

}