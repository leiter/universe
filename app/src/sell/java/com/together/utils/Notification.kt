package com.together.utils

import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.together.R
import com.together.app.MainActivity

fun provideBaseNotificationBuilder(
    context: Context,
    pendingIntent: PendingIntent = provideActivityPendingIntent(context)
): NotificationCompat.Builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
    .setAutoCancel(true)
    .setSmallIcon(R.drawable.ic_add_shopping_cart_black)
    .setContentTitle("Bodenschätze Bestellungen")
    .setContentIntent(pendingIntent)

fun provideActivityPendingIntent(
    context: Context
): PendingIntent =
    PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            action = ACTION_SHOW_ORDER_FRAGMENT
        },
        PendingIntent.FLAG_CANCEL_CURRENT
    )

fun Context.isThisServiceRunning(clazz: Class<*>): Boolean {
    val manager = getSystemService<ActivityManager>()
    return manager?.let { activityManager ->
        activityManager.getRunningServices(Integer.MAX_VALUE).forEach {
            if(clazz.name == it.service.className) { return@let true }
        }
        return@let false
    } ?: false
}