package com.example.acPolycoaters.ui.issueForProductionOrder.UI.qrScannerUi

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.acPolycoaters.R
import com.example.acPolycoaters.databinding.ActivityScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QRScannerActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private var cameraProvider: ProcessCameraProvider? = null
    private val scanner = BarcodeScanning.getClient()

    // Runtime permission launcher (for Android 6+)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                openSettingsDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)
        supportActionBar?.hide()
        previewView = findViewById(R.id.previewView)

        checkAndRequestPermission()
    }

    private fun checkAndRequestPermission() {
        // For Android 6 (API 23) and above -> runtime permission required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            // For Android 5.1 and below -> permission is granted at install time
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            processImageProxy(imageProxy)
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis
        )
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { value ->
                        vibrateDevice()
                        //Toast.makeText(this, "Scanned: $value", Toast.LENGTH_LONG).show()

                        val intent = Intent()
                        intent.putExtra("batch_code", value)
                        setResult(RESULT_OK, intent)
                        finish()

                        cameraProvider?.unbindAll()

                        // exit the listener properly
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener {
                Log.e("MLKit", "Barcode scanning failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    @Suppress("DEPRECATION")
    private fun vibrateDevice() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    200,  // vibration duration in ms
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(200) // deprecated but works on older devices
        }
    }

    private fun openSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Camera permission is required to scan QR codes.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}


/*class QRScannerActivity : AppCompatActivity() {

    // View binding instance to access views in the layout
    private lateinit var binding: ActivityScannerBinding

    // CodeScanner instance
    private lateinit var codeScanner: CodeScanner

    */
/**
 * This launcher is used to request one or more permissions and handle the result.
 * It replaces the old onRequestPermissionsResult() method.
 *//*
    @SuppressLint("NewApi")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedList = permissions.filter { !it.value }.keys

            if (deniedList.isEmpty()) {
                // ✅ All permissions were granted by the user.
                codeScanner.startPreview()
            } else {
                // ❌ Some permissions were denied.
                var isPermanentlyDenied = false
                for (permission in deniedList) {
                    if (!shouldShowRequestPermissionRationale(permission)) {
                        // The user has selected "Don't ask again."
                        isPermanentlyDenied = true
                        break
                    }
                }

                if (isPermanentlyDenied) {
                    // Guide the user to the app settings to grant the permissions.
                    openSettingsDialog()
                } else {
                    // Show a rationale dialog to explain why the permissions are needed.
                    showRationaleDialog()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the CodeScanner with the activity context and scanner view
        codeScanner = CodeScanner(this, binding.scannerView)

        // Configure the scanner and check for permissions
        setupScanner()
        checkAndRequestPermissions()
    }

    */
/**
 * Configures the behavior of the CodeScanner.
 *//*
    private fun setupScanner() {
        codeScanner.camera = CodeScanner.CAMERA_BACK // Use the rear camera
        codeScanner.formats = CodeScanner.ALL_FORMATS // Scan all supported formats
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE // Scan one code and stop
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        // Callback for when a QR code is successfully decoded
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val result = it.text
                val intent = Intent()
                intent.putExtra("batch_code", result)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        // Callback for handling scanner errors
        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Camera error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Restart the preview on a tap
        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    */
/**
 * Checks for necessary permissions and requests them if they are not granted.
 * This method handles the difference in storage permissions across Android versions.
 *//*
    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        // The CAMERA permission is a core requirement for all versions.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        // -- MODIFIED LOGIC FOR ANDROID 13 AND HIGHER --
        // On Android 13 (API 33) and above, a new specific media permission is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API 33
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // Old logic for Android 9 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            // Launch the permission request dialog
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            // All required permissions are already granted, start the scanner immediately
            codeScanner.startPreview()
        }
    }

    */
/**
 * Displays a dialog to explain to the user why the permissions are needed.
 *//*
    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setMessage("Camera and Storage permissions are required for scanning QR codes.")
            .setPositiveButton("Allow") { _, _ -> checkAndRequestPermissions() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    */
/**
 * Displays a dialog to prompt the user to go to app settings for a permanently denied permission.
 *//*
    private fun openSettingsDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("You have denied some permissions. Please go to Settings to allow them.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
                finish() // Close the activity if the user cancels
            }
            .create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        // If the scanner has been initialized, start the preview.
        // This is important for when the user returns from the settings screen.
        if (::codeScanner.isInitialized &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        // Release the camera resources when the activity is paused to prevent conflicts.
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }
}*/

/*class QRScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var codeScanner: CodeScanner

    */
/** Permission launcher *//*
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

    */
/** Check and request runtime permissions *//*
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

    */
/** Show rationale dialog if denied *//*
    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setMessage("Camera and Storage permissions are required for scanning QR codes.")
            .setPositiveButton("Allow") { _, _ -> checkAndRequestPermissions() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    */
/** Open settings dialog if permanently denied *//*
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
}*/

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

    */
/** Check and request runtime permissions *//*
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

    */
/** Handle permission result *//*
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

    */
/** Show settings dialog *//*
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
