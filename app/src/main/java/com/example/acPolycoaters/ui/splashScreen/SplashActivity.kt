package com.example.acPolycoaters.ui.splashScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.acPolycoaters.Global_Classes.AppConstants
import com.example.acPolycoaters.Global_Classes.MaterialProgressDialog
import com.example.acPolycoaters.Global_Notification.NetworkConnection
import com.example.acPolycoaters.R
import com.example.acPolycoaters.SessionManagement.SessionManagement
import com.example.acPolycoaters.databinding.ActivitySplashBinding
import com.example.acPolycoaters.ui.home.HomeActivity
import com.example.acPolycoaters.ui.login.LoginActivity
import com.example.acPolycoaters.ui.setting.SettingActivity
import com.pixplicity.easyprefs.library.Prefs

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {
    var networkConnection = NetworkConnection()
    private lateinit var sessionManagement: SessionManagement
    private lateinit var binding: ActivitySplashBinding
    lateinit var materialProgressDialog: MaterialProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        sessionManagement = SessionManagement(applicationContext)
        materialProgressDialog = MaterialProgressDialog(this@SplashActivity)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_slide)
        binding.headerIcon.startAnimation(slideAnimation)


        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            // todo end rotate
            Handler().postDelayed({
                gotoLogin()
            }, 3000)

        } else {
            var alertDialog = AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Internet Connection Alert")
                .setMessage("Please Check Your Internet Connection")
                .setPositiveButton("") { dialogInterface, i ->
                    var intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }.show()
            alertDialog.setCanceledOnTouchOutside(false)
        }

    }

    fun gotoLogin() {
        Log.e("SplashTimeout==>"," " + sessionManagement.getSessionTimeout(applicationContext))
        val savedBPLID = Prefs.getString(AppConstants.BPLID, "")
        if (!sessionManagement.getSessionId(applicationContext).isNullOrEmpty() && !sessionManagement.getSessionTimeout(applicationContext).equals(null)) {
            if (savedBPLID.isEmpty()) {
                /*if (Prefs.getString(AppConstants.AppIP, "").isNotEmpty() && Prefs.getString(AppConstants.DBUrl, "").isNotEmpty()) {*/
                Log.e("Splash==>", "gotoLogin() => second if condition to navigate to SettingActivity")
                val intent = Intent(this@SplashActivity, SettingActivity::class.java)
                startActivity(intent)
                /*} else {
                    Log.e("Splash==>", "gotoLogin() => second else condition to navigate to LoginActivity")
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                }*/

            } else {
                Log.e("Splash==>", "gotoLogin() => first else condition to navigate to HomeActivity")
                val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                startActivity(intent)
            }

            finish()
        } else {
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

/*    fun callLoginApi(){
        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            materialProgressDialog.show()
            if (validation.isCheckNull(this, activityLoginBinding.loginUsername, activityLoginBinding.loginUsername.toString()) && validation.isCheckNull(this, activityLoginBinding.loginPassword, activityLoginBinding.loginPassword.toString())) {
                var jsonObject: JsonObject = JsonObject()
                jsonObject.addProperty("CompanyDB", AppConstants.COMPANY_DB)
                jsonObject.addProperty("Password", sessionManagement.get)
                jsonObject.addProperty("UserName", activityLoginBinding.loginUsername.text.toString())
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
                                    Log.e("api_success-----", response.toString())
                                    var intent: Intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                    GlobalMethods.showSuccess(this@LoginActivity, "Successfully Login.")
                                } else {
                                    materialProgressDialog.dismiss()

                                    val gson1 = GsonBuilder().create()
                                    var mError : OtpErrorModel
                                    try {
                                        val s = response.errorBody()!!.string()
                                        mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                        if (mError.error.code .equals(400)) {
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

            } else {
                if (activityLoginBinding.loginUsername.text.toString().isNullOrEmpty()) {
                    activityLoginBinding.loginUsername.error = "Please enter your Username."
                } else {
                    activityLoginBinding.loginPassword.error = "Please enter your password"
                }
            }
        }
        else {
            materialProgressDialog.dismiss()
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Internet Connection Alert")
                .setMessage("Please Check Your Internet Connection")
                .setPositiveButton("Close") { dialogInterface, i ->
                    finish()
                }.show()
        }
    }*/

}