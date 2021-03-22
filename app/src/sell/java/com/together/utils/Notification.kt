package com.together.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.together.R
import com.together.app.MainActivity

fun provideBaseNotificationBuilder(
    context: Context,
    pendingIntent: PendingIntent?=null,
    orderlist: String = ""
) = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
    .setAutoCancel(true)
    .setSmallIcon(R.drawable.ic_add_shopping_cart_black)
    .setContentTitle("Bodenschätze")
//    .setContentText(orderlist)
//    .setContentIntent(pendingIntent)

fun provideActivityPendingIntent(
    context: Context
) =
    PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            action = ACTION_SHOW_ORDER_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )