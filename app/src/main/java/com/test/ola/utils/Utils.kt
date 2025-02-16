package com.test.ola.utils

import android.util.Log
import android.widget.Toast
import com.test.ola.fcmNotification.models.Notification
import com.test.ola.fcmNotification.models.NotificationData
import com.test.ola.fcmNotification.retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object Utils {


    suspend fun sendNotification(title: String, content: String) {
        val notification = Notification(
            message = NotificationData(
                "/topics/all",
                hashMapOf(
                    "title" to title,
                    "body" to content
                )
            )
        )

        RetrofitInstance.notificationCall()?.notification(notification)?.enqueue(
            object : Callback<Notification?> {
                override fun onResponse(p0: Call<Notification?>, p1: Response<Notification?>) {
                    /*
                    Toast.makeText(this, "notification send", Toast.LENGTH_SHORT)
                        .show()*/
                }

                override fun onFailure(p0: Call<Notification?>, p1: Throwable) {
                   // Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
                }
            })

    }



}