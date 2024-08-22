package com.correct.mobezero.utils

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class AutoLogout(private val context: Context, workerParams: WorkerParameters?) : Worker(
    context, workerParams!!
) {
    override fun doWork(): Result {
        Log.e("AutoLogout : ", "Initiated")
        /*     UserDefaults defaults = new UserDefaults(context);
        defaults.setShouldAutoLogout(true);
        defaults.save();*/
        return Result.success()
    }
}
