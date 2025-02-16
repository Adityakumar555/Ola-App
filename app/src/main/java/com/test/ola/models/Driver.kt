package com.test.ola.models

data class Driver(
    val name: String? = "",
    val number: String? = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val userType: String? = "",
    val vehicleType: String? = "",
)