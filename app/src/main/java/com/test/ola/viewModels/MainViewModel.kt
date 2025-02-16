package com.test.ola.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.test.ola.fcmNotification.models.Notification
import com.test.ola.fcmNotification.models.NotificationData
import com.test.ola.fcmNotification.retrofit.RetrofitInstance
import com.test.ola.models.Driver
import com.test.ola.models.RideAccept
import com.test.ola.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private var isUserSave = MutableLiveData<Boolean>()
    val userSave: LiveData<Boolean> = isUserSave

    private var profileUpdate = MutableLiveData<Boolean>()
    val isProfileUpdate: LiveData<Boolean> = profileUpdate

    private var setRides = MutableLiveData<List<RideAccept>>()
    val acceptedRide: LiveData<List<RideAccept>> = setRides

    private var setUser = MutableLiveData<User>()
    val userIs: LiveData<User> = setUser

    private var setCurrentDeviceToken = MutableLiveData<String>()
    val currentDeviceTokenIs: LiveData<String> = setCurrentDeviceToken


    fun getCurrentToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            // Get new FCM registration token
            setCurrentDeviceToken.postValue(task.result)
            // token = task.result
        })
    }

    fun saveData(
        number: String,
        name: String,
        userType: String,
        vehicleType: String,
        password: String,
        latitude: Double,
        longitude: Double,
        token: String?
    ) {

        val user = if (userType == "User") {
            hashMapOf(
                "name" to name,
                "number" to number,
                "userType" to userType,
                "password" to password,
                "latitude" to latitude,
                "longitude" to longitude,
                "deviceToken" to token
            )
        } else {
            hashMapOf(
                "name" to name,
                "number" to number,
                "userType" to userType,
                "vehicleType" to vehicleType,
                "password" to password,
                "latitude" to latitude,
                "longitude" to longitude,
                "deviceToken" to token
            )
        }



        firestore.collection(userType)
            .document(number)
            .set(user)
            .addOnSuccessListener {
                isUserSave.value = true

            }
            .addOnFailureListener {
                isUserSave.value = false
            }
    }


    fun updateLatitudeAndLongitude(number: String, latitude: Double, longitude: Double) {
        firestore.collection("Driver")
            .document(number)
            .update("latitude", latitude, "longitude", longitude)
    }

    fun getUser(collection: String, number: String) {
        firestore.collection(collection)
            .document(number)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    setUser.postValue(user)
                }
            }

    }

    fun updateProfile(collection: String, name: String, number: String?) {

        if (number != null) {
            firestore.collection(collection)
                .document(number)
                .update(
                    "name", name,
                )
                .addOnSuccessListener {
                    profileUpdate.value = true
                }
                .addOnFailureListener { e ->
                    profileUpdate.value = false
                }
        }
    }

    fun saveNearEstDriver(
        nearbyDriver: Driver,
        from: String,
        to: String,
        mobileNumber: String?,
        userName: String?
    ) {
        val nearDriver = hashMapOf(
            "name" to nearbyDriver.name,
            "number" to nearbyDriver.number,
            "vehicleType" to nearbyDriver.vehicleType!!,
            "latitude" to nearbyDriver.latitude,
            "longitude" to nearbyDriver.longitude,
            "from" to from,
            "to" to to,
            "rideRequestUserNumber" to mobileNumber,
            "rideRequestUserName" to userName,
            )

        firestore.collection("NearestDriver")
            .add(nearDriver)

    }


    fun sendNotification(token: String, title: String, content: String) {
        val notification = Notification(
            message = NotificationData(
                token,
                hashMapOf(
                    "title" to title,
                    "body" to content
                )
            )
        )

        RetrofitInstance.notificationCall()?.notification(notification)?.enqueue(
            object : Callback<Notification?> {
                override fun onResponse(p0: Call<Notification?>, p1: Response<Notification?>) {
                    /*Toast.makeText(this@DriverActivity, "notification send", Toast.LENGTH_SHORT)
                        .show()*/
                }

                override fun onFailure(p0: Call<Notification?>, p1: Throwable) {
                    //   Toast.makeText(this@DriverActivity, "error", Toast.LENGTH_SHORT).show()
                }
            })
    }


    fun fetchRideIfAnyOneAccept(mobileNumber: String?) {
        firestore.collection("RideAccepted")
            .whereEqualTo("number", mobileNumber)
            .addSnapshotListener { data, error ->
                if (data?.isEmpty == true) {
                    val rides = data.toObjects(RideAccept::class.java)
                    setRides.postValue(rides)
                    return@addSnapshotListener
                }

                val rides = data?.toObjects(RideAccept::class.java)
                setRides.postValue(rides)
            }
    }

}