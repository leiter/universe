package com.together.app

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.together.utils.ACTION_START_SERVICE
import com.together.utils.ACTION_STOP_SERVICE

class SwitchWorker(context: Context, parameters: WorkerParameters) : Worker(context,parameters) {

    var running : Boolean = false

    override fun doWork(): Result {

        val a = if (!running) ACTION_START_SERVICE else ACTION_STOP_SERVICE

        Intent(applicationContext, ListenerService::class.java).also {
            it.action = a
            applicationContext.startService(it)
        }
        Log.e("TTTTT", "WWWWWWWWWWWWWWWw  $running");
        running = !running
        return Result.success()
    }
}