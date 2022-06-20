package com.together.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.work.*
import com.together.repository.SharedData
import com.together.utils.ACTION_START_SERVICE
import com.together.utils.ACTION_STOP_SERVICE
import com.together.utils.FILE_CONFIG_NOTIFICATION
import com.together.utils.getDayTimeDate
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.prefs.Preferences

class SwitchWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    private lateinit var disposable: Disposable

    companion object {
        const val MODE = "mode"

        private const val startKey = "start"
        private const val stopKey = "end"
        private const val skipToDayKey = "skipToDay"
        private const val initializedKey = "initializedKey"

        private const val startDefault = "20:18"
        private const val endDefault = "20:17"
        private const val skipToDayDefault = false
//        private const val initializedDefault = false
    }

    private val notificationConf: NotificationConf = NotificationConf(context)

    class NotificationConf(context: Context) {

        private val currentDate = Calendar.getInstance()
        private val dueDate = Calendar.getInstance()

        var startDelay : Long = -1
        var stopDelay:  Long = -1

        init { load(context) }

        private fun load(context: Context){
            val sharedPreferences = context.getSharedPreferences(FILE_CONFIG_NOTIFICATION,
                Context.MODE_PRIVATE)
            startDelay = calculateDelay(sharedPreferences.getString(
                startKey, startDefault) ?: startDefault)
            stopDelay = calculateDelay(sharedPreferences.getString(
                stopKey, endDefault) ?: endDefault)
        }


        private fun calculateDelay(stringTime: String, before: Boolean = true) : Long {
            val hourMinute = stringTime.split(":")
            dueDate.set(Calendar.HOUR_OF_DAY,hourMinute[0].toInt())
            dueDate.set(Calendar.MINUTE,hourMinute[1].toInt())
            dueDate.set(Calendar.SECOND,0)
            if(before) {
                if(dueDate.before(currentDate)){
                    dueDate.add(Calendar.HOUR_OF_DAY, 24)
                }
            } else {
                if(dueDate.after(currentDate)){
                    dueDate.add(Calendar.HOUR_OF_DAY, 24)
                }
            }
            Log.e("TTTTT", "Gap ${dueDate.timeInMillis - currentDate.timeInMillis}");
            return (dueDate.timeInMillis - currentDate.timeInMillis)/1000
        }

    }


    override fun doWork(): Result {
        val start = inputData.getString(MODE)!!
        Log.e("TTTTT", "doWork  $start");
        val a = if (start == ACTION_START_SERVICE) ACTION_STOP_SERVICE else ACTION_START_SERVICE
        handleService(start)
        scheduleNextAction(a)
        return Result.success()
    }

    private fun scheduleNextAction(start: String) {
        val delay = if(start== ACTION_START_SERVICE) notificationConf.startDelay
        else notificationConf.stopDelay
        Log.e("TTTTT", "Schedule $start   $delay");
        val nextRequest = OneTimeWorkRequestBuilder<SwitchWorker>()
            .setInputData(workDataOf(MODE to start))
            .addTag("Marco")
            .setInitialDelay(delay, TimeUnit.SECONDS)
//        if (ACTION_START_SERVICE == start)
//            nextRequest.setConstraints(
//                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
//            )
        WorkManager.getInstance()
            .enqueueUniqueWork(start, ExistingWorkPolicy.KEEP, nextRequest.build())
    }

    private fun handleService(action: String) {
        Intent(applicationContext, ListenerService::class.java).also {
            it.action = action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (action == ACTION_START_SERVICE) applicationContext.startForegroundService(it)
                else applicationContext.stopService(it)
            } else {
                applicationContext.startService(it)
            }
        }
    }

    private fun calculateDelay(): Int {
        return 30
    }

}

