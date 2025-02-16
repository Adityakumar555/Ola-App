package com.test.ola.models

data class RideAccept(
    val name: String? = "",
    val rideId: String? = "",
    val number: String? = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val userType: String? = "",
    val vehicleType: String? = "",
    val from: String? = "",
    val to: String? = "",
    val rideRequestUserNumber: String? = "",
    val rideRequestUserName: String? = "",
    val rideStatus: String? = "",
)
