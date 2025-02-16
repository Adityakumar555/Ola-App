package com.test.ola.fcmNotification.retrofit

import com.test.ola.fcmNotification.AccessTokens
import com.test.ola.fcmNotification.models.Notification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationCall {

    @POST ( "/v1/projects/ola-app-a8080/messages:send")
    @Headers("Content-Type: application/json")
    fun notification(
        @Body message: Notification,
        @Header("Authorization") accessToken: String="Bearer ${AccessTokens.getAccessToken()}"
    ): Call<Notification>
}