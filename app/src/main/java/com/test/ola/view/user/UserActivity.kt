package com.test.ola.view.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.test.ola.MainViewModel
import com.test.ola.databinding.ActivityUserBinding
import com.test.ola.models.Driver
import com.test.ola.utils.AppSharedPrefrence
import com.test.ola.utils.EnableAppLocationPermissionDialogFragment
import com.test.ola.utils.MyHelper
import com.test.ola.view.UserProfileDetailsActivity

class UserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding

    private val appSharedPrefrence by lazy { AppSharedPrefrence.getInstance(this) }
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var mainViewModel: MainViewModel

    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0

    private val myHelper by lazy { MyHelper(this) }
    private lateinit var resultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Granted
        } else {
            // Permission Denied / Cancel
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appSharedPrefrence?.visitMainActivity(true)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        checkPermission()
        if(!myHelper.checkNotificationPermission()){
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        binding.profile.setOnClickListener {
            startActivity(Intent(this, UserProfileDetailsActivity::class.java))
        }

        // for get access toke
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        binding.book.setOnClickListener {

            if (myHelper.isLocationEnable()) {
                if (myHelper.checkLocationPermission()) {

                    val from = binding.from.text.toString().trim()
                    val to = binding.to.text.toString().trim()

                    if (from.isEmpty() || to.isEmpty()) {
                        binding.from.requestFocus()
                        Toast.makeText(this, "Enter correct destination.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    var vehicle: String? = null

                    val selectedVehicle = binding.vehicleTypeGroup.checkedRadioButtonId

                    if (selectedVehicle == binding.car.id) {
                        vehicle = "Car"
                    } else if (selectedVehicle == binding.auto.id) {
                        vehicle = "Auto"
                    } else if (selectedVehicle == binding.bike.id) {
                        vehicle = "Bike"
                    }

                    firestore.collection("Driver")
                        .whereEqualTo("vehicleType", vehicle)
                        .get()
                        .addOnSuccessListener { data ->

                            if (data.isEmpty) {
                                Toast.makeText(this, "Vehicle Not Found.", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            val nearbyDriver = mutableListOf<Driver>()

                            for (i in data.documents) {

                                val latitude = i.getDouble("latitude")
                                val longitude = i.getDouble("longitude")
                                val deviceToken = i.get("deviceToken")

                                Log.d("driverLocationn", "${latitude},${longitude}")
                                Log.d("userLocationn", "${userLatitude},${userLongitude}")

                                if (latitude != null && longitude != null) {
                                    val radious = calculateDistance(
                                        latitude, longitude,
                                        userLatitude, userLongitude
                                    )

                                    if (radious <= 1000) {

                                        val dr = i.toObject(Driver::class.java)
                                        if (dr != null) {
                                            nearbyDriver.add(dr)

                                            if (deviceToken != null) {
                                                mainViewModel.sendNotification(
                                                    deviceToken.toString(),
                                                    "You have new Ride",
                                                    "You have a new ride request $from to $to"
                                                )
                                            }

                                            mainViewModel.saveNearEstDriver(dr,
                                                from,
                                                to,
                                                appSharedPrefrence?.getMobileNumber(),
                                                appSharedPrefrence?.getUserName()
                                            )

                                        }
                                    } else {

                                        Toast.makeText(this, "driver not found in 1km area.", Toast.LENGTH_SHORT).show()
                                        return@addOnSuccessListener
                                    }
                                }
                            }

                            Toast.makeText(
                                this,
                                "ride request send,wait for driver acceptence.",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.d("driverDatass", "${nearbyDriver}")
                        }

                } else {
                    myHelper.requestLocationPermission(this)
                }
            } else {
                myHelper.onGPS(resultLauncher)
            }

        }


    }

    private fun checkPermission() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                } else {
                }
            }

        if (myHelper.isLocationEnable()) {
            if (myHelper.checkLocationPermission()) {
                updateCurrentLocation()
                getCurrentUserLatLong()
            } else {
                myHelper.requestLocationPermission(this)
            }
        } else {
            myHelper.onGPS(resultLauncher)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateCurrentLocation()
            getCurrentUserLatLong()
            if(!myHelper.checkNotificationPermission()){
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (shouldShowRequestPermissionRationale(permissions[0])) {
                val progressDialog = EnableAppLocationPermissionDialogFragment()
                progressDialog.show(supportFragmentManager, "requestLocationPermission")
            }
        }
    }

    private fun getCurrentUserLatLong() {
        appSharedPrefrence?.getMobileNumber()?.let { it1 ->
            firestore.collection("User")
                .document(it1)
                .get()
                .addOnSuccessListener { data ->

                    userLatitude = data.getDouble("latitude") ?: 0.0
                    userLongitude = data.getDouble("longitude") ?: 0.0

                    // Log.d("driverLocationn", "${latitude},${longitude}")
                    Log.d("userLocationn", "${userLatitude},${userLongitude}")

                }
        }
    }


    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val earthRadius = 6371000 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }

    private fun updateCurrentLocation() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
            val location: Location? = task.result
            if (location != null) {
                appSharedPrefrence?.getMobileNumber()?.let {
                    mainViewModel.updateLatitudeAndLongitude(
                        it,
                        location.latitude,
                        location.longitude
                    )
                }
            }
        }
    }
}