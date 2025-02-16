package com.test.ola.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.test.ola.MainViewModel
import com.test.ola.databinding.ActivitySignInBinding
import com.test.ola.utils.AppSharedPrefrence
import com.test.ola.utils.EnableAppLocationPermissionDialogFragment
import com.test.ola.utils.MyHelper
import com.test.ola.view.driver.DriverActivity
import com.test.ola.view.user.UserActivity

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val appSharedPreference by lazy { AppSharedPrefrence.getInstance(this) }

    private lateinit var mainViewModel: MainViewModel

    private var token:String? = null

    private lateinit var resultLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val myHelper by lazy { MyHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        binding.userRadioGroup.setOnCheckedChangeListener { group, checkId ->
            val selectedUser: RadioButton = findViewById(checkId)
            if (selectedUser == binding.user) {
                binding.driverView.visibility = View.GONE
            } else {
                binding.driverView.visibility = View.VISIBLE
            }
        }


        mainViewModel.getCurrentToken()
        mainViewModel.currentDeviceTokenIs.observe(this, Observer { tokenIs->
            token = tokenIs
        })

        checkPermission()

        binding.save.setOnClickListener {

            if (myHelper.isLocationEnable()) {
                if (myHelper.checkLocationPermission()) {
                    val name = binding.name.text.toString().trim()
                    val number = binding.number.text.toString().trim()
                    val password = binding.password.text.toString().trim()

                    if (number.isEmpty() || name.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Enter all Details.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    } else {
                        fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                            val location: Location? = task.result
                            if (location != null) {
                                val latitude = location.latitude
                                val longitude = location.longitude
                                appSharedPreference?.saveUserName(name)

                                var userType: String

                                val userTypeId: Int = binding.userRadioGroup.checkedRadioButtonId

                                if (userTypeId == binding.user.id) {
                                    userType = "User"

                                    mainViewModel.saveData(
                                        number,
                                        name,
                                        userType,
                                        "",
                                        password,
                                        latitude,
                                        longitude,
                                        token
                                    )
                                    mainViewModel.userSave.observe(this, Observer { isSuccess ->
                                        if (isSuccess) {
                                            appSharedPreference?.saveMobileNumber(number)
                                            appSharedPreference?.saveUserType(userType)
                                            navigateToUserActivity()

                                            Toast.makeText(
                                                this,
                                                "User Register Successfully.",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        } else {

                                            Toast.makeText(this, "User Not Register.", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    })

                                } else {

                                    userType = "Driver"

                                    var vehicleType: String? = null

                                    val vehicleTypeId: Int = binding.vehicleTypeGroup.checkedRadioButtonId

                                    when (vehicleTypeId) {
                                        binding.car.id -> {
                                            vehicleType = "Car"
                                        }

                                        binding.auto.id -> {
                                            vehicleType = "Auto"
                                        }

                                        binding.bike.id -> {
                                            vehicleType = "Bike"
                                        }
                                    }

                                    if (vehicleType != null) {
                                        mainViewModel.saveData(
                                            number,
                                            name,
                                            userType,
                                            vehicleType,
                                            password,
                                            latitude,
                                            longitude,
                                            token
                                        )
                                    }

                                    mainViewModel.userSave.observe(this, Observer { isSuccess ->

                                        if (isSuccess) {
                                            appSharedPreference?.saveMobileNumber(number)
                                            appSharedPreference?.saveUserType(userType)

                                            navigateToDriverActivity()

                                            Toast.makeText(
                                                this,
                                                "Driver Register Successfully.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {

                                            Toast.makeText(this, "Driver Not Register.", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    })
                                }
                            }
                        }
                    }
                }else{
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

        } else {
            if (shouldShowRequestPermissionRationale(permissions[0])) {
                val progressDialog = EnableAppLocationPermissionDialogFragment()
                progressDialog.show(supportFragmentManager, "requestLocationPermission")
            }
        }
    }

    private fun navigateToDriverActivity() {
        val intent = Intent(this, DriverActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun navigateToUserActivity() {
        val intent = Intent(this, UserActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}