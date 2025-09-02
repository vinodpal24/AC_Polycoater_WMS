package com.example.acPolycoaters.Retrofit_Api

import com.example.acPolycoaters.Global_Classes.AppConstants.IS_TEST_ENVIRONMENT
import com.example.acPolycoaters.Global_Classes.AppConstants.isTestEnvUIVisible
import com.example.acPolycoaters.Global_Classes.AppConstants.isTestForClient
import com.pixplicity.easyprefs.library.Prefs

class ApiConstantForURL {
     private val isDevelopment: Boolean = if (isTestEnvUIVisible) !Prefs.getBoolean(IS_TEST_ENVIRONMENT) else false
     val CUSTOM_API_BASE_URL = "http://103.197.76.73:8000/ACPOLY/"
     //val CUSTOM_API_BASE_URL_NEW = "http://103.197.76.72:9090/api/"

     val CUSTOM_API_BASE_URL_NEW: String

          get() {
               val port = if (isDevelopment || isTestForClient) 9092 else 9090
               val ip = "103.197.76.72"
               return "http://$ip:$port/api/"
          }
}