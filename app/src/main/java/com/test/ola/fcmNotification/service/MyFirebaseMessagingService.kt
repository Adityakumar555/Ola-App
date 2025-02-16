
package com.test.ola.fcmNotification.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.test.ola.R

class MyFirebaseMessagingService:FirebaseMessagingService() {

    private val channelID = "fcm_notification"
    private val channelName = "fcmApp"


    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val channel = NotificationChannel(
            channelID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH,
        )

        val notification = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(getColor(R.color.black))
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        //NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)



    }



}