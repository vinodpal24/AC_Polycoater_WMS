package com.example.acPolycoaters.ui.inventoryTransferStandalone.ui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acPolycoaters.Global_Classes.AppConstants
import com.example.acPolycoaters.Global_Classes.GlobalMethods
import com.example.acPolycoaters.Global_Classes.MaterialProgressDialog
import com.example.acPolycoaters.Global_Notification.NetworkConnection
import com.example.acPolycoaters.Model.GetDocSeriesModel
import com.example.acPolycoaters.Model.GetQuantityModel
import com.example.acPolycoaters.Model.GetWarehouseModel
import com.example.acPolycoaters.Model.OtpErrorModel
import com.example.acPolycoaters.R
import com.example.acPolycoaters.Retrofit_Api.ApiConstantForURL
import com.example.acPolycoaters.Retrofit_Api.NetworkClients
import com.example.acPolycoaters.Retrofit_Api.QuantityNetworkClient
import com.example.acPolycoaters.SessionManagement.SessionManagement
import com.example.acPolycoaters.databinding.ActivityInventoryTransferStandaloneBinding
import com.example.acPolycoaters.ui.home.HomeActivity
import com.example.acPolycoaters.ui.inventoryTransferStandalone.adapter.GoodsChanchalAdapter
import com.example.acPolycoaters.ui.inventoryTransferStandalone.model.GetSeriesModel
import com.example.acPolycoaters.ui.inventoryTransferStandalone.model.InventoryPostResponse
import com.example.acPolycoaters.ui.inventoryTransferStandalone.model.LocalListForGoods
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ScanedOrderBatchedItems
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.WarehouseBPL_IDModel
import com.example.acPolycoaters.ui.issueForProductionOrder.UI.qrScannerUi.QRScannerActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pixplicity.easyprefs.library.Prefs
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType
import com.webapp.internetconnection.CheckNetwoorkConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.SocketTimeoutException

class InventoryTransferStandaloneActivity : AppCompatActivity(), GoodsChanchalAdapter.OnItemActionListener {
    private lateinit var binding: ActivityInventoryTransferStandaloneBinding
    lateinit var materialProgressDialog: MaterialProgressDialog
    lateinit var checkNetwoorkConnection: CheckNetwoorkConnection
    private lateinit var sessionManagement: SessionManagement
    private lateinit var networkConnection: NetworkConnection
    private var itemCode = ""
    private lateinit var recyclerView: RecyclerView
    var quantityList_gl: ArrayList<String> = ArrayList<String>()
    private var scanedBatchedItemsList_gl: ArrayList<ScanedOrderBatchedItems.Value> = ArrayList()
    var goodsOrderLineAdapter: GoodsChanchalAdapter? = null
    private lateinit var whList: ArrayList<GetWarehouseModel.Value>

    //private lateinit var series: ArrayList<GetSeriesModel.Value>
    var tempList: ArrayList<String> = ArrayList()
    var itemLineArrayList: ArrayList<ScanedOrderBatchedItems.Value> =
        ArrayList<ScanedOrderBatchedItems.Value>()
    lateinit var batchList: List<ScanedOrderBatchedItems.Value>
    var noneQuantityList: ArrayList<String> = ArrayList<String>()
    private var BPLIDNum = 0
    private var pos: Int = 0
    private var series: String = ""
    var fromWhareHouse = ""
    var fromBinLocation = ""
    var toBinLocation = ""
    var toWhareHouse = ""
    var BPLID = ""
    var BinManaged = ""
    var type = ""
    var itemDesc = ""
    var BatchScannedData = ""
    var textMain = ""
    var itemCodeMain = ""
    var tvTotalScannQty = ""
    var rvBatchItems = ""
    var itemList_gl = ""
    var typeMain = ""
    var selectedFromWarehosueName = ""
    var selectedToWarehosueName = ""
    var selectedFromWarehosueCode = ""
    var selectedToWarehosueCode = ""
    var selectedBPLId = ""
    var selectedSeriesCode = ""
    var selectedSeriesName = ""

    private val qrScannerFromWarehouseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val scannedResult = data?.getStringExtra("batch_code")
            Log.i("ITR_STAND", "Item Scan Data: $data\nScanResult From warehouse: $scannedResult")

            if (!scannedResult.isNullOrEmpty()) {
                binding.edFromWarehouse.setText(scannedResult)
                // move focus to ToWarehouse
                binding.edToWarehouse.isFocusableInTouchMode = true
                binding.edToWarehouse.requestFocus()
                val parts = scannedResult.split(",")
                val lastPart = parts.last()
                val itemCode = parts[0]
                type = lastPart
                BatchScannedData = scannedResult

                if (parts.isNotEmpty()) {
                    fromWhareHouse = parts[0].trim()

                    if (parts.size > 2) {   // ✅ check at least 3 elements before accessing index 2
                        fromBinLocation = parts[2].trim()
                    }

                    if (fromWhareHouse.isNotEmpty()) {
                        //getBPL_IDNumber()

                        // getBPLID(fromWhareHouse, "INVT")
                        BPLID = if (Prefs.getString(AppConstants.BPLID, "").isNotEmpty()) Prefs.getString(AppConstants.BPLID, "") else ""
                        getDocSeriesApi(BPLID)
                    }
                }

                // binding.edFromWhareHouse.setText("")
            }
        }
    }

    private val qrScannerToWarehouseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val scannedResult = data?.getStringExtra("batch_code")
            Log.i("ITR_STAND", "Item Scan Data: $data\nScanResult To warehouse: $scannedResult")

            if (!scannedResult.isNullOrEmpty()) {
                binding.edToWarehouse.setText(scannedResult)
                binding.edBatchCodeScan.isFocusableInTouchMode = true
                binding.edBatchCodeScan.requestFocus()  // move focus to ToWarehouse
                val parts = scannedResult.split(",")
                val lastPart = parts.last()
                val itemCode = parts[0]
                type = lastPart
                BatchScannedData = scannedResult

                if (parts.isNotEmpty()) {
                    toWhareHouse = parts[0].trim()

                    if (parts.size > 2) {   // ✅ check before using index 2
                        toBinLocation = parts[2].trim()
                    }
                }

                // binding.edToWhareHouse.setText("")
            }
        }
    }


    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val scannedResult = data?.getStringExtra("batch_code")
            Log.i("ITR_STAND", "Item Scan Data: $data\nScanResult: $scannedResult")
            if (!scannedResult.isNullOrEmpty()) {
                val parts = scannedResult.split(",")
                val lastPart = parts.last()
                val batchCode = parts.getOrNull(0).toString()
                val itemCode = parts.getOrNull(1).toString()
                type = parts.getOrNull(6).toString()
                BatchScannedData = scannedResult

                when (type) {
                    "Batch" -> {
                        if (checkDuplicate(AppConstants.scannedItemForGood, batchCode)) {
                            scanBatchLinesItem(batchCode, recyclerView, pos, itemCode, binding.tvTotalScannQty, type)
                        }
                    }

                    "Serial" -> {
                        if (checkDuplicateForSerial(AppConstants.scannedItemForGood, batchCode)) {
                            //scanSerialLineItem(parts[1], recyclerView, pos, itemCode, binding.tvTotalScannQty, type)
                        }
                    }

                    "NONE", "None" -> {
                        itemDesc = parts[2]
                        if (checkDuplicateForNone(AppConstants.scannedItemForGood, batchCode)) {
                            callNoneBindFunction(itemCode, recyclerView, pos, binding.tvTotalScannQty, itemCode, batchCode)
                        }
                    }

                    else -> {
                        GlobalMethods.showMessage(this, "Scan Type is $type")
                    }
                }
            }
        }
    }

    private fun callNoneBindFunction(itemCode: String, recyclerView: RecyclerView, pos: Int, tvTotalScannQty: TextView, itemCode1: String, batchCode: String) {
        if (itemCode.isNotEmpty()) {

            var itemList_gl: ArrayList<ScanedOrderBatchedItems.Value> = ArrayList()
            itemList_gl.clear()
            var data = ScanedOrderBatchedItems.Value("0", itemCode, itemDesc, "", "", "", "", "", "", "", "", 0L, "", "", 0.0, 0.0, 0.0, 0.0, 0.0, "", 0.0, 0.0, 0.0, 0.0, "")

            itemList_gl.add(data)

            for (i in itemList_gl.indices) {
                itemLineArrayList.add(itemList_gl[i])
            }

            getQuantityFromApiForNoneType(itemCode, itemList_gl)


        }
    }

    private fun getQuantityFromApiForNoneType(itemCode: String, itemList_gl: ArrayList<ScanedOrderBatchedItems.Value>) {

        //todo changes by shubh for warehouse listing
        if (networkConnection.getConnectivityStatusBoolean(this@InventoryTransferStandaloneActivity)) {
            materialProgressDialog.show()
            var apiConfig = ApiConstantForURL()
            QuantityNetworkClient.updateBaseUrlFromConfig(apiConfig, true)

            val networkClient = QuantityNetworkClient.create(this@InventoryTransferStandaloneActivity)
            networkClient.getQuantityGoodsWithWareHouseCode(
                sessionManagement.getCompanyDB(applicationContext)!!,
                itemCode,
                "",
                ""
            )//getQuantityValue(batchCode, itemCode, "RM-309")//sessionManagement.getWarehouseCode(this@GoodsOrderActivity)!!
                .enqueue(object : Callback<GetQuantityModel> {
                    override fun onResponse(
                        call: Call<GetQuantityModel>,
                        response: Response<GetQuantityModel>
                    ) {
                        try {
                            materialProgressDialog.dismiss()
                            if (response.isSuccessful) {
                                val responseModel = response.body()!!

                                if (responseModel.value.isNotEmpty() && !responseModel.value[0].Quantity.isNullOrEmpty() && !responseModel.value[0].Quantity.equals("0.0")) {

                                    for (i in itemList_gl.indices) {
                                        itemLineArrayList.add(itemList_gl[i])
                                    }


                                    var stringList: ArrayList<String> = ArrayList()
                                    stringList.addAll(noneQuantityList)


                                    if (!itemList_gl.isNullOrEmpty()) {

                                        Log.e("list_size-----", itemList_gl.size.toString())

                                        stringList.add("0")

                                        noneQuantityList.clear()

                                        for (i in stringList.indices) {
                                            noneQuantityList.add(stringList[i])
                                            quantityList_gl.add(stringList[i])

                                        }


                                        //todo adding new serial item in locallstof appconstant
                                        AppConstants.scannedItemForGood.add(
                                            LocalListForGoods(
                                                DocEntry = itemList_gl[0].DocEntry.toInt(),
                                                ItemCode = itemList_gl[0].ItemCode,
                                                ItemDescription = itemList_gl[0].ItemDescription,
                                                Status = itemList_gl[0].Status,
                                                Batch = "",
                                                SystemNumber = itemList_gl[0].SystemNumber.toInt(),
                                                SerialNumber = "",
                                                ScanType = "None",
                                                Quantity = "0.0",
                                                WareHouseCode = "",//responseModel.value[0].WarehouseCode
                                                UnitPrice = "",
                                                BatchNumber = "",
                                                SystemSerialNumber = itemList_gl[0].SystemNumber,
                                                InternalSerialNumber = itemList_gl[0].SerialNumber,
                                                NoneVal = itemList_gl[0].NoneVal,
                                                wareHouseListing = responseModel.value as MutableList<GetQuantityModel.Value>

                                            )
                                        )

                                        Log.e("ITR_STAND", "None LOCAL LIST>>>: ${AppConstants.scannedItemForGood}")
                                        Log.e("ITR_STAND", " None LOCAL LISTSIZE>>>: ${AppConstants.scannedItemForGood.size}")

                                        goodsOrderLineAdapter!!.notifyDataSetChanged()
                                        // Calculate total quantity and set to TextView (assuming you have a TextView instance)
                                        val totalQuantity = AppConstants.scannedItemForGood.sumOf {
                                            GlobalMethods.changeDecimal(it.Quantity)!!.toDouble()
                                        }

//                                        binding.tvTotalScannQty.text = ": $totalQuantity"

                                    }

                                } else {
                                    GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "No Quantity Found of this Goods Issue.")
                                    //   batchItemsAdapter?.notifyDataSetChanged()
                                }
                            } else {
                                handleErrorResponse(response)
                            }
                        } catch (e: Exception) {
                            materialProgressDialog.dismiss()
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(call: Call<GetQuantityModel>, t: Throwable) {
                        Log.e("scanItemApiFailed-----", t.toString())
                        materialProgressDialog.dismiss()
                    }
                })
        } else {
            materialProgressDialog.dismiss()
            Toast.makeText(this@InventoryTransferStandaloneActivity, "No Network Connection", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun scanBatchLinesItem(batch: String, rvBatchItems: RecyclerView, position: Int, itemCode: String?, tvTotalScannQty: TextView, type: String) {
        if (networkConnection.getConnectivityStatusBoolean(this)) {
            materialProgressDialog.show()

            val networkClient = NetworkClients.create(this)
            networkClient.doGetBatchNumScanDetails("Batch eq '" + batch + "'" + " and ItemCode eq '" + itemCode + "'")
                .apply {
                    enqueue(object : Callback<ScanedOrderBatchedItems> {
                        override fun onResponse(
                            call: Call<ScanedOrderBatchedItems>,
                            response: Response<ScanedOrderBatchedItems>
                        ) {
                            try {
                                materialProgressDialog.dismiss()
                                if (response.isSuccessful) {
                                    Log.e("response---------", response.body().toString())

                                    var responseModel = response.body()!!
                                    if (responseModel.value.size > 0 && !responseModel.value.isNullOrEmpty()) {
                                        var modelResponse = responseModel.value
                                        scanedBatchedItemsList_gl.addAll(modelResponse)

                                        var itemList_gl: ArrayList<ScanedOrderBatchedItems.Value> = ArrayList()
                                        itemList_gl.clear()
                                        itemList_gl.add(responseModel.value[0])


                                        if (!itemList_gl.isNullOrEmpty()) {

                                            Log.e("list_size-----", itemList_gl.size.toString())

                                            //todo quantity..

                                            textMain = batch
                                            itemCodeMain = itemList_gl[0].ItemCode
                                            typeMain = type

                                            getQuantityFromApi(batch, itemList_gl[0].ItemCode, tvTotalScannQty, rvBatchItems, itemList_gl, type)

                                        }
                                    } else {
                                        GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Invalid Batch Code")
                                        Log.e("not_response---------", response.message())
                                    }

                                } else {
                                    materialProgressDialog.dismiss()
                                    val gson1 = GsonBuilder().create()
                                    var mError: OtpErrorModel
                                    try {
                                        val s = response.errorBody()!!.string()
                                        mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                        if (mError.error.code.equals(400)) {
                                            GlobalMethods.showError(
                                                this@InventoryTransferStandaloneActivity,
                                                mError.error.message.value
                                            )
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(
                                                this@InventoryTransferStandaloneActivity,
                                                mError.error.message.value
                                            )
                                            Log.e("json_error------", mError.error.message.value)
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }

                            } catch (e: Exception) {
                                materialProgressDialog.dismiss()
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(call: Call<ScanedOrderBatchedItems>, t: Throwable) {
                            Log.e("scanItemApiFailed-----", t.toString())
                            materialProgressDialog.dismiss()
                        }

                    })
                }
        } else {
            materialProgressDialog.dismiss()
            Toast.makeText(this@InventoryTransferStandaloneActivity, "No Network Connection", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getQuantityFromApi(
        batchCode: String,
        itemCode: String,
        tvTotalScannQty: TextView,
        rvBatchItems: RecyclerView,
        itemList_gl: ArrayList<ScanedOrderBatchedItems.Value>,
        type: String
    ) {

        //todo changes by shubh for warehouse listing
        if (networkConnection.getConnectivityStatusBoolean(this@InventoryTransferStandaloneActivity)) {
            materialProgressDialog.show()

            val networkClient = QuantityNetworkClient.create(this@InventoryTransferStandaloneActivity)
            networkClient.getQuantityValue(
                sessionManagement.getCompanyDB(applicationContext)!!,
                batchCode,
                itemCode,
                selectedFromWarehosueCode
            )
                .enqueue(object : Callback<GetQuantityModel> {
                    override fun onResponse(
                        call: Call<GetQuantityModel>,
                        response: Response<GetQuantityModel>
                    ) {
                        try {
                            materialProgressDialog.dismiss()
                            if (response.isSuccessful) {
                                val responseModel = response.body()!!
                                if (responseModel.value.isNotEmpty() && !responseModel.value[0].Quantity.isNullOrEmpty() && !responseModel.value[0].Quantity.equals("0.0")) {
                                    tempList.clear()

                                    for (i in itemList_gl.indices) {
                                        itemLineArrayList.add(itemList_gl[i])
                                    }


                                    Log.e("stringList", "Success=>" + responseModel.value)
                                    var stringList: ArrayList<String> = ArrayList()
                                    stringList.clear()
                                    stringList.add(responseModel.value[0].Quantity)


                                    AppConstants.scannedItemForGood.add(
                                        LocalListForGoods(
                                            DocEntry = itemList_gl[0].DocEntry.toInt(),
                                            ItemCode = itemCode,
                                            ItemDescription = itemList_gl[0].ItemDescription,
                                            Status = itemList_gl[0].Status,
                                            Batch = itemList_gl[0].Batch,
                                            SystemNumber = itemList_gl[0].SystemNumber.toInt(),
                                            SerialNumber = itemList_gl[0].SerialNumber,
                                            ScanType = "Batch",
                                            Quantity = "0.0",
                                            WareHouseCode = "",
                                            UnitPrice = "",
                                            BatchNumber = itemList_gl[0].Batch,
                                            SystemSerialNumber = itemList_gl[0].SystemNumber,
                                            InternalSerialNumber = itemList_gl[0].SerialNumber,
                                            wareHouseListing = responseModel.value as MutableList<GetQuantityModel.Value>
                                        )
                                    )

                                    Log.e("ITR_STAND", "BATCH LOCAL LIST>>>: ${AppConstants.scannedItemForGood}")
                                    Log.e("ITR_STAND", " BATCH LOCAL LISTSIZE>>>: ${AppConstants.scannedItemForGood.size}")
                                    goodsOrderLineAdapter!!.notifyDataSetChanged()

                                    val totalQuantity = AppConstants.scannedItemForGood.sumOf {
                                        GlobalMethods.changeDecimal(it.Quantity)!!.toDouble()
                                    }

//                                    binding.tvTotalScannQty.text = ": $totalQuantity"


                                    if (stringList.isNotEmpty() && !stringList.contains("0")) {

                                    } else {
                                        //  batchItemsAdapter?.notifyDataSetChanged()
                                        GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Batch / Roll No. has zero Quantity of this PO.")
                                    }
                                } else {
                                    GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "No Quantity Found of this Goods Issue.")
                                    //   batchItemsAdapter?.notifyDataSetChanged()
                                }
                            } else {
                                handleErrorResponse(response)
                            }
                        } catch (e: Exception) {
                            materialProgressDialog.dismiss()
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(call: Call<GetQuantityModel>, t: Throwable) {
                        Log.e("scanItemApiFailed-----", t.toString())
                        materialProgressDialog.dismiss()
                    }
                })
        } else {
            materialProgressDialog.dismiss()
            Toast.makeText(this@InventoryTransferStandaloneActivity, "No Network Connection", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun handleErrorResponse(response: Response<GetQuantityModel>) {
        materialProgressDialog.dismiss()
        val gson = GsonBuilder().create()
        try {
            val errorBody = response.errorBody()!!.string()
            val errorModel = gson.fromJson(errorBody, OtpErrorModel::class.java)
            errorModel.error.message.value?.let {
                GlobalMethods.showError(this@InventoryTransferStandaloneActivity, it)
                Log.e("json_error------", it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryTransferStandaloneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        clickListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initViews() {
        binding.edFromWarehouse.isFocusableInTouchMode = true
        binding.edFromWarehouse.requestFocus()
        supportActionBar?.hide()
        binding.etPostingDate.setText(GlobalMethods.getCurrentDate_dd_MM_yyyy())
        materialProgressDialog = MaterialProgressDialog(this@InventoryTransferStandaloneActivity)
        checkNetwoorkConnection = CheckNetwoorkConnection(application)
        sessionManagement = SessionManagement(this@InventoryTransferStandaloneActivity)
        networkConnection = NetworkConnection()
        callWarehouseListApi()
        goodsOrderLineAdapter = GoodsChanchalAdapter(this, AppConstants.scannedItemForGood, binding.tvTotalScannQty, this)
        binding.rvBatchItems.apply {
            adapter = goodsOrderLineAdapter
            layoutManager = LinearLayoutManager(this@InventoryTransferStandaloneActivity)
            //  setHasFixedSize(true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun clickListeners() {
        binding.apply {
            ivOnback.setOnClickListener {
                onBackPressed()
            }

            chipCancel.setOnClickListener {
                onBackPressed()
            }

            chipSave.setOnClickListener {
                getPostJson()
            }

            etPostingDate.setOnClickListener {
                GlobalMethods.datePicker(this@InventoryTransferStandaloneActivity, binding.etPostingDate)
            }
            if (sessionManagement.getScannerType(this@InventoryTransferStandaloneActivity) == "LEASER") {
                binding.ivScanBatchCode.visibility = View.GONE
                binding.edBatchCodeScan.requestFocus()
                binding.edFromWarehouse.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                        // You can handle the text before it changes here
                    }

                    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                        // This method is called when the text changes
                        val scannedResult = charSequence.toString()
                        if (!scannedResult.isNullOrEmpty()) {
                            binding.edFromWarehouse.setText(scannedResult)
                            // move focus to ToWarehouse
                            binding.edToWarehouse.isFocusableInTouchMode = true
                            binding.edToWarehouse.requestFocus()
                            val parts = scannedResult.split(",")
                            val lastPart = parts.last()
                            val itemCode = parts[0]
                            type = lastPart
                            BatchScannedData = scannedResult

                            if (parts.isNotEmpty()) {
                                fromWhareHouse = parts[0].trim()

                                if (parts.size > 2) {   // ✅ check at least 3 elements before accessing index 2
                                    fromBinLocation = parts[2].trim()
                                }

                                if (fromWhareHouse.isNotEmpty()) {
                                    //getBPL_IDNumber()

                                    // getBPLID(fromWhareHouse, "INVT")
                                    BPLID = if (Prefs.getString(AppConstants.BPLID, "").isNotEmpty()) Prefs.getString(AppConstants.BPLID, "") else ""
                                    getDocSeriesApi(BPLID)
                                }
                            }

                            // binding.edFromWhareHouse.setText("")
                        }
                    }

                    override fun afterTextChanged(editable: Editable?) {
                        // You can handle after text is changed here
                    }
                })
                binding.edToWarehouse.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                        // You can handle the text before it changes here
                    }

                    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                        // This method is called when the text changes
                        val scannedResult = charSequence.toString()
                        Log.i("ITR_STAND", "Item Scan Data: $scannedResult\nScanResult To warehouse: $scannedResult")

                        if (!scannedResult.isNullOrEmpty()) {
                            binding.edToWarehouse.setText(scannedResult)
                            binding.edBatchCodeScan.isFocusableInTouchMode = true
                            binding.edBatchCodeScan.requestFocus()  // move focus to ToWarehouse
                            val parts = scannedResult.split(",")
                            val lastPart = parts.last()
                            val itemCode = parts[0]
                            type = lastPart
                            BatchScannedData = scannedResult

                            if (parts.isNotEmpty()) {
                                toWhareHouse = parts[0].trim()

                                if (parts.size > 2) {   // ✅ check before using index 2
                                    toBinLocation = parts[2].trim()
                                }
                            }

                            // binding.edToWhareHouse.setText("")
                        }
                    }

                    override fun afterTextChanged(editable: Editable?) {
                        // You can handle after text is changed here
                    }
                })



                binding.edBatchCodeScan.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        //todo HIDE
                        Handler(Looper.getMainLooper()).postDelayed({
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            if (imm != null && binding.edBatchCodeScan != null) {
                                imm.hideSoftInputFromWindow(binding.edBatchCodeScan.windowToken, 0)
                            }
                        }, 200)

                        val scannedResult = s.toString().trim()

                        Log.i("ITR_STAND", "ScanResult: $scannedResult")
                        if (!scannedResult.isNullOrEmpty()) {
                            val parts = scannedResult.split(",")
                            val lastPart = parts.last()
                            val batchCode = parts.getOrNull(0).toString()
                            val itemCode = parts.getOrNull(1).toString()
                            type = parts.getOrNull(6).toString()
                            BatchScannedData = scannedResult

                            when (type) {
                                "Batch" -> {
                                    if (checkDuplicate(AppConstants.scannedItemForGood, batchCode)) {
                                        scanBatchLinesItem(batchCode, recyclerView, pos, itemCode, binding.tvTotalScannQty, type)
                                    }
                                }

                                "Serial" -> {
                                    if (checkDuplicateForSerial(AppConstants.scannedItemForGood, batchCode)) {
                                        //scanSerialLineItem(parts[1], recyclerView, pos, itemCode, binding.tvTotalScannQty, type)
                                    }
                                }

                                "NONE", "None" -> {
                                    itemDesc = parts[2]
                                    if (checkDuplicateForNone(AppConstants.scannedItemForGood, batchCode)) {
                                        callNoneBindFunction(itemCode, recyclerView, pos, binding.tvTotalScannQty, itemCode, batchCode)
                                    }
                                }

                                else -> {
                                    GlobalMethods.showMessage(this@InventoryTransferStandaloneActivity, "Scan Type is $type")
                                }
                            }
                        }
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

            } else if (sessionManagement.getScannerType(this@InventoryTransferStandaloneActivity) == "QR_SCANNER" || sessionManagement.getScannerType(
                    this@InventoryTransferStandaloneActivity
                ) == null
            ) {

                if (sessionManagement.getScannerType(this@InventoryTransferStandaloneActivity) == null) {
                    showPopupNotChooseScanner()
                } else {
                    /* val intent = Intent(this@InventoryTransferStandaloneActivity, QRScannerActivity::class.java)
                     (this@InventoryTransferStandaloneActivity).startActivityForResult(intent, REQUEST_CODE)*/

                    binding.apply {
                        ivScanBatchCode.visibility = View.VISIBLE
                        val intent = Intent(
                            this@InventoryTransferStandaloneActivity,
                            QRScannerActivity::class.java
                        )

                        ivScanBatchCode.setOnClickListener {
                            var text = edBatchCodeScan.text.toString().trim()
                            recyclerView = rvBatchItems
                            qrScannerLauncher.launch(intent)
                        }

                        ivScanFromWarehouse.setOnClickListener {

                            qrScannerFromWarehouseLauncher.launch(intent)
                        }

                        ivScanToWarehouse.setOnClickListener {

                            qrScannerToWarehouseLauncher.launch(intent)
                        }
                    }
                }
            }
        }
    }

    private fun callWarehouseListApi() {
        val apiConfig = ApiConstantForURL()
        QuantityNetworkClient.updateBaseUrlFromConfig(apiConfig, true)
        val networkClient = QuantityNetworkClient.create(this)
        networkClient.getWarehouse().apply {
            enqueue(object : Callback<GetWarehouseModel> {
                override fun onResponse(call: Call<GetWarehouseModel>, response: Response<GetWarehouseModel>) {
                    try {
                        if (response.isSuccessful) {
                            materialProgressDialog.dismiss()
                            var responseModel = response.body()!!
                            whList = responseModel.value as ArrayList<GetWarehouseModel.Value>
                            setFromWarehouseAdapter(whList)
                        } else {
                            materialProgressDialog.dismiss()

                            Prefs.clear()

                            val gson1 = GsonBuilder().create()
                            var mError: OtpErrorModel
                            try {
                                val s = response.errorBody()!!.string()
                                mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                if (mError.error.code.equals(400)) {
                                    GlobalMethods.showError(this@InventoryTransferStandaloneActivity, mError.error.message.value)
                                }
                                if (mError.error.message.value != null) {
                                    GlobalMethods.showError(this@InventoryTransferStandaloneActivity, mError.error.message.value)
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

                override fun onFailure(call: Call<GetWarehouseModel>, t: Throwable) {
                    Log.e("login_api_failure-----", t.toString())
                    materialProgressDialog.dismiss()
                    when (t) {
                        is SocketTimeoutException -> {
                            GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Connection timed out. Please try again.")
                        }

                        is IOException -> {
                            GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Network error. Please check your internet connection.")
                        }

                        else -> {
                            GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Something went wrong: ${t.localizedMessage}")
                        }
                    }
                    //Toast.makeText(this@PurchaseTransferLinesActivity, t.message, Toast.LENGTH_SHORT)
                }

            })
        }
    }

    private fun callSeriesByBPLIdApi(bplid: String) {
        val apiConfig = ApiConstantForURL()
        QuantityNetworkClient.updateBaseUrlFromConfig(apiConfig, true)
        val networkClient = QuantityNetworkClient.create(this)
        networkClient.getSeries(bplid).apply {
            enqueue(object : Callback<GetSeriesModel> {
                override fun onResponse(call: Call<GetSeriesModel>, response: Response<GetSeriesModel>) {
                    try {
                        if (response.isSuccessful) {
                            materialProgressDialog.dismiss()
                            var responseModel = response.body()!!
                            setSeriesSinner(responseModel.value)
                        } else {
                            materialProgressDialog.dismiss()

                            Prefs.clear()

                            val gson1 = GsonBuilder().create()
                            var mError: OtpErrorModel
                            try {
                                val s = response.errorBody()!!.string()
                                mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                if (mError.error.code.equals(400)) {
                                    GlobalMethods.showError(this@InventoryTransferStandaloneActivity, mError.error.message.value)
                                }
                                if (mError.error.message.value != null) {
                                    GlobalMethods.showError(this@InventoryTransferStandaloneActivity, mError.error.message.value)
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

                override fun onFailure(call: Call<GetSeriesModel>, t: Throwable) {
                    Log.e("login_api_failure-----", t.toString())
                    materialProgressDialog.dismiss()
                    when (t) {
                        is SocketTimeoutException -> {
                            GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Connection timed out. Please try again.")
                        }

                        is IOException -> {
                            GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Network error. Please check your internet connection.")
                        }

                        else -> {
                            GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Something went wrong: ${t.localizedMessage}")
                        }
                    }
//                                Prefs.clear()
                    //Toast.makeText(this@PurchaseTransferLinesActivity, t.message, Toast.LENGTH_SHORT)
                }

            })
        }
    }


    private fun setSeriesSinner(value: List<GetSeriesModel.Value>) {
        val seriesList = value.map { it.SeriesName }
        val adapter = ArrayAdapter(
            this@InventoryTransferStandaloneActivity,
            android.R.layout.simple_spinner_dropdown_item,
            seriesList
        )

        binding.acSeries.setAdapter(adapter)
        //binding.acScanType.hint = "Select DB"

        // Handle item selection
        binding.acSeries.setOnItemClickListener { _, _, position, _ ->
            selectedSeriesName = value[position].SeriesName
            selectedSeriesCode = value[position].Series

            // Set the text of the AutoCompleteTextView with the selected item
            binding.acSeries.setText(selectedSeriesName, false) //

        }
    }

    private fun setFromWarehouseAdapter(value: java.util.ArrayList<GetWarehouseModel.Value>) {
        // 1. Create display list with "WHCode - WHName"
        val whList = value.map { "${it.WareHouseCode} - ${it.WareHouseName}" }

        val adapter = ArrayAdapter(
            this@InventoryTransferStandaloneActivity,
            android.R.layout.simple_spinner_dropdown_item,
            whList
        )

        binding.acFromWarehouse.setAdapter(adapter)
        binding.acToWarehouse.setAdapter(adapter)

        // 2. Set default warehouse if matched
        /*val defaultWarehouseCode = productionOrderLineList_gl.getOrNull(0)?.WarehouseCode
        Log.e("WAREHOUSE_NAME", "Default Warehouse Code : $defaultWarehouseCode")

        // 3. Find index of matching warehouse code
        val defaultIndex = value.indexOfFirst { it.WareHouseCode.equals(defaultWarehouseCode, ignoreCase = true) }

        if (defaultIndex != -1) {
            val defaultDisplayName = "${value[defaultIndex].WareHouseCode} - ${value[defaultIndex].WareHouseName}"
            activityFormBinding.acWarehouse.setText(defaultDisplayName, false)

            // Set selected values
            selectedFromWarehosueCode = value[defaultIndex].WareHouseCode.toString()
            selectedFromWarehosueName = value[defaultIndex].WareHouseName.toString()
        }*/

        // 4. Handle selection change
        binding.acFromWarehouse.setOnItemClickListener { _, _, position, _ ->
            selectedFromWarehosueName = value[position].WareHouseName.toString()
            selectedFromWarehosueCode = value[position].WareHouseCode.toString()
            selectedBPLId = value[position].BPLid.toString()
            //callSeriesByBPLIdApi(selectedBPLId)
            getDocSeriesApi(selectedBPLId)
            binding.acFromWarehouse.setText("$selectedFromWarehosueCode - $selectedFromWarehosueName", false)

            // Update all line items with selected warehouse code
            //productionOrderLineList_gl.forEach { it.WarehouseCode = selectedFromWarehosueCode }

            Log.w("WAREHOUSE_NAME", "Warehouse From: $selectedFromWarehosueName, Code From: $selectedFromWarehosueCode")
        }

        binding.acToWarehouse.setOnItemClickListener { _, _, position, _ ->
            selectedToWarehosueName = value[position].WareHouseName.toString()
            selectedToWarehosueCode = value[position].WareHouseCode.toString()
            binding.acToWarehouse.setText("$selectedToWarehosueCode - $selectedToWarehosueName", false)

            // Update all line items with selected warehouse code
            //productionOrderLineList_gl.forEach { it.WarehouseCode = selectedFromWarehosueCode }

            Log.w("WAREHOUSE_NAME", "Warehouse To: $selectedToWarehosueName, Code To: $selectedToWarehosueCode")
        }
    }

    private fun getDocSeriesApi(bplidNum: String) {
        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            try {
                val customClient = ApiConstantForURL()
                QuantityNetworkClient.updateBaseUrlFromConfig(customClient, true)
                val customQtyClient = QuantityNetworkClient.create(this@InventoryTransferStandaloneActivity)
                customQtyClient.getDocSeries(bplidNum, "67").apply {
                    enqueue(object : Callback<GetDocSeriesModel> {
                        override fun onResponse(
                            call: Call<GetDocSeriesModel>,
                            response: Response<GetDocSeriesModel>
                        ) {
                            try {
                                materialProgressDialog.dismiss()
                                if (response.isSuccessful) {
                                    var responseModel = response.body()!!
                                    if (!responseModel.value.isNullOrEmpty()) {

                                        series = responseModel.value[0].Series
                                    } else {
                                        Toast.makeText(
                                            this@InventoryTransferStandaloneActivity,
                                            "Not Found!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    materialProgressDialog.dismiss()
                                    val gson1 = GsonBuilder().create()
                                    var mError: OtpErrorModel
                                    try {
                                        val s = response.errorBody()!!.string()
                                        mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                        if (mError.error.code.equals(400)) {
                                            GlobalMethods.showError(
                                                this@InventoryTransferStandaloneActivity,
                                                mError.error.message.value
                                            )
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(
                                                this@InventoryTransferStandaloneActivity,
                                                mError.error.message.value
                                            )
                                            Log.e("json_error------", mError.error.message.value)
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }

                            } catch (e: InvocationTargetException) {
                                materialProgressDialog.dismiss()
                                e.printStackTrace()
                                Log.e("error---------", e.toString())
                            }
                        }

                        override fun onFailure(call: Call<GetDocSeriesModel>, t: Throwable) {
                            Log.e("scannedItemFailure-----", t.toString())
                            materialProgressDialog.dismiss()
                        }

                    })
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("e-----------", e.toString())
            }

        } else {
            materialProgressDialog.dismiss()
            Toast.makeText(applicationContext, "No Network Connection", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPostJson() {

        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            if (selectedFromWarehosueCode.isNullOrEmpty()) {
                GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Please scan fromWarehouse first.")
                return
            } else if (selectedToWarehosueCode.isNullOrEmpty()) {
                GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Please scan toWarehouse first.")
                return
            } /*else if (selectedSeriesCode.isNullOrEmpty()) {
                GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Please select document series.")
                return
            }*/ else if (binding.etPostingDate.text.toString().isNullOrEmpty()) {
                GlobalMethods.showError(this@InventoryTransferStandaloneActivity, "Please select doc date.")
                return
            }


            val groupedItems = AppConstants.scannedItemForGood.groupBy { it.ItemCode }
            Log.e("Payload", "groupedItems => $groupedItems")

            val documentLinesArray = JsonArray()


            groupedItems.forEach { (itemCode, items) ->

                var itemJsonObject = JsonObject()

                val batchNumbersArray = JsonArray()
                val serialNumbersArray = JsonArray()
                val noneNumbersObj = JsonObject()
                var warehouseCode = ""

                Log.e("Payload", "groupedItems.forEach => itemCode : $itemCode items: $items")

                items.forEach { item ->
                    Log.e("Payload", "items.forEach => item: $item")
                    Log.e("Payload", "item.Batch => Batch: ${item.Batch} ScanType : ${item.ScanType}")
                    item.Batch?.let { batch ->
                        Log.e("Payload", "item.Batch => Batch: $batch")
                        if (batch.isNotEmpty()) {
                            itemJsonObject = JsonObject().apply {
                                addProperty("ItemCode", itemCode)
                                addProperty("WarehouseCode", selectedToWarehosueCode)
                                addProperty("FromWarehouseCode", selectedFromWarehosueCode)
                                addProperty("Quantity", items.sumOf { if (it.ScanType == "None") 0.0 else GlobalMethods.changeDecimal(it.Quantity)!!.toDouble() })
                            }
                            val batchNumberParts = batch.split(",")
                            Log.e("Payload", "Item.ItemCode => ${item.ItemCode} ItemCode == $itemCode")
                            if (item.ItemCode == itemCode && !item.ScanType.equals("None", ignoreCase = true)) {
                                //batchNumberParts.size > 0 && batchNumberParts[0] == itemCode && !item.ScanType.equals("None", ignoreCase = true)
                                val batchObject = JsonObject().apply {
                                    addProperty("BatchNumber", batch)
                                    addProperty("Quantity", item.Quantity)
//                                  addProperty("WareHouseCode", item.WareHouseCode)
                                }
                                batchNumbersArray.add(batchObject)
                            } else {
                                Log.e("Payload", "else => item.ItemCode == itemCode")
                            }
                            //todo remove shubh so that get warehouse from inner
                            warehouseCode = item.WareHouseCode

                        } else {
                            Log.e("Payload", "batch  => empty")
                        }


                    }



                    item.SerialNumber?.let { serial ->
                        if (serial.isNotEmpty()) {
                            itemJsonObject = JsonObject().apply {
                                addProperty("ItemCode", itemCode)
                                addProperty("WarehouseCode", selectedToWarehosueCode)
                                addProperty("FromWarehouseCode", selectedFromWarehosueCode)
                                addProperty("Quantity", items.sumOf { if (it.ScanType == "None") 0.0 else GlobalMethods.changeDecimal(it.Quantity)!!.toDouble() })
                            }

                            val serialNumberParts = serial.split(",")
                            if (item.ItemCode == itemCode && !item.ScanType.equals("None", ignoreCase = true)) {//serialNumberParts.size > 0 &&
                                val serialObject = JsonObject().apply {
                                    addProperty("SystemSerialNumber", item.SystemSerialNumber)
                                    // addProperty("InternalSerialNumber", item.InternalSerialNumber)
                                    addProperty("Quantity", "1")
//                                    addProperty("WareHouseCode", item.WareHouseCode)
                                }
                                serialNumbersArray.add(serialObject)
                            }
                            warehouseCode = item.WareHouseCode
                        }
                    }
                }


                itemJsonObject.add("StockTransferLinesBinAllocations", getJsonArray(items, items.size))


                if (batchNumbersArray.size() > 0) {
                    itemJsonObject.add("BatchNumbers", batchNumbersArray)
                }

                if (serialNumbersArray.size() > 0) {
                    itemJsonObject.add("SerialNumbers", serialNumbersArray)
                }

                if (warehouseCode.isNotEmpty()) {
                    itemJsonObject.addProperty("WarehouseCode", warehouseCode)
                }

                if (itemJsonObject.entrySet().size > 1) { // Ensure that the object has properties other than "ItemCode"
                    documentLinesArray.add(itemJsonObject)
                }
                // documentLinesArray.add(itemJsonObject)

                for (current in items) {
                    if (current.ScanType.equals("None", ignoreCase = true)) {
                        val serialObject = JsonObject().apply {
                            addProperty("ItemCode", current.ItemCode)
                            addProperty("Quantity", current.Quantity.toDouble())
                            addProperty("WarehouseCode", selectedToWarehosueCode)
                            addProperty("FromWarehouseCode", selectedFromWarehosueCode)
                        }
                        serialObject.add("StockTransferLinesBinAllocations", getJsonArray(items, items.size))

                        documentLinesArray.add(serialObject)
                    }

                }


            }
            val docDate = GlobalMethods.convert_dd_MM_yyyy_into_yyyy_MM_dd(binding.etPostingDate.text.toString())

            val finalJsonObject = JsonObject().apply {

                addProperty("DocDate", docDate)
                addProperty("DueDate", GlobalMethods.getCurrentDateFormatted())
                addProperty("TaxDate", GlobalMethods.getCurrentDateFormatted())
                addProperty("BPLID", BPLID)
                addProperty("Series", series)
                //addProperty("U_Type", "")
                addProperty("FromWarehouse", selectedFromWarehosueCode)
                addProperty("ToWarehouse", selectedToWarehosueCode)
                //addProperty("U_WMSUSER", sessionManagement.getUsername(this@InventoryTransferStandaloneActivity))  //WMS userName tagged (added by Vinod @25Apr,2025)
                //addProperty("U_WMSPOST", "Y") //WMS userName tagged (added by Vinod @25Apr,2025)
                add("StockTransferLines", documentLinesArray)
            }

            val gson = Gson()
            val json = gson.toJson(finalJsonObject)
            println(json)

            Log.e("ITR_STAND", "PayLoad: $json")
            materialProgressDialog.show()

            val networkClient = NetworkClients.create(this@InventoryTransferStandaloneActivity)
            networkClient.dostockTransfer(finalJsonObject).apply {
                enqueue(object : Callback<InventoryPostResponse> {
                    override fun onResponse(
                        call: Call<InventoryPostResponse>,
                        response: Response<InventoryPostResponse>
                    ) {
                        try {


                            AppConstants.IS_SCAN = false
                            materialProgressDialog.dismiss()
                            Log.e("success---BP---", "==>" + response.code())
                            if (response.code() == 201 || response.code() == 200) {
                                AppConstants.scannedItemForGood.clear()
                                Log.e("success------", "Successful!")
                                Log.d("Doc_Num", "onResponse: " + response.body()!!.DocNum.toString())

                                /*AestheticDialog.Builder(this@InventoryTransferStandaloneActivity, DialogStyle.EMOTION, DialogType.SUCCESS)
                                    .setTitle("Success")
                                    .setMessage("Inventory transfer standalone post successfully with docnum ${response.body()?.DocNum}")
                                    .show()*/

                                GlobalMethods.showSuccess(
                                    this@InventoryTransferStandaloneActivity,
                                    "Inventory transfer standalone post successfully with docnum ${response.body()?.DocNum}"
                                )
                                finish()
                                /* showSuccessDialog(
                                     context = this@InventoryTransferStandaloneActivity,
                                     title = "Inventory Transfer Standalone",
                                     successMsg = "Inventory transfer standalone post successfully with docnum ",
                                     docNum = response.body()?.DocNum.toString(),
                                     cancelable = true
                                 ) {
                                     finish()
                                 }*/
                            } else {
                                materialProgressDialog.dismiss()
                                val gson1 = GsonBuilder().create()
                                var mError: OtpErrorModel
                                try {
                                    val s = response.errorBody()!!.string()
                                    mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                    if (mError.error.code.equals(400)) {
                                        GlobalMethods.showError(
                                            this@InventoryTransferStandaloneActivity,
                                            mError.error.message.value
                                        )
                                    }
                                    if (mError.error.message.value != null) {
                                        GlobalMethods.showError(
                                            this@InventoryTransferStandaloneActivity,
                                            mError.error.message.value
                                        )
                                        Log.e("json_error------", mError.error.message.value)
                                    }
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }

                            }
                        } catch (e: Exception) {

                            materialProgressDialog.dismiss()
                            e.printStackTrace()
                            Log.e("catch---------", e.toString())
                        }

                    }

                    override fun onFailure(call: Call<InventoryPostResponse>, t: Throwable) {

                        Log.e("orderLines_failure-----", t.toString())
                        materialProgressDialog.dismiss()
                    }

                })
            }

        } else {
            materialProgressDialog.dismiss()

            Toast.makeText(this@InventoryTransferStandaloneActivity, "No Network Connection", Toast.LENGTH_SHORT).show()

        }

    }

    private fun getJsonArray(list: List<LocalListForGoods>, batcharraySize: Int): JsonArray {

        val stockBin = JsonArray()
        var baseLine = 0
        /*if (batcharraySize == 0) {
            baseLine = -1
        } else {
            baseLine = 0
        }*/
        if (list != null || !list.isEmpty()) {
            for (i in list.indices) {
                baseLine = i
                var fromObject = JsonObject()
                Log.i("FromToBinLocation", "FromBinLocation: $fromBinLocation")
                fromObject.addProperty("BinAbsEntry", fromBinLocation)
                fromObject.addProperty("Quantity", list.get(i).Quantity)
                fromObject.addProperty("BinActionType", "batFromWarehouse")
                fromObject.addProperty("SerialAndBatchNumbersBaseLine", baseLine)
                if (fromBinLocation.isNotEmpty())
                    stockBin.add(fromObject)


                var ToObject = JsonObject()
                Log.i("FromToBinLocation", "ToBinLocation: $toBinLocation")
                //ToObject.addProperty("BinAbsEntry",toBinLocation) previous
                ToObject.addProperty("BinAbsEntry", toBinLocation)
                ToObject.addProperty("Quantity", list.get(i).Quantity)
                ToObject.addProperty("BinActionType", "batToWarehouse")
                ToObject.addProperty("SerialAndBatchNumbersBaseLine", baseLine)
                if (toBinLocation.isNotEmpty())
                    stockBin.add(ToObject)
            }
        }

        return stockBin
    }

    private fun getBPL_IDNumber() {
        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            try {
                materialProgressDialog.show()
                val networkClient = NetworkClients.create(this@InventoryTransferStandaloneActivity)
                var batch = "Quality"//WIP
                networkClient.doGetBplID(
                    "BusinessPlaceID,WarehouseCode",
                    "WarehouseCode eq '$fromWhareHouse'"
                ).apply {
                    enqueue(object : Callback<WarehouseBPL_IDModel> {
                        override fun onResponse(
                            call: Call<WarehouseBPL_IDModel>,
                            response: Response<WarehouseBPL_IDModel>
                        ) {
                            try {
                                materialProgressDialog.dismiss()
                                if (response.isSuccessful) {
                                    var responseModel = response.body()!!
                                    if (!responseModel.value.isNullOrEmpty()) {
                                        //BPLIDNum = responseModel.value[0].BusinessPlaceID
//                                        getWarehouseCode = responseModel.value[0].WarehouseCode
                                        sessionManagement.setWarehouseCode(
                                            this@InventoryTransferStandaloneActivity,
                                            responseModel.value[0].WarehouseCode
                                        )
                                        //setAdapter()
//                                        Toast.makeText(this@ProductionOrderLinesActivity, BPLIDNum.toString(), Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(
                                            this@InventoryTransferStandaloneActivity,
                                            "Not Found!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    materialProgressDialog.dismiss()
                                    val gson1 = GsonBuilder().create()
                                    var mError: OtpErrorModel
                                    try {
                                        val s = response.errorBody()!!.string()
                                        mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                        if (mError.error.code.equals(400)) {
                                            GlobalMethods.showError(
                                                this@InventoryTransferStandaloneActivity,
                                                mError.error.message.value
                                            )
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(
                                                this@InventoryTransferStandaloneActivity,
                                                mError.error.message.value
                                            )
                                            Log.e("json_error------", mError.error.message.value)
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }

                            } catch (e: InvocationTargetException) {
                                materialProgressDialog.dismiss()
                                e.printStackTrace()
                                Log.e("error---------", e.toString())
                            }
                        }

                        override fun onFailure(call: Call<WarehouseBPL_IDModel>, t: Throwable) {
                            Log.e("scannedItemFailure-----", t.toString())
                            materialProgressDialog.dismiss()
                        }

                    })
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("e-----------", e.toString())
            }

        } else {
            materialProgressDialog.dismiss()
            Toast.makeText(applicationContext, "No Network Connection", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showPopupNotChooseScanner() {
        val builder =
            AlertDialog.Builder(this@InventoryTransferStandaloneActivity, R.style.CustomAlertDialog)
                .create()
        val view =
            LayoutInflater.from(this@InventoryTransferStandaloneActivity)
                .inflate(R.layout.custom_popup_alert, null)
        builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        builder.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        builder.window?.setGravity(Gravity.CENTER)
        builder.setView(view)

        //todo set ui..
        val cancelBtn = view.findViewById<MaterialButton>(R.id.cancel_btn)
        val yesBtn = view.findViewById<MaterialButton>(R.id.ok_btn)

        cancelBtn.setOnClickListener {
            builder.dismiss()
        }

        yesBtn.setOnClickListener {
            var intent = Intent(this@InventoryTransferStandaloneActivity, HomeActivity::class.java)
            startActivity(intent)
            builder.dismiss()

        }

        builder.setCancelable(true)
        builder.show()
    }

    //TODO duplicatcy checking from list...
    fun checkDuplicate(
        scanedBatchedItemsList_gl: MutableList<LocalListForGoods>,
        batchCode: String
    ): Boolean {
        var startus: Boolean = true;
        for (items in scanedBatchedItemsList_gl) {
            if (items.Batch == null) {
                startus = true
            } else if (items.Batch.equals(batchCode)) {
                startus = false
                Toast.makeText(this, "Batch no. Already Exists!", Toast.LENGTH_SHORT).show()
            }
        }
        return startus
    }


    fun checkDuplicateForSerial(
        scanedBatchedItemsList_gl: MutableList<LocalListForGoods>,
        batchCode: String
    ): Boolean {
        var startus: Boolean = true;
        for (items in scanedBatchedItemsList_gl) {
            if (items.SerialNumber == null) {
                startus = true
            } else if (items.SerialNumber.equals(batchCode)) {
                startus = false
                Toast.makeText(this, "Serial no. Already Exists!", Toast.LENGTH_SHORT).show()
            }
        }
        return startus
    }


    fun checkDuplicateForNone(
        scanedBatchedItemsList_gl: MutableList<LocalListForGoods>,
        batchCode: String
    ): Boolean {
        var startus: Boolean = true
        for (items in scanedBatchedItemsList_gl) {
            if (items.NoneVal == null) {
                startus = true
            } else if (items.NoneVal.equals(batchCode)) {
                startus = false
                Toast.makeText(this, "None no. Already Exists!", Toast.LENGTH_SHORT).show()
            }
        }
        return startus
    }

    override fun onQuantityChanged(position: Int, newQuantity: String, tvBatchQuantity: TextInputEditText) {
        //var maxQuantity = GlobalMethods.changeDecimal(AppConstants.scannedItemForGood[position].FixedQuantity)!!.toDoubleOrNull() ?: 0.0
        var QUANTITYVAL = newQuantity

        //val value = QUANTITYVAL.toIntOrNull() ?: 0

        if (QUANTITYVAL.isEmpty()) {
            QUANTITYVAL = "0"
        }

        if (!QUANTITYVAL.isNullOrEmpty()) {

            /* if (maxQuantity != null && value > maxQuantity)
                {
             GlobalMethods.showError(this, "Value cannot exceed then Open Quantity")
             tvBatchQuantity.setText("")
                }
             else { }*/

            AppConstants.scannedItemForGood[position].Quantity = QUANTITYVAL
            val totalQuantity = AppConstants.scannedItemForGood.sumOf {
                GlobalMethods.changeDecimal(it.Quantity)!!.toDouble()
            }

            binding.tvTotalScannQty.text = ": $totalQuantity"

            Log.e("ITR_STAND", "onQuantityChanged: ${AppConstants.scannedItemForGood[position].Quantity} ")
        }
    }

    override fun onItemRemoved(position: Int) {
        var batch = ""

        if (!AppConstants.scannedItemForGood[position].Batch.isNullOrEmpty()) {
            batch = AppConstants.scannedItemForGood[position].Batch.toString()
        } else
            batch = AppConstants.scannedItemForGood[position].SerialNumber.toString()


        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm...")
            .setMessage("Do you want to delete " + batch + " Item .")
            .setIcon(R.drawable.ic_trash)
            .setPositiveButton("Confirm",
                DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i1: Int ->
                    goodsOrderLineAdapter!!.removeItem(position)
                    goodsOrderLineAdapter!!.notifyDataSetChanged()
                    val totalQuantity = AppConstants.scannedItemForGood.sumOf {
                        GlobalMethods.changeDecimal(it.Quantity)!!.toDouble()
                    }

                    binding.tvTotalScannQty.text = ": $totalQuantity"
                    Log.e("ITR_STAND", "onItemRemoved: ${AppConstants.scannedItemForGood.size}")
                    Log.e("ITR_STAND", "onItemRemoved: ${AppConstants.scannedItemForGood}")


                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })

            .show()
    }

    override fun onWareHouseChanged(position: Int, newQuantity: String, warehouse: String, currentItem: LocalListForGoods) {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        AppConstants.scannedItemForGood.clear()
    }

}