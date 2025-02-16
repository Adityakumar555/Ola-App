package com.test.ola.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.test.ola.viewModels.MainViewModel
import com.test.ola.databinding.ActivitySignUpBinding
import com.test.ola.utils.AppSharedPrefrence
import com.test.ola.utils.MyHelper
import com.test.ola.view.driver.DriverActivity
import com.test.ola.view.user.UserActivity

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val appSharedPreference by lazy { AppSharedPrefrence.getInstance(this) }
    private lateinit var mainViewModel: MainViewModel
    private var token: String? = null

    private lateinit var resultLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val myHelper by lazy { MyHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]


        if (appSharedPreference?.isMainActivityVisit() == true) {
            if (appSharedPreference?.getUserType() == "User") {
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, DriverActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        mainViewModel.getCurrentToken()
        mainViewModel.currentDeviceTokenIs.observe(this, Observer { tokenIs ->
            token = tokenIs
        })


        binding.signin.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        checkPermission()


        binding.login.setOnClickListener {

            if (myHelper.isLocationEnable()) {
                if (myHelper.checkLocationPermission()) {

                    val number = binding.number.text.toString().trim()
                    val password = binding.password.text.toString().trim()

                    if (number.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Enter correct credential.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    } else {

                        var userType: String? = null
                        val userTypeId: Int = binding.userRadioGroup.checkedRadioButtonId
                        if (userTypeId == binding.user.id) {
                            userType = "User"
                        } else {
                            userType = "Driver"
                        }

                        firestore.collection(userType)
                            .document(number)
                            .get()
                            .addOnSuccessListener { data ->

                                if (!data.exists()) {
                                    Toast.makeText(this, "Enter correct Details.", Toast.LENGTH_SHORT)
                                        .show()
                                    return@addOnSuccessListener
                                }

                                val getDbSavedPassword = data.get("password")
                                val name = data.get("name")
                                val savedCurrentDeviceToken = data.get("deviceToken")

                                if (password == getDbSavedPassword) {
                                    if (savedCurrentDeviceToken != token) {
                                        firestore.collection(userType)
                                            .document(number)
                                            .update("deviceToken", token)
                                    }

                                    appSharedPreference?.saveUserName(name.toString())

                                    appSharedPreference?.saveMobileNumber(number)
                                    appSharedPreference?.saveUserType(userType)

                                    if (userType == "User") {
                                        val intent = Intent(this, UserActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        val intent = Intent(this, DriverActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }

                                } else {
                                    Toast.makeText(this, "Enter correct Password.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Enter correct Details.", Toast.LENGTH_SHORT).show()
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
}