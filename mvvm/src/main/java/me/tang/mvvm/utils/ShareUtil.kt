package me.tang.mvvm.utils

import android.app.Activity
import androidx.core.app.ShareCompat

fun share(
    activity: Activity,
    title: String?,
    content: String?
) {
    ShareCompat.IntentBuilder.from(activity)
        .setType("text/plain")
        .setSubject(title)
        .setText(content)
        .setChooserTitle(title)
        .startChooser()
}