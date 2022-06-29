package me.tang.mvvm.ext

import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.internal.message.DialogContentLayout

fun MaterialDialog.getContentLayout() : DialogContentLayout {
    return this.view.contentLayout
}