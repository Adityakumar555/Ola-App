package com.test.ola.utils

import android.content.Context

class AppSharedPrefrence private constructor(context: Context) {


    private val sharedPreference = context.getSharedPreferences("OLA",Context.MODE_PRIVATE)

    companion object{

        private var instance: AppSharedPrefrence? = null

        fun getInstance(context: Context): AppSharedPrefrence? {

            if (instance == null){
                synchronized(this){
                    if (instance == null){
                        instance = AppSharedPrefrence(context)
                    }
                }
            }
            return instance
        }
    }

    fun saveUserName(userName: String) {
        sharedPreference.edit().putString(PrefKeys.USER_NAME, userName).apply()
    }

    fun getUserName(): String? {
        return sharedPreference.getString(PrefKeys.USER_NAME, null)
    }

    fun saveUserType(value: String) {
        sharedPreference.edit().putString(PrefKeys.USER_TYPE, value).apply()
    }

    fun saveMobileNumber(value: String) {
        sharedPreference.edit().putString(PrefKeys.USER_NUMBER, value).apply()
    }

    fun visitMainActivity(value: Boolean) {
        sharedPreference.edit().putBoolean(PrefKeys.VISITED_MAIN_ACTIVITY, value).apply()
    }

    fun getMobileNumber(): String? {
        return sharedPreference.getString(PrefKeys.USER_NUMBER, null)
    }

    fun getUserType(): String? {
        return sharedPreference.getString(PrefKeys.USER_TYPE, null)
    }

    fun isMainActivityVisit(): Boolean {
        return sharedPreference.getBoolean(PrefKeys.VISITED_MAIN_ACTIVITY, false)
    }

    fun clearAllData() {
        sharedPreference.edit().clear().apply()
    }

}