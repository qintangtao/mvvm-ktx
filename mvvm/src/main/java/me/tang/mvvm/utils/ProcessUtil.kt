package me.tang.mvvm.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process

fun isMainProcess(context: Context) = context.packageName == currentProcessName(context)


private fun currentProcessName(context: Context): String {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (process in manager.runningAppProcesses) {
        if (process.pid == Process.myPid()) {
            return process.processName
        }
    }
    return ""
}