package com.test.ola.fcmNotification

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AccessTokens {
    private val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"

    fun getAccessToken():String?{
        try {
            val jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"ola-app-a8080\",\n" +
                    "  \"private_key_id\": \"9826ba1bdb28123b78c1dd310ae5f3ead9286040\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDSYNo/FLed1BvD\\nsKhad7ZHO5wm5OTSU3zopRiZTtQAawZ08CKYbP/Fp4v4mpWK+r6sVXUnFM1OReB/\\nWhPWg61j/0LQg+1rZZGi088ozYGO5hOto0muOZ2gJNCa6FnUv2YyKaXYY4cnpH//\\nJtvtOI3ZFUiqWqU+47tMn4Kv8LbSc7nTdCbphz0S/OG2sP7bVzt6ZygJDKiQ3E2E\\nWsDzr182geAwYfvVRoC0U/0nIo6EFKi2YxGRupi40ZTmzpgX4bbOg+WzaJSxpW9X\\nVnnQRS0pgfj171ivtSRPR/ql1/v/c2b0urIO7nVMFbZmKuTQgA7j0iOrzkX95OYP\\n6SqBRXLhAgMBAAECggEACZsEGgFb1yIpNJ6ZODy7Kw5T5VN6BmerFkr7+BuBGuYI\\nFuCkPrwK01swnRSvT2e+mxshiMvWiixu+TebXGX5na1I72D3rxm1jCcCQFtePxBe\\nnYTtKz4cliRaLTl+yQPGcnPD+5DjSdZFJRjwzZhxgxP5g/Q0s2311kQ59QcvTMVo\\nT0nxODXZbI5gQFFdN08dwhIx3wrPmNM7ZyqkJjh9TOTxdoZqJOUbmtC+jRbwghjO\\nsmPiEi6gzyErLPa7YehBVCKYcy9RGfoRK6HV+PiOgsyGuZIf0r/H9CiiTGnL16zd\\nJ8ViQpF1rxgcD6GJYwp61Ts1QpuX9nBxRaEUEmRyUQKBgQD2qUd/ytDEGXTjbLmk\\nofo5ReVhHKAhsABU+rdZcWGlj6NVCmZC7ttEOn0cnLXv0sS5QOJVQT0//v+dWKe7\\nCE/aKmEGz2KEiInTDTP9BBgWfcDACp1io/FguTlOTLrjyjKufSWcVfgq6AQr6ziH\\n3K3yD6+UmXPOax81kAkYzcNzcQKBgQDaV+hOaQk487sS8Eq7MD1VFupGE78ZlCN6\\nhwO0Qu9KFy3R+PohYwEwQlmc42DdNQENW9dJOX8TDackquZoT/wDLmgFnd/fGme2\\nhArl6ljRcyn2n6wp+ftzul3rgjKP+ovNVfOV8BT3Yl6p+v6YeA8Z2orm0lRt+lo3\\npSFSvVxecQKBgCyEWLcjr5H01jfFg6vG4BY+Gyvvsqbh0O2FYlnuMKVkyyGqAtLJ\\nsSEbPUFQtzVVYNPedfrVD2zhZNjNC1iiI2dIhsJn42vwCI/iriNX/dDxWTxPriAD\\nXqkKROFwz7oU4BjF1WflzChSP459oXJDzdEGK7YyC8iRZMyR9lJXsJLBAoGBAKnD\\nDZjha80/G6Wm9MPDxvwAfBgORFfT6R3cGdIu9LPrf7Gw3nHU81idTeWAmJCFJJNJ\\nZALH1Hhw9hK5Wbmi7rNegnxTenuRoS+0THDkmzgGTYs2LHrapVoefeWdbkipqcb1\\n3BUo/HQ3pJiO0SKkvkEAvVyoDgMQUtLO7jDRkB7RAoGBAMzweproWTZrVoTll2aZ\\n7oc2+WboL/jNLPnF7/kgc/3ybu4MYCbhnF79TkGjGNAaw5ig/gM4AzGWwTFxTVXZ\\njBtkSsvaByXQO57plBOAobqFcjYKJTWDoxlw/M6njTQI/xiZhtapKW0+eT6CpFHT\\nfa2dkz1qVqBXDo+P5nehT9zI\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-fbsvc@ola-app-a8080.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"103827681405485880950\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40ola-app-a8080.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}"

            val stream= ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredentials = GoogleCredentials.fromStream(stream).createScoped(arrayListOf(
                firebaseMessagingScope
            ))

            googleCredentials.refresh()
            return googleCredentials.accessToken.tokenValue
        }catch (e: IOException){
            return null
        }
    }
}