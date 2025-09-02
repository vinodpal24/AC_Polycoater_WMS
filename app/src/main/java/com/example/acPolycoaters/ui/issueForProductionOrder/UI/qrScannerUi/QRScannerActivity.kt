package com.example.acPolycoaters.ui.issueForProductionOrder.UI.qrScannerUi

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.acPolycoaters.databinding.ActivityScannerBinding


class QRScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var codeScanner: CodeScanner

    /** Permission launcher */
    @SuppressLint("NewApi")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedList = permissions.filter { !it.value }.keys

            if (deniedList.isEmpty()) {
                // ✅ All permissions granted
                codeScanner.startPreview()
            } else {
                // ❌ Some denied
                var permanentlyDenied = false
                for (permission in deniedList) {
                    if (!shouldShowRequestPermissionRationale(permission)) {
                        permanentlyDenied = true
                        break
                    }
                }

                if (permanentlyDenied) {
                    openSettingsDialog()
                } else {
                    showRationaleDialog()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        codeScanner = CodeScanner(this, binding.scannerView)
        setupScanner()
        checkAndRequestPermissions()
    }

    private fun setupScanner() {
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val result = it.text
                val intent = Intent()
                intent.putExtra("batch_code", result)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Camera error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    /** Check and request runtime permissions */
    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        // Camera always needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        // Storage based on version
        when {
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> { // Android 9 and below
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> { // Android 10
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                // Android 11+ → Scoped storage, no explicit storage perms required
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            codeScanner.startPreview()
        }
    }

    /** Show rationale dialog if denied */
    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setMessage("Camera and Storage permissions are required for scanning QR codes.")
            .setPositiveButton("Allow") { _, _ -> checkAndRequestPermissions() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** Open settings dialog if permanently denied */
    private fun openSettingsDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("You have denied some permissions. Allow them in Settings to continue.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.setOnDismissListener {
            // Check again if permission is still not granted, show dialog again
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                openSettingsDialog()
            }
        }

        dialog.show()
    }


    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) codeScanner.startPreview()
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) codeScanner.releaseResources()
        super.onPause()
    }
}

/*class QRScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var codeScanner: CodeScanner
    private val REQUEST_ID_PERMISSIONS = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        codeScanner = CodeScanner(this, binding.scannerView)

        setupScanner()
        checkAndRequestPermissions()
    }

    private fun setupScanner() {
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val result = it.text
                val intent = Intent()
                intent.putExtra("batch_code", result)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Camera error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    *//** Check and request runtime permissions *//*
    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        // Camera is always required
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        // Handle storage based on Android version
        when {
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> { // Android 9 and below
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> { // Android 10
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else -> { // Android 11 and above → Scoped storage, no storage perms required
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                REQUEST_ID_PERMISSIONS
            )
        } else {
            codeScanner.startPreview()
        }
    }

    *//** Handle permission result *//*
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_ID_PERMISSIONS) {
            val deniedList = mutableListOf<String>()

            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(permissions[i])
                }
            }

            if (deniedList.isEmpty()) {
                // ✅ All granted
                codeScanner.startPreview()
            } else {
                // ❌ Some denied
                var permanentlyDenied = false
                for (permission in deniedList) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        permanentlyDenied = true
                        break
                    }
                }

                if (permanentlyDenied) {
                    // User clicked "Don't ask again" → force open App Settings
                    openSettingsDialog()
                } else {
                    // Ask again with explanation
                    AlertDialog.Builder(this)
                        .setMessage("Camera and Storage permissions are required for scanning QR codes.")
                        .setPositiveButton("Allow") { _, _ -> checkAndRequestPermissions() }
                        .setNegativeButton("Cancel"){ _, _ -> checkAndRequestPermissions() }
                        .show()
                }
            }
        }
    }

    *//** Show settings dialog *//*
    private fun openSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("You have denied some permissions. Allow all permissions in Settings to continue.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel"){ _, _ -> checkAndRequestPermissions() }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) codeScanner.startPreview()
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) codeScanner.releaseResources()
        super.onPause()
    }
}*/


/*
class QRScannerActivity : AppCompatActivity() {
    lateinit var activityScannerBinding: ActivityScannerBinding
    private lateinit var codeScanner: CodeScanner
    val REQUEST_ID_MULTIPLE_PERMISSIONS = 7
    var result = ""
    var position = 0
    var itemCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityScannerBinding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(activityScannerBinding.root)

        codeScanner = CodeScanner(this@QRScannerActivity, activityScannerBinding.scannerView)

        // todo Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // todo ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        //todo getting permission for camera firstly...
        checkAndRequestPermissions()
        // todo Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
//                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                result = it.text
                    onBackPressed()

            }
        }
        //todo error throw...
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        activityScannerBinding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onBackPressed() {
        var intent : Intent = Intent()
        intent.putExtra("batch_code", result)
        setResult(RESULT_OK, intent)
        finish()
    }


    //todo set permission for image...
    private fun checkAndRequestPermissions(): Boolean {
        val camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val wtite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (wtite != PackageManager.PERMISSION_GRANTED) { listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE) }
        if (camera != PackageManager.PERMISSION_GRANTED) { listPermissionsNeeded.add(Manifest.permission.CAMERA) }
        if (read != PackageManager.PERMISSION_GRANTED) { listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE) }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("in fragment on request", "Permission callback called-------")
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {
                val perms: MutableMap<String, Int> = HashMap()
                // Initialize the map with both permissions
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.size > 0) {
                    var i = 0
                    while (i < permissions.size) {
                        perms[permissions[i]] = grantResults[i]
                        i++
                    }
                    // Check for both permissions
                    if (perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("in fragment on request", "CAMERA & WRITE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE permission granted")
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("  ", "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(
                                this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showDialogOK("Camera and Storage Permission required for this app") { dialog, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                    DialogInterface.BUTTON_NEGATIVE -> {}
                                }
                            }
                        }
                        else {
                            */
/*val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)*//*

                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }


}*/
