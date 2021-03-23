package com.together.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.together.R
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.changed
import com.together.repository.storage.getObservable
import com.together.repository.storage.parseChildren
import com.together.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class ListenerService : Service() {

    private val disposable = CompositeDisposable()

    var openOrders: MutableList<Result.Order> = mutableListOf()

    var orderMap: MutableMap<String, List<Result.Order>> = mutableMapOf()

    override fun onCreate() {
        super.onCreate()
//        android.os.Debug.waitForDebugger()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_SERVICE -> {
                    startForeGround()
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
        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForeGround() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        val n = provideBaseNotificationBuilder(this)

        val i = Intent(this, ListenerService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }

        n.addAction(
            R.drawable.ic_stat_name, "Ausschalten",
            PendingIntent.getService(this, 2, i, FLAG_UPDATE_CURRENT)
        )
        n.setNotificationSilent()
        startForeground(NOTIFICATION_ID, n.build())
    }

    private fun setUpOrderConnection() {

        val day1 = Database.orderSeller().orderByKey().startAt("20210325").getObservable()
        val day2 = Database.orderSeller().orderByKey().startAt("20210326").getObservable()

        day1.concatWith(day2).subscribeOn(Schedulers.io())
            .subscribe({ dataSnapshot ->
                val list = dataSnapshot.parseChildren<Result.Order>()
                orderMap[dataSnapshot.key!!] = list
                manageOrders(list)

            }, { Log.e("TTTTT", "For debugging", it) }).addTo(disposable)
    }

    private fun manageOrders(list: List<Result.Order>) {
        var addedItemCount = 0
        var alteredItemCount = 0
        var firstTime = false
        if(openOrders.isEmpty()) firstTime = true
        list.forEach { newItem ->
            val g = openOrders.find { it.id ==  newItem.id }
            g?.let { if(g.changed(newItem))
                alteredItemCount++
            } ?: run {
                addedItemCount++
                openOrders.add(newItem)
            }
        }
        val text = if (firstTime) "Es liegen ${openOrders.size} Bestellungen vor."
                else "Es bestehen ${openOrders.size} Bestellungen. " +
                "$addedItemCount neue Bestellungen und $alteredItemCount geänderte."

        val n = provideBaseNotificationBuilder(this)

        val i = Intent(this, ListenerService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        n.setContentIntent(PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                action = ACTION_SHOW_ORDER_FRAGMENT
            },
            FLAG_CANCEL_CURRENT
        ))

        n.addAction(
            R.drawable.ic_add_shopping_cart_black, "Ausschalten",
            PendingIntent.getService(this, 2, i, FLAG_UPDATE_CURRENT)
        )
        n.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        n.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, n.build())
        Log.e("TTTTT", "Altered $alteredItemCount,   Added $addedItemCount")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}