package com.test.ola.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.appcompat.app.AppCompatActivity.LOCATION_SERVICE
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

class MyHelper(private val context: Context) {


    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), 10
        )
    }


    fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnable(): Boolean {
        val location: LocationManager =
            context.getSystemService(LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(location)
    }

    fun onGPS(resultLauncher: ActivityResultLauncher<IntentSenderRequest>)  {

        // Creates a request for GPS location.
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).build()

        // This builder adds location requests to check settings.
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        // checkLocationSettings method check device gps is on or of
        val task =
            LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())

        // if user gps is off
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                   // ek resolution prompt create hota hai
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    // resolution prompt ko launch karne ke liye IntentSenderRequest ko resultLauncher.launch(intentSenderRequest) se isko execute karte hain.
                    resultLauncher.launch(intentSenderRequest)
                    // show a popup
                } catch (e: Exception) {
                    e.printStackTrace()
                 //   Toast.makeText(context, "GPS is already enabled", Toast.LENGTH_SHORT).show()
                }
            }
        }

        task.addOnSuccessListener {
            //   Toast.makeText(context, "GPS is already enabled", Toast.LENGTH_SHORT).show()
        }
    }


    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above, check for POST_NOTIFICATIONS permission
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 13, permission is not required, assume true
            true
        }
    }



}