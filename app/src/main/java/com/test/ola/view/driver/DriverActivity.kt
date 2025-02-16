package com.test.ola.view.driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.test.ola.viewModels.DriverViewModel
import com.test.ola.viewModels.MainViewModel
import com.test.ola.databinding.ActivityDriverBinding
import com.test.ola.utils.AppSharedPrefrence
import com.test.ola.utils.EnableAppLocationPermissionDialogFragment
import com.test.ola.utils.MyHelper
import com.test.ola.view.UserProfileDetailsActivity
import com.test.ola.view.driver.adapter.ShowAllRidesAdapter
import com.test.ola.view.driver.interfaces.DriverRideClickListener

class DriverActivity : AppCompatActivity(),DriverRideClickListener {

    private lateinit var binding: ActivityDriverBinding
    private val appSharedPrefrence by lazy { AppSharedPrefrence.getInstance(this) }
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var mainViewModel: MainViewModel
    private lateinit var driverViewModel: DriverViewModel

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
        binding = ActivityDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appSharedPrefrence?.visitMainActivity(true)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        driverViewModel = ViewModelProvider(this)[DriverViewModel::class.java]

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        binding.profile.setOnClickListener {
            startActivity(Intent(this, UserProfileDetailsActivity::class.java))
        }

        checkPermission()
        if(!myHelper.checkNotificationPermission()){
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // for get access toke
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        driverViewModel.rideAccept.observe(this, Observer { rideAccepted ->
            if (rideAccepted) {
                Toast.makeText(this, "Ride accepted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ride Not Accept.", Toast.LENGTH_SHORT).show()
            }
        })

        firestore.collection("NearestDriver")
            .whereEqualTo("number", appSharedPrefrence?.getMobileNumber())
            //.get()
            .addSnapshotListener { querySnapshot, exception ->

                if (querySnapshot != null) {
                    for (driver in querySnapshot.documents) {

                        val from = driver.get("from")
                        val to = driver.get("to")
                        val rideRequestUserNumber = driver.getString("rideRequestUserNumber")
                        val driverName = driver.getString("name")


                        if (rideRequestUserNumber != null) {
                            firestore.collection("User")
                                .document(rideRequestUserNumber)
                                .get()
                                .addOnSuccessListener { data ->

                                    val token = data.getString("deviceToken")


                                    val builder = AlertDialog.Builder(this)
                                    builder.setTitle("Ride Request")
                                    builder.setMessage("You have a new ride request $from to $to")

                                    builder.setPositiveButton("Accept") { dialog, which ->

                                        if (token != null) {
                                            mainViewModel.sendNotification(
                                                token,
                                                "Ride Accepted.",
                                                "Your Ride Accepted by $driverName , Enjoy Ride..."
                                            )
                                        }

                                        driverViewModel.rideAccepted(
                                            appSharedPrefrence?.getMobileNumber(),
                                            driver
                                        )
                                        driverViewModel.deleteAllRideToRideRequestUserNumber(
                                            appSharedPrefrence?.getMobileNumber(),
                                            rideRequestUserNumber
                                        )

                                        dialog.dismiss()
                                    }

                                    builder.setNegativeButton("Cancel") { dialog, which ->

                                        driverViewModel.deleteRequest(
                                            appSharedPrefrence?.getMobileNumber(),
                                            rideRequestUserNumber
                                        )

                                        Toast.makeText(
                                            this,
                                            "Ride Cancel", Toast.LENGTH_SHORT
                                        ).show()
                                        dialog.dismiss()
                                    }

                                    try {
                                        builder.show()
                                    }
                                    catch (e: Exception) {
                                    }
                                }
                        }
                    }
                }
            }


        val showAllRidesAdapter = ShowAllRidesAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = showAllRidesAdapter


        mainViewModel.fetchRideIfAnyOneAccept(appSharedPrefrence?.getMobileNumber())
       mainViewModel.acceptedRide.observe(this, Observer { rideData->

           if (rideData.isEmpty()){
               binding.noRideRequestFound.visibility = View.VISIBLE
               binding.recyclerView.visibility = View.GONE
           }else{
               binding.noRideRequestFound.visibility = View.GONE
               binding.recyclerView.visibility = View.VISIBLE

               showAllRidesAdapter.updateRideList(rideData)
               showAllRidesAdapter.notifyDataSetChanged()
           }


       })

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

        } else {
            if (shouldShowRequestPermissionRationale(permissions[0])) {
                val progressDialog = EnableAppLocationPermissionDialogFragment()
                progressDialog.show(supportFragmentManager, "requestLocationPermission")
            }
        }
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

    override fun rideComplete(rideId: String) {
        driverViewModel.rideComplete(rideId)
    }

    override fun rideCancel(rideId: String) {
        driverViewModel.rideDelete(rideId)
    }
}