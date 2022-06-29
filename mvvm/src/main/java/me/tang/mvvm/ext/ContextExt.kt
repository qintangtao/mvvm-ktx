package me.tang.mvvm.ext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.copyTextIntoClipboard(text: CharSequence?, label: String? = "") {
    if (text.isNullOrEmpty()) return
    val cbs = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return
    cbs.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun Context.showToast(message: CharSequence) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showToast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun <T> Context.startActivity(type: Class<T>) {
    startActivity(Intent(this, type))
}

