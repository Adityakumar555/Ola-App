package com.test.ola.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID


class DriverViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val isRideAccept = MutableLiveData<Boolean>()
    val rideAccept:LiveData<Boolean> = isRideAccept

    fun deleteRequest(driverNumber: String?, rideRequestUserNumber: String?) {
        firestore.collection("NearestDriver")
            .whereEqualTo("number", driverNumber)
            .whereEqualTo("rideRequestUserNumber", rideRequestUserNumber)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        Log.d("deletedrivers", document.id)
                        firestore.collection("NearestDriver")
                            .document(document.id)
                            .delete()
                    }
                }
            }
    }

    fun rideAccepted(mobileNumber: String?, driver: DocumentSnapshot?) {

        val name = driver?.get("name")
        val number = driver?.get("number")
        val vehicleType = driver?.get("vehicleType")
        val latitude = driver?.get("latitude")
        val longitude = driver?.get("longitude")
        val from = driver?.get("from")
        val to = driver?.get("to")
        val rideRequestUserNumber = driver?.get("rideRequestUserNumber")
        val rideRequestUserName = driver?.get("rideRequestUserName")

        val rideId = UUID.randomUUID().toString()

        val saveRide = hashMapOf(
            "rideId" to rideId,
            "name" to name,
            "number" to number,
            "vehicleType" to vehicleType!!,
            "latitude" to latitude,
            "longitude" to longitude,
            "from" to from,
            "to" to to,
            "rideRequestUserNumber" to rideRequestUserNumber,
            "rideRequestUserName" to rideRequestUserName,
            "rideStatus" to "Pending",
            )

        firestore.collection("RideAccepted")
            .document(rideId)
            .set(saveRide)
            .addOnSuccessListener {
                isRideAccept.value = true
            }
            .addOnFailureListener {
                isRideAccept.value = false
            }


    }

    fun deleteAllRideToRideRequestUserNumber(mobileNumber: String?, rideRequestUserNumber: String?) {
        firestore.collection("NearestDriver")
            .whereEqualTo("rideRequestUserNumber",rideRequestUserNumber)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        Log.d("deletedrivers", document.id)
                        firestore.collection("NearestDriver")
                            .document(document.id)
                            .delete()
                    }
                }
            }

    }

    fun rideComplete(rideId: String) {
        firestore.collection("RideAccepted")
            .document(rideId)
            .update( "rideStatus" ,"Complete")
    }

    fun rideDelete(rideId: String) {
        firestore.collection("RideAccepted")
            .document(rideId)
            .delete()
    }


}