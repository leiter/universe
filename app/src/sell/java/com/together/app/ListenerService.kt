package com.together.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.together.R
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.storage.getObservable
import com.together.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class ListenerService : Service() {


    private val disposable = CompositeDisposable()



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_SERVICE -> {
                    startInforeGround()
                    setUpOrderConnection()
                }

                ACTION_STOP_SERVICE -> {
                    disposable.clear()
                    stopForeground(true)
                    stopSelf()

                }

            }
        }



        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    private fun startInforeGround() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        val n = provideBaseNotificationBuilder(this)

        val i = Intent(this,ListenerService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }

        n.addAction(R.drawable.ic_clear_green,"Stop", PendingIntent.getService(this,2,i,FLAG_UPDATE_CURRENT))

        startForeground(NOTIFICATION_ID,n.build())
    }

    private fun setUpOrderConnection() {
        Database.orderSeller().orderByKey()
            .getObservable()
            .subscribeOn(Schedulers.io()).subscribe({ it ->
                val g = it.getValue(Result.Order::class.java)

                Log.e("TTTTT", "For debugging $it");
                Log.e("TTTTT", "For debugging ");
            }, {
                Log.e("TTTTT", "For debugging", it);
            }).addTo(disposable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}