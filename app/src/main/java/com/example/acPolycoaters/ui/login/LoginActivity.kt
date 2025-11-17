package com.example.acPolycoaters.ui.login

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.acPolycoaters.Global_Classes.AppConstants
import com.example.acPolycoaters.Global_Classes.AppConstants.isTestEnvUIVisible
import com.example.acPolycoaters.Global_Classes.GlobalMethods
import com.example.acPolycoaters.Global_Classes.MaterialProgressDialog
import com.example.acPolycoaters.Global_Notification.NetworkConnection
import com.example.acPolycoaters.Model.DatabaseModel
import com.example.acPolycoaters.ui.login.Model.LoginResponseModel
import com.example.acPolycoaters.Model.OtpErrorModel
import com.example.acPolycoaters.Retrofit_Api.ApiConstantForURL
import com.example.acPolycoaters.Retrofit_Api.NetworkClients
import com.example.acPolycoaters.Retrofit_Api.QuantityNetworkClient
import com.example.acPolycoaters.SessionManagement.SessionManagement
import com.example.acPolycoaters.Validation.Validation
import com.example.acPolycoaters.databinding.ActivityLoginBinding
import com.example.acPolycoaters.ui.home.HomeActivity
import com.example.acPolycoaters.ui.setting.SettingActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.pixplicity.easyprefs.library.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class LoginActivity : AppCompatActivity() {
    private lateinit var activityLoginBinding: ActivityLoginBinding

    //    private var loginViewModel: LoginViewModel? = null
    lateinit var validation: Validation
    lateinit var networkConnection: NetworkConnection
    lateinit var materialProgressDialog: MaterialProgressDialog
    private lateinit var sessionManagement: SessionManagement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(activityLoginBinding.root)

        supportActionBar?.hide()

        //todo initialization...
        validation = Validation()
        networkConnection = NetworkConnection()
        materialProgressDialog = MaterialProgressDialog(this@LoginActivity)
        sessionManagement = SessionManagement(this@LoginActivity)
        callDatabaseApi()
        //setStaticDbName(dbResponse)
        activityLoginBinding.apply {
            switchForEnvironment.visibility = if (isTestEnvUIVisible) View.VISIBLE else View.GONE

            if (Prefs.getBoolean(AppConstants.IS_TEST_ENVIRONMENT)) {
                switchForEnvironment.isChecked = true
                switchForEnvironment.text = "Live Environment (Port : 9090)"
                Log.e("Environment", "SettingActivity: Default => ${Prefs.getBoolean(AppConstants.IS_TEST_ENVIRONMENT)}")
            } else {
                switchForEnvironment.isChecked = false
                switchForEnvironment.text = "Test Environment (Port : 9092)"
                Log.e("Environment", "SettingActivity: Default => ${Prefs.getBoolean(AppConstants.IS_TEST_ENVIRONMENT)}")
            }

            switchForEnvironment.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Prefs.putBoolean(AppConstants.IS_TEST_ENVIRONMENT, true)
                    switchForEnvironment.text = "Live Environment (Port : 9090)"
                    Log.e("Environment", "SettingActivity: switchForEnvironment.setOnCheckedChangeListener => ${Prefs.getBoolean(AppConstants.IS_TEST_ENVIRONMENT)}")
                } else {
                    Prefs.putBoolean(AppConstants.IS_TEST_ENVIRONMENT, false)
                    switchForEnvironment.text = "Test Environment (Port : 9092)"
                    Log.e("Environment", "SettingActivity: switchForEnvironment.setOnCheckedChangeListener => ${Prefs.getBoolean(AppConstants.IS_TEST_ENVIRONMENT)}")
                }
            }

        }

        //todo Place cursor at the end of text in EditText
        activityLoginBinding.loginUsername.setSelection(activityLoginBinding.loginUsername.length())

        //todo
        sessionManagement.setFromWhere(applicationContext, "Login")

        //todo set company db--
        val companyDB = sessionManagement.getCompanyDB(this)

        Log.e("CompanyDB===>", "onCreate: " + companyDB)

        //todo set code for check company db is null or not and set text auto---
        activityLoginBinding.edtCompanyDB.setText(companyDB ?: "")
        activityLoginBinding.AcDbNameList.setText(companyDB, false)
        //todo login click listener..
        activityLoginBinding.loginButton.setOnClickListener {
//            initiateLoginCall()

            //sessionManagement.setCompanyDB(applicationContext, activityLoginBinding.edtCompanyDB.getText().toString())

            apiCall()
        }


    }

    private fun setStaticDbName(dbResponse: ArrayList<DatabaseModel.Value>?) {
        //val dbNames = arrayListOf("ACPL_LIVE_NEW", "TEST_03052025", "TEST_26062025", "TEST_18082025","TEST_ACPL_24092025","TEST_09102025")
        val dbNames: ArrayList<String> =
            ArrayList(dbResponse?.map { it.dbname } ?: emptyList())



        val adapter = ArrayAdapter(
            this@LoginActivity,
            R.layout.simple_spinner_dropdown_item,
            dbNames
        )

        activityLoginBinding.AcDbNameList.setAdapter(adapter)

        activityLoginBinding.AcDbNameList.threshold = 0 // Show suggestions even without typing

        // Handle item selection
        activityLoginBinding.AcDbNameList.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            activityLoginBinding.AcDbNameList.setText(selectedItem, false) // false to prevent filtering again
        }
    }

    private fun callDatabaseApi(){
        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
                materialProgressDialog.show()
            var apiConfig = ApiConstantForURL()

            QuantityNetworkClient.updateBaseUrlFromConfig(apiConfig, true)
            val networkClient = QuantityNetworkClient.create(this)
            networkClient.getDatabaseList().apply {
                    enqueue(object : Callback<DatabaseModel> {
                        override fun onResponse(
                            call: Call<DatabaseModel>,
                            response: Response<DatabaseModel>
                        ) {
                            try {
                                if (response.isSuccessful) {
                                    materialProgressDialog.dismiss()
                                    var dbResponse = response.body()?.value
                                    setStaticDbName(dbResponse)

                                } else {
                                    materialProgressDialog.dismiss()

                                    val gson1 = GsonBuilder().create()
                                    var mError: OtpErrorModel
                                    try {
                                        val s = response.errorBody()!!.string()
                                        mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                        if (mError.error.code.equals(400)) {
                                            GlobalMethods.showError(this@LoginActivity, mError.error.message.value)
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(this@LoginActivity, mError.error.message.value)
                                            Log.e("json_error------", mError.error.message.value)
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(call: Call<DatabaseModel>, t: Throwable) {
                            Log.e("login_api_failure-----", t.toString())
                            materialProgressDialog.dismiss()
                            Toast.makeText(this@LoginActivity, t.message, Toast.LENGTH_SHORT)
                        }

                    })
                }



        } else {
            materialProgressDialog.dismiss()
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Internet Connection Alert")
                .setMessage("Please Check Your Internet Connection")
                .setPositiveButton("Close") { dialogInterface, i ->
                    finish()
                }.show()
        }
    }


    private fun apiCall() {
        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            if (activityLoginBinding.AcDbNameList.text.toString().isNullOrEmpty()) {
                activityLoginBinding.AcDbNameList.error = "Please enter your Company DB"
            } else if (activityLoginBinding.loginUsername.text.toString().isNullOrEmpty()) {
                activityLoginBinding.loginUsername.error = "Please enter user name"
            } else if (activityLoginBinding.loginPassword.text.toString().isNullOrEmpty()) {
                activityLoginBinding.loginPassword.error = "Please enter password"
            } else {
                materialProgressDialog.show()
                sessionManagement.setCompanyDB(applicationContext, activityLoginBinding.AcDbNameList.getText().toString())
                var jsonObject: JsonObject = JsonObject()
                jsonObject.addProperty("CompanyDB", sessionManagement.getCompanyDB(this))
                jsonObject.addProperty("Password", activityLoginBinding.loginPassword.text.toString().trim())
                jsonObject.addProperty("UserName", activityLoginBinding.loginUsername.text.toString().trim())
                NetworkClients.create(this).doGetLoginCall(jsonObject).apply {
                    enqueue(object : Callback<LoginResponseModel> {
                        override fun onResponse(
                            call: Call<LoginResponseModel>,
                            response: Response<LoginResponseModel>
                        ) {
                            try {
                                if (response.isSuccessful) {
                                    materialProgressDialog.dismiss()
                                    var loginResponseModel = response.body()!!
                                    //todo shares preference store...
                                    sessionManagement.setSessionId(this@LoginActivity, loginResponseModel.SessionId)
                                    sessionManagement.setSessionTimeout(this@LoginActivity, loginResponseModel.SessionTimeout)
                                    sessionManagement.setFromWhere(this@LoginActivity, "ElseCase")
                                    sessionManagement.setUsername(this@LoginActivity, activityLoginBinding.loginUsername.text.toString().trim())
                                    sessionManagement.setPassword(this@LoginActivity, activityLoginBinding.loginPassword.text.toString().trim())

                                    Log.e("api_success-----", response.toString())
                                    if (Prefs.getString(AppConstants.BPLID, "").isNotEmpty()) {
                                        var intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                        startActivity(intent)
                                    } else {
                                        var intent = Intent(this@LoginActivity, SettingActivity::class.java)
                                        startActivity(intent)
                                    }
                                    finish()
                                    GlobalMethods.showSuccess(this@LoginActivity, "Successfully Login.")
                                } else {
                                    materialProgressDialog.dismiss()

                                    val gson1 = GsonBuilder().create()
                                    var mError: OtpErrorModel
                                    try {
                                        val s = response.errorBody()!!.string()
                                        mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                        if (mError.error.code.equals(400)) {
                                            GlobalMethods.showError(this@LoginActivity, mError.error.message.value)
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(this@LoginActivity, mError.error.message.value)
                                            Log.e("json_error------", mError.error.message.value)
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponseModel>, t: Throwable) {
                            Log.e("login_api_failure-----", t.toString())
                            materialProgressDialog.dismiss()
                            Toast.makeText(this@LoginActivity, t.message, Toast.LENGTH_SHORT)
                        }

                    })
                }
            }


        } else {
            materialProgressDialog.dismiss()
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Internet Connection Alert")
                .setMessage("Please Check Your Internet Connection")
                .setPositiveButton("Close") { dialogInterface, i ->
                    finish()
                }.show()
        }
    }


    /* //todo login through mvvm architecture...
     private fun initiateLoginCall() {
         val loginRequest = LoginRequest(
             "17122022",
             activityLoginBinding.loginPassword.text.toString(),
             activityLoginBinding.loginUsername.text.toString()
         )
         loginViewModel?.doGetLoginCall(
             activityLoginBinding.loginPassword.text.toString(),
             activityLoginBinding.loginUsername.text.toString()
         )?.observe(this, Observer { response ->
             if (response == null) {
                 Log.e("failure-----", response.toString())
                 Toast.makeText(this, "Failed to Login!", Toast.LENGTH_SHORT)
             } else {
                 var intent: Intent = Intent(this, HomeActivity::class.java)
                 startActivity(intent)
                 Log.e("reponse-----", response.toString())
 //                MDToast.makeText(this, "Successfully Login.", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR)
                 Toast.makeText(this, "Successfully Login.", Toast.LENGTH_SHORT)
             }
         })
     }*/

}