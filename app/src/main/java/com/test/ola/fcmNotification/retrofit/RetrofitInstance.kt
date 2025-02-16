package com.test.ola.fcmNotification.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {


    private val retrofit: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }



    fun notificationCall(): NotificationCall? {
        return retrofit.create(NotificationCall::class.java)
    }

}