package me.tang.mvvm.base

import android.view.View

interface OnItemClickListener<T> {
    fun onClick(view: View, item: T)
}