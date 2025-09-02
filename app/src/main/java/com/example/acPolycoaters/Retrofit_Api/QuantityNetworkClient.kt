package com.example.acPolycoaters.Retrofit_Api

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.acPolycoaters.SessionManagement.SessionManagement
import com.example.acPolycoaters.ui.login.LoginActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.Authenticator
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

object QuantityNetworkClient {

    /*private var retrofit: Retrofit? = null
    private var okHttpClient: OkHttpClient? = null
    private var mGsonConverter: GsonConverterFactory? = null
    private lateinit var sessionManagement: SessionManagement

    val client: OkHttpClient
        @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
        get() {
            if (okHttpClient == null) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY

                val httpBuilder = OkHttpClient.Builder()
                httpBuilder.connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)  /// show all JSON in logCat
                okHttpClient = httpBuilder.build()

            }
            return okHttpClient!!
        }


    val gsonConverter: GsonConverterFactory
        get() {
            if (mGsonConverter == null) {
                mGsonConverter = GsonConverterFactory
                    .create(
                        GsonBuilder()
                            .setLenient()
                            .disableHtmlEscaping()
                            .create()
                    )
            }
            return mGsonConverter!!
        }


    fun create(context: Context): NetworkInterface {
        sessionManagement = SessionManagement(context)
        if (sessionManagement.getFromWhere(context) == "Login") {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(gsonConverter)
                .client(client)
                .build()
            return retrofit.create(NetworkInterface::class.java)
        } else {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(gsonConverter)

                .client(initCallOkHttp(context))
                .build()
            return retrofit.create(NetworkInterface::class.java)
        }


    }

    fun initCallOkHttp(context: Context): OkHttpClient {

        val httpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.MINUTES);
        Log.e("Token==>", " " + sessionManagement.getSessionId(context))

        httpClient.addInterceptor(Interceptor { chain ->
            val original = chain.request()
            Log.e("SessionTimeout==>", " " + sessionManagement.getSessionTimeout(context))
            val requestBuilder = original.newBuilder()
                .addHeader("Cookie", "B1SESSION=" + sessionManagement.getSessionId(context))
//                .addHeader("Authorization", "Bearer " + sessionManagement.getSessionId(context))
                .header("content-type", "application/json").cacheControl(CacheControl.FORCE_NETWORK)
            val request = requestBuilder.build()
            Log.e("URL=>", request.toString())
            val response = chain.proceed(request)
            if (sessionManagement.getSessionTimeout(context).equals(null) || sessionManagement.getSessionTimeout(context).equals("") || response.code == 301 || response.code == 401) {

                sessionManagement.ClearSession(context)
                logoutScreen(context)
                return@Interceptor response
            }
            *//*else if (response.code == 400) {
                val gson = Gson()
                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                val message: OtpErrorModel.Message = gson.fromJson(response.body!!.charStream(), OtpErrorModel.Message::class.java)
                if (message.value != null) {
                    Toast.makeText(context, message.value, Toast.LENGTH_SHORT).show()
                }
            } *//*
            response
        })
        return httpClient.build()
    }

    private fun logoutScreen(context: Context) {
        val mainIntent = Intent(context, LoginActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.applicationContext.startActivity(mainIntent)
        (context as Activity).finish()
    }


    //todo need it now..
    val retrofitInstance: Retrofit?
        get() {
            if (okHttpClient == null) {
            }
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }


    const val HEADER_CACHE_CONTROL = "Cache-Control"
    const val HEADER_PRAGMA = "Cinntra"

//    private lateinit var mContext: Context

    private fun provideCache(): Cache? {
        var cache: Cache? = null
        try {
//            cache = Cache(File(mContext!!.cacheDir, "http-cache"), (10 * 1024 * 1024).toLong()) // 10 MB
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Could not create Cache!")
        }
        return cache
    }

    private fun provideCacheInterceptor(): Interceptor? {
        return Interceptor { chain: Interceptor.Chain ->
            val response = chain.proceed(chain.request())
            val cacheControl: CacheControl = if (isConnected()) {
                CacheControl.Builder().maxAge(0, TimeUnit.SECONDS)
                    .build()
            } else {
                CacheControl.Builder()
                    .maxStale(7, TimeUnit.DAYS)
                    .build()
            }
            response.newBuilder()
                .removeHeader(HEADER_PRAGMA)
                .removeHeader(HEADER_CACHE_CONTROL)
                .header(HEADER_CACHE_CONTROL, cacheControl.toString())
                .build()
        }
    }

    private fun provideOfflineCacheInterceptor(): Interceptor? {
        return Interceptor { chain: Interceptor.Chain ->
            var request = chain.request()
            if (!isConnected()) {
                val cacheControl: CacheControl = CacheControl.Builder()
                    .maxStale(7, TimeUnit.DAYS)
                    .build()
                request = request.newBuilder()
                    .removeHeader(HEADER_PRAGMA)
                    .removeHeader(HEADER_CACHE_CONTROL)
                    .cacheControl(cacheControl)
                    .build()
            }
            chain.proceed(request)
        }
    }


    fun isConnected(): Boolean {
        *//* try {
             val e = mContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
             val activeNetwork = e.activeNetworkInfo
             return activeNetwork != null && activeNetwork.isConnectedOrConnecting
         } catch (e: java.lang.Exception) {
             Log.w(ContentValues.TAG, e.toString())
         }*//*
        return false
    }*/

    private const val TIMEOUT_MINUTES = 5L
    private var BASE_URL = ""

    /**
     * Update base URL dynamically from configuration
     */
    fun updateBaseUrlFromConfig(config: ApiConstantForURL,isNewRoute:Boolean = false) {
        BASE_URL = if(isNewRoute) config.CUSTOM_API_BASE_URL_NEW else config.CUSTOM_API_BASE_URL
        Log.d("Updated BASE_URL", "Standard Base Url => ${BASE_URL}")
    }
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    private val gsonConverterFactory: GsonConverterFactory by lazy {
        GsonConverterFactory.create(
            GsonBuilder()
                .setLenient()
                .disableHtmlEscaping()
                .create()
        )
    }

    // Utility to build a fresh OkHttpClient.Builder each time
    private fun newOkHttpBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_MINUTES, TimeUnit.MINUTES)
            .readTimeout(TIMEOUT_MINUTES, TimeUnit.MINUTES)
            .writeTimeout(TIMEOUT_MINUTES, TimeUnit.MINUTES)
    }

    // Extension function to avoid duplicate interceptors
    private fun OkHttpClient.Builder.addIfAbsent(interceptor: Interceptor): OkHttpClient.Builder {
        if (!this.interceptors().any { it.javaClass == interceptor.javaClass }) {
            this.addInterceptor(interceptor)
        }
        return this
    }

    /**
     * Creates an OkHttpClient without session headers (used for login or unauthenticated calls)
     */
    private fun getBaseClient(): OkHttpClient {
        return newOkHttpBuilder()
            .addIfAbsent(loggingInterceptor)
            .build()
    }

    /**
     * Creates an OkHttpClient with session headers
     */
    private fun getAuthenticatedClient(context: Context, sessionManagement: SessionManagement): OkHttpClient {
        return newOkHttpBuilder()
            .addIfAbsent(loggingInterceptor)
            .addIfAbsent(createSessionCookieInterceptor(context, sessionManagement))
            .authenticator(SessionAuthenticator(context, sessionManagement))
            .build()
    }

    /**
     * Session interceptor to manage session cookies and timeout checks
     */
    private fun createSessionCookieInterceptor(context: Context, sessionManagement: SessionManagement): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val sessionId = sessionManagement.getSessionId(context) // Get current session ID

            // Only add the cookie if a session ID exists
            val requestBuilder = original.newBuilder().apply {
                if (!sessionId.isNullOrEmpty()) {
                    addHeader("Cookie", "B1SESSION=$sessionId")
                }
                header("content-type", "application/json") // Ensure JSON content type
                cacheControl(CacheControl.FORCE_NETWORK) // Prevent stale cached responses
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }
    }

    fun create(context: Context): NetworkInterface {
        val sessionManagement = SessionManagement(context) // Initialize session management
        // Determine if the client should be authenticated or not.
        // "Login" implies the user is not yet authenticated, so use the base client.
        // Any other status implies authenticated, so use the client with authenticator.
        val isCurrentlyLoggedIn = sessionManagement.getFromWhere(context) != "Login"

        val okHttpClient = if (isCurrentlyLoggedIn) {
            getAuthenticatedClient(context, sessionManagement)
        } else {
            getBaseClient()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL) // Use the dynamically updated base URL
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // For RxJava support
            .addConverterFactory(gsonConverterFactory) // For JSON serialization/deserialization
            .client(okHttpClient) // Set the configured OkHttpClient
            .build()

        return retrofit.create(NetworkInterface::class.java)
    }

    /**
     * OkHttp Authenticator to handle 401 (Unauthorized) or 301 (Moved Permanently, as per your backend)
     * responses by attempting to refresh the session token and re-attempting the original request.
     */
    class SessionAuthenticator(
        private val context: Context,
        private val sessionManagement: SessionManagement
    ) : Authenticator {

        // Using a ReentrantLock to ensure that only one thread attempts to refresh the token at a time.
        // This prevents multiple simultaneous refresh calls if many requests fail concurrently.
        private val lock = ReentrantLock()

        // A separate OkHttpClient instance for the refresh token API call.
        // This client MUST NOT include this Authenticator itself to prevent a recursive loop
        // (Authenticator calling itself when trying to refresh).
        private val refreshClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_MINUTES, TimeUnit.MINUTES)
                .readTimeout(TIMEOUT_MINUTES, TimeUnit.MINUTES)
                .writeTimeout(TIMEOUT_MINUTES, TimeUnit.MINUTES)
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }) // Still log refresh calls
                .build()
        }

        /**
         * This method is called by OkHttp when a request receives a 401 or 301 response.
         * It attempts to re-authenticate and provide a new request.
         */
        override fun authenticate(route: Route?, response: Response): Request? {
            // Only proceed if the response code indicates an authentication issue.
            if (response.code != 401 && response.code != 301) {
                return null // Not an authentication problem, so no re-authentication attempt
            }

            // Acquire the lock to ensure only one thread performs the refresh operation.
            lock.lock()
            try {
                // Get the current session ID *after* acquiring the lock.
                // This is crucial: if another thread refreshed the token while we were waiting for the lock,
                // we should use the new token directly instead of attempting to refresh again.
                val currentSessionId = sessionManagement.getSessionId(context)

                // Check if the request that failed already used a stale token, and if a new one is available.
                // If the current request's cookie is different from the session management's current ID,
                // it means another thread already refreshed and updated the session ID.
                val originalSessionIdInRequest = response.request.header("Cookie")
                    ?.split(";")
                    ?.find { it.trim().startsWith("B1SESSION=") }
                    ?.split("=")
                    ?.getOrNull(1)

                if (currentSessionId != null && originalSessionIdInRequest != currentSessionId) {
                    // Token already refreshed by another thread. Retry the original request with the new token.
                    Log.d("SessionAuthenticator", "Session ID already refreshed by another thread. Retrying original request with new ID.")
                    return response.request.newBuilder()
                        .header("Cookie", "B1SESSION=$currentSessionId")
                        .build()
                }

                // If we reach here, it means this thread is responsible for refreshing the session.
                Log.d("SessionAuthenticator", "Session expired (401/301 received). Attempting to refresh...")

                // Attempt to refresh the session by making a synchronous API call.
                val newSessionId = refreshSession()

                if (newSessionId != null) {
                    // If refresh is successful, save the new session ID.
                    sessionManagement.setSessionId(context,newSessionId)
                    sessionManagement.setFromWhere(context,"Authenticated") // Mark as authenticated again
                    Log.d("SessionAuthenticator", "Session refreshed successfully. New Session ID: $newSessionId")

                    // Return a new request with the updated session ID header.
                    // OkHttp will then automatically retry the original failed request with this new token.
                    return response.request.newBuilder()
                        .header("Cookie", "B1SESSION=$newSessionId")
                        .build()
                } else {
                    // If refresh fails for any reason, log out the user.
                    Log.e("SessionAuthenticator", "Failed to refresh session. Redirecting to login.")
                    logoutScreen(context)
                    return null // Do not retry the request; it means re-authentication is not possible
                }
            } finally {
                lock.unlock() // Always release the lock
            }
        }

        /**
         * **IMPORTANT: Implement your actual session refresh API call here.**
         * This method makes a synchronous API call (e.g., a login call) to obtain a new session ID.
         *
         * @return The new session ID if the refresh is successful, otherwise null.
         */
        private fun refreshSession(): String? {
            // IMPORTANT: Replace this with your actual session refresh logic.
            // This typically involves making a login call with stored credentials
            // (username/password) or a refresh token.

            val username = sessionManagement.getUsername(context)
            val password = sessionManagement.getPassword(context)
            val dbName = sessionManagement.getCompanyDB(context)

            if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
                Log.e("SessionAuthenticator", "No stored credentials available for session refresh.")
                return null
            }

            try {
                // Create a temporary Retrofit instance for the refresh call.
                // This instance uses 'refreshClient' which does NOT have this Authenticator,
                // preventing a recursive loop.
                val refreshRetrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL) // Use the same base URL
                    .addConverterFactory(gsonConverterFactory)
                    .client(refreshClient) // Use the client without the authenticator
                    .build()

                // Get an instance of your NetworkInterface (or a dedicated login service)
                val refreshService = refreshRetrofit.create(NetworkInterface::class.java)

                // Prepare your login request body. Adjust keys and values based on your API.
                var jsonObject = JsonObject()
                jsonObject.addProperty("CompanyDB", dbName)
                jsonObject.addProperty("Password", password)
                jsonObject.addProperty("UserName", username)

                // Execute the login call SYNCHRONOUSLY.
                // DO NOT use suspend functions directly here if not in a coroutine scope,
                // or use .await() on a Call<T> if you're using Kotlin's suspend.
                // For Authenticator, .execute() is the correct synchronous way.
                val response = refreshService.doGetLoginCall(jsonObject).execute()

                if (response.isSuccessful) {
                    // Parse the response to extract the new session ID.
                    // Your API might return the session ID in a 'Set-Cookie' header,
                    // or within the JSON response body. Adjust parsing accordingly.
                    val newSessionId = response.headers()["Set-Cookie"]
                        ?.split(";")
                        ?.find { it.trim().startsWith("B1SESSION=") }
                        ?.split("=")
                        ?.getOrNull(1) // Get the value after '='

                    if (newSessionId.isNullOrEmpty()) {
                        Log.e("SessionAuthenticator", "Refresh successful, but no B1SESSION cookie found in response.")
                        return null
                    }
                    return newSessionId
                } else {
                    Log.e("SessionAuthenticator", "Refresh login failed: ${response.code()} - ${response.message()}")
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrEmpty()) {
                        Log.e("SessionAuthenticator", "Refresh error body: $errorBody")
                    }
                    return null
                }
            } catch (e: Exception) {
                Log.e("SessionAuthenticator", "Error during session refresh: ${e.message}", e)
                return null
            }
        }

    }

    /**
     * Redirect to LoginActivity and clear activity stack
     */
    private fun logoutScreen(context: Context) {
        val mainIntent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.applicationContext.startActivity(mainIntent)
        (context as? Activity)?.finish()
    }
}