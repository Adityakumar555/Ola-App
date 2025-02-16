package com.test.ola.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.test.ola.MainViewModel
import com.test.ola.databinding.ActivityUserProfileDetailsBinding
import com.test.ola.utils.AppSharedPrefrence

class UserProfileDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileDetailsBinding
    private val firestoreDB = FirebaseFirestore.getInstance()
    private val appSharedPreferences by lazy { AppSharedPrefrence.getInstance(this) }
    private lateinit var mainViewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        val number = appSharedPreferences?.getMobileNumber()?:""
        val selectedUserType = appSharedPreferences?.getUserType()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.progressBar.visibility = View.VISIBLE

        val collection = if (selectedUserType == "User") "User" else "Driver"

        mainViewModel.getUser(collection,number)
        mainViewModel.userIs.observe(this, Observer { user->
            binding.progressBar.visibility = View.GONE
            if (user != null){
                user.name?.let { appSharedPreferences?.saveUserName(it) }
                binding.nameInput.setText(user.name)
                binding.mobileInput.setText(user.number)
            }else{
                binding.nameInput.setText("")
                binding.mobileInput.setText("")
            }

        })


        binding.logoutBtn.setOnClickListener {
            appSharedPreferences?.clearAllData()
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }


        binding.updateProfile.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()

            if (name.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE

                val collection = if (selectedUserType == "User") "User" else "Driver"

                mainViewModel.updateProfile(collection,name,appSharedPreferences?.getMobileNumber())

                mainViewModel.isProfileUpdate.observe(this, Observer { profileUpdate->
                    binding.progressBar.visibility = View.GONE
                    if (profileUpdate){
                        Toast.makeText(this, "Profile Update Successfully.", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Profile Update failed.", Toast.LENGTH_SHORT).show()
                    }
                })

            } else {
                Toast.makeText(this, "fill all field.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
