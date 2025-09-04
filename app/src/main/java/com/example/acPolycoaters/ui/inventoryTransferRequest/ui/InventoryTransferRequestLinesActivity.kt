package com.example.acPolycoaters.ui.inventoryTransferRequest.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acPolycoaters.Global_Classes.AppConstants
import com.example.acPolycoaters.Global_Classes.GlobalMethods
import com.example.acPolycoaters.Global_Classes.MaterialProgressDialog
import com.example.acPolycoaters.Global_Notification.NetworkConnection
import com.example.acPolycoaters.Model.GetDocSeriesModel
import com.example.acPolycoaters.Model.OtpErrorModel
import com.example.acPolycoaters.R
import com.example.acPolycoaters.Retrofit_Api.ApiConstantForURL
import com.example.acPolycoaters.Retrofit_Api.NetworkClients
import com.example.acPolycoaters.Retrofit_Api.QuantityNetworkClient
import com.example.acPolycoaters.SessionManagement.SessionManagement
import com.example.acPolycoaters.databinding.ActivityInventoryTransferRequestLinesBinding
import com.example.acPolycoaters.interfaces.PassList
import com.example.acPolycoaters.ui.inventoryTransferRequest.adapter.InventoryTransferItemAdapter
import com.example.acPolycoaters.ui.inventoryTransferRequest.model.InventoryRequestModel
import com.example.acPolycoaters.ui.inventoryTransferStandalone.model.InventoryPostResponse
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ScanedOrderBatchedItems
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pixplicity.easyprefs.library.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.SocketTimeoutException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InventoryTransferRequestLinesActivity : AppCompatActivity(), PassList, InventoryTransferItemAdapter.AdapterCallback {
    var inventoryItem: InventoryRequestModel.Value? = null
    var fromWarehouse = ""
    var toWarehouse = ""
    private var selectedSeriesName = ""
    private var selectedSeries = ""
    private var series: String = ""

    private lateinit var activityFormBinding: ActivityInventoryTransferRequestLinesBinding

    //    private lateinit var InventoryTransferItemAdapter: DemoAdapter
    private lateinit var InventoryTransferItemAdapter: InventoryTransferItemAdapter
    private lateinit var productionOrderLineList_gl: ArrayList<InventoryRequestModel.StockTransferLines>

    var position: Int? = 0
    lateinit var networkConnection: NetworkConnection
    lateinit var materialProgressDialog: MaterialProgressDialog
    private lateinit var sessionManagement: SessionManagement
    private var BPLIDNum = 0
    private var valueList: List<InventoryRequestModel.StockTransferLines> = ArrayList()
    private var connection: Connection? = null
    var openQty = 0.0

    //todo batch scan and quantity list interface override...
    var hashMapBatchList: HashMap<String, ArrayList<ScanedOrderBatchedItems.Value>> = HashMap<String, ArrayList<ScanedOrderBatchedItems.Value>>()
    var hashmapBatchQuantityList: HashMap<String, ArrayList<String>> = HashMap<String, ArrayList<String>>()
    var serialHashMapQuantityList: java.util.HashMap<String, ArrayList<String>> = java.util.HashMap<String, ArrayList<String>>()
    var noneHashMapQuantityList: java.util.HashMap<String, ArrayList<String>> = java.util.HashMap<String, ArrayList<String>>()


    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFormBinding = ActivityInventoryTransferRequestLinesBinding.inflate(layoutInflater)
        setContentView(activityFormBinding.root)

        Log.e("warehouse", "onCreate:InOrder Current")
        title = "Form Screen"
        try {
            inventoryItem = intent.getSerializableExtra("inventReqModel") as InventoryRequestModel.Value
            fromWarehouse = inventoryItem?.FromWarehouse.toString()
            toWarehouse = inventoryItem?.ToWarehouse.toString()
            activityFormBinding.etFromWarehouse.setText(fromWarehouse)
            activityFormBinding.etToWarehouse.setText(toWarehouse)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

        deleteCache(this)

        supportActionBar?.hide()

        Log.d("checking", "Working Tarun")

        //activityFormBinding.ivLaserCode.setFocusable(true)
        //activityFormBinding.ivLaserCode.requestFocus()

        /*Handler(Looper.getMainLooper()).postDelayed({
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            if (imm != null && currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            }
        }, 200)*/


        //todo initialization...
        networkConnection = NetworkConnection()
        materialProgressDialog = MaterialProgressDialog(this@InventoryTransferRequestLinesActivity)
        sessionManagement = SessionManagement(this@InventoryTransferRequestLinesActivity)


        //todo get arguments data...
        try {
            val intent = intent
            productionOrderLineList_gl = intent.getSerializableExtra("productionLinesList") as ArrayList<InventoryRequestModel.StockTransferLines>

            position = intent.extras?.getInt("pos")
            activityFormBinding.tvTitle.text = "Request No : " + inventoryItem!!.DocNum
            Log.e("TAG", "onCreate:===> " + productionOrderLineList_gl.size)

            setAdapter()
           val BPLID = if (Prefs.getString(AppConstants.BPLID, "").isNotEmpty()) Prefs.getString(AppConstants.BPLID, "") else ""
            getDocSeriesApi(BPLID)

        } catch (e: IOException) {
            Log.e("TAG", "onCreate:===> " + e.message)

            e.printStackTrace()
        }

        activityFormBinding.etFromWarehouse.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // You can handle the text before it changes here
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val text = charSequence.toString().trim()

                if (text.isNotEmpty()) {
                    val arr = text.split(",")
                    if (arr.size > 1) {
                        fromWarehouse = arr[0].trim()
                        //sessionManagement.setInvReqWarehouseCode(this@InventoryTransferRequestLinesActivity, fromWarehouse)
                        activityFormBinding.etFromWarehouse.removeTextChangedListener(this) // avoid infinite loop
                        activityFormBinding.etFromWarehouse.setText(fromWarehouse)
                        activityFormBinding.etFromWarehouse.setSelection(fromWarehouse.length) // place cursor at end
                        activityFormBinding.etFromWarehouse.addTextChangedListener(this)
                    } else {
                        fromWarehouse = text
                        activityFormBinding.etFromWarehouse.setSelection(fromWarehouse.length) // place cursor at end
                    }
                    sessionManagement.setWarehouseCode(this@InventoryTransferRequestLinesActivity, fromWarehouse)
                }

                Log.i("WAREHOUSE", "FromWarehouse: $fromWarehouse")
            }

            override fun afterTextChanged(editable: Editable?) {
                // You can handle after text is changed here
            }
        })

        activityFormBinding.etToWarehouse.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // You can handle the text before it changes here
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val text = charSequence.toString().trim()

                if (text.isNotEmpty()) {
                    val arr = text.split(",")
                    if (arr.size > 1) {
                        toWarehouse = arr[0].trim()
                        activityFormBinding.etToWarehouse.removeTextChangedListener(this)
                        activityFormBinding.etToWarehouse.setText(toWarehouse)
                        activityFormBinding.etToWarehouse.setSelection(toWarehouse.length)
                        activityFormBinding.etToWarehouse.addTextChangedListener(this)
                    } else {
                        toWarehouse = text
                        activityFormBinding.etFromWarehouse.setSelection(toWarehouse.length) // place cursor at end
                    }
                }
            }

            override fun afterTextChanged(editable: Editable?) {
                // You can handle after text is changed here
            }
        })

        activityFormBinding.etFromWarehouse.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // Assuming ENTER signifies the end of a scan
                val scannedCode = activityFormBinding.etFromWarehouse.text.toString().trim()
                // The TextWatcher will handle updating fromWarehouse and the EditText
                // You might want to add additional logic here, like validating the scanned code
                // or automatically shifting focus to etToWarehouse if fromWarehouse is complete.
                Log.d("Scan", "Scanned From Warehouse: $scannedCode")
                true // Consume the event
            } else {
                false
            }
        }

        activityFormBinding.etToWarehouse.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val scannedCode = activityFormBinding.etToWarehouse.text.toString().trim()
                Log.d("Scan", "Scanned To Warehouse: $scannedCode")
                // The TextWatcher will handle updating toWarehouse and the EditText
                true // Consume the event
            } else {
                false
            }
        }

        //getDocSeries()

        activityFormBinding.ivOnback.setOnClickListener {
            onBackPressed()
        }


        //todo cancel lines...
        activityFormBinding.chipCancel.setOnClickListener {
            onBackPressed()
        }
    }

    /*private fun getDocSeries() {
        val apiConfig = ApiConstantForURL()
        QuantityNetworkClient.updateBaseUrlFromConfig(apiConfig)
        val networkClient = QuantityNetworkClient.create(this)
        val bplId = if (Prefs.getString(AppConstants.BPLID, "").isNotEmpty()) Prefs.getString(AppConstants.BPLID, "") else ""
        networkClient.getDocSeries(bplId, "67").apply {
            enqueue(object : Callback<ModelSeries> {
                override fun onResponse(call: Call<ModelSeries>, response: Response<ModelSeries>) {
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
                                    GlobalMethods.showError(this@InventoryTransferRequestLinesActivity, mError.error.message.value)
                                }
                                if (mError.error.message.value != null) {
                                    GlobalMethods.showError(this@InventoryTransferRequestLinesActivity, mError.error.message.value)
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

                override fun onFailure(call: Call<ModelSeries>, t: Throwable) {
                    Log.e("login_api_failure-----", t.toString())
                    materialProgressDialog.dismiss()
                    when (t) {
                        is SocketTimeoutException -> {
                            GlobalMethods.showError(this@InventoryTransferRequestLinesActivity, "Connection timed out. Please try again.")
                        }

                        is IOException -> {
                            GlobalMethods.showError(this@InventoryTransferRequestLinesActivity, "Network error. Please check your internet connection.")
                        }

                        else -> {
                            GlobalMethods.showError(this@InventoryTransferRequestLinesActivity, "Something went wrong: ${t.localizedMessage}")
                        }
                    }
//                                Prefs.clear()
                    Log.e("DBURL==>", "onFailure: " + Prefs.getString(AppConstants.DBUrl, ""))

                    //Toast.makeText(this@PurchaseTransferLinesActivity, t.message, Toast.LENGTH_SHORT)
                }

            })
        }
    }
    private fun setSeriesSinner(value: List<ModelSeries.Value>) {
        val seriesList = value.map { it.SeriesName.toString() }
        val adapter = ArrayAdapter(
            this@InventoryTransferRequestLinesActivity,
            android.R.layout.simple_spinner_dropdown_item,
            seriesList
        )

        activityFormBinding.acSeries.setAdapter(adapter)
        //binding.acScanType.hint = "Select DB"

        // Handle item selection
        activityFormBinding.acSeries.setOnItemClickListener { _, _, position, _ ->
            selectedSeriesName = value[position].SeriesName
            selectedSeries = value[position].Series

            // Set the text of the AutoCompleteTextView with the selected item
            activityFormBinding.acSeries.setText(selectedSeriesName, false) //

        }
    }*/

    //todo set adapter....
    fun setAdapter() {

        activityFormBinding.ivNoDataFound.visibility = View.GONE
        activityFormBinding.rvProductionOrderList.visibility = View.VISIBLE
        activityFormBinding.btnLinearLayout.visibility = View.VISIBLE
        productionOrderLineList_gl =
            setFilteredList(productionOrderLineList_gl) as ArrayList<InventoryRequestModel.StockTransferLines> /* = java.util.ArrayList<com.soothe.sapApplication.ui.issueForProductionOrder.Model.InventoryRequestModel.StockTransferLines> */

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        activityFormBinding.rvProductionOrderList.layoutManager = layoutManager
        //todo parse save button in adapter constructor for click listener on adapter...

        InventoryTransferItemAdapter = InventoryTransferItemAdapter(
            this@InventoryTransferRequestLinesActivity, productionOrderLineList_gl, networkConnection, materialProgressDialog, this@InventoryTransferRequestLinesActivity, activityFormBinding.chipSave
        )

        activityFormBinding.rvProductionOrderList.adapter = InventoryTransferItemAdapter
    }

    var productionOrderLineList_temp: MutableList<InventoryRequestModel.StockTransferLines> = mutableListOf()

    private fun setFilteredList(arrayList: java.util.ArrayList<InventoryRequestModel.StockTransferLines>): MutableList<InventoryRequestModel.StockTransferLines> {

        var position = -1
        for ((index, item) in arrayList.withIndex()) {
            if (item is InventoryRequestModel.StockTransferLines && item.RemainingOpenQuantity.toDouble() > 0 && item.RemainingOpenQuantity != "0.0") {
                productionOrderLineList_temp.add(item)
            }


        }
        return productionOrderLineList_temp

    }

    //todo override function for save items posting
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onApiResponseStock(
        response: HashMap<String, ArrayList<ScanedOrderBatchedItems.Value>>,
        listResponse: ArrayList<InventoryRequestModel.StockTransferLines>,
        quantityResponse: HashMap<String, ArrayList<String>>,
        serialQuantityResponse: java.util.HashMap<String, ArrayList<String>>,
        noneQuantityResponse: java.util.HashMap<String, ArrayList<String>>
    ) {
        Log.e("hashmap--->", quantityResponse.toString())

        hashMapBatchList = response
        hashmapBatchQuantityList = quantityResponse
        serialHashMapQuantityList = serialQuantityResponse
        noneHashMapQuantityList = noneQuantityResponse
        Log.w("INVENT_REQ","hashMapBatchList: $hashMapBatchList\nhashmapBatchQuantityList: $hashmapBatchQuantityList")
        postInventorystock(inventoryItem!!, listResponse)

    }


    lateinit var batchList: List<ScanedOrderBatchedItems.Value>
    lateinit var batchQuantityList: MutableList<String>


    //todo here save issue for production lines items of order...
    @RequiresApi(Build.VERSION_CODES.O)
    private fun postInventorystock(
        inventoryItem: InventoryRequestModel.Value,
        list: List<InventoryRequestModel.StockTransferLines>
    ) {
        val now = LocalDateTime.now()
        val formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

        fun showError(msg: String) {
            GlobalMethods.showError(this@InventoryTransferRequestLinesActivity, msg)
        }

        if (fromWarehouse.isNullOrEmpty()) return showError("fromWarehouse can't be empty.")
        if (toWarehouse.isNullOrEmpty()) return showError("toWarehouse can't be empty.")
        //if (selectedSeries.isNullOrEmpty()) return showError("Please select document series.")

        val postedJson = JsonObject().apply {
            addProperty("Series", series)
            addProperty("DocDate", formattedDate)
            addProperty("DueDate", inventoryItem.DueDate)
            addProperty("CardCode", inventoryItem.CardCode)
            addProperty("Comments", inventoryItem.Comments)
            addProperty("FromWarehouse", fromWarehouse)
            addProperty("ToWarehouse", toWarehouse)
            addProperty("TaxDate", inventoryItem.TaxDate)
            addProperty("DocObjectCode", "67")
            addProperty("BPLID", inventoryItem.BPLID)
            addProperty("ShipToCode", inventoryItem.ShipToCode)
            addProperty("U_Type", inventoryItem.DocType)
            //addProperty("U_WMSPOST", "Y") //U_WMSPOST tagged (added by Vinod @13Aug,2025)
            //addProperty("U_WMSUSER", sessionManagement.getUsername(this@InventoryTransferRequestLinesActivity))  //WMS userName tagged (added by Vinod @13Aug,2025)
        }

        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            val stockTransferArray = JsonArray()

            list.forEachIndexed { idx, line ->
                val wasScanned = line.isScanned > 0
                val usesBatch = line.Batch.equals("Y", true)
                val usesSerial = line.Serial.equals("Y", true)
                val usesNone = line.None.equals("Y", true)
                Log.i("INVENT_REQ", "binAllocationJSONs ($idx) =>\n${GlobalMethods.toPrettyJson(list[idx].binAllocationJSONs)}")
                if (!wasScanned) return@forEachIndexed

                val quantities: List<String> =
                    when {
                        usesBatch -> hashmapBatchQuantityList["Item$idx"] ?: emptyList()
                        usesSerial -> serialHashMapQuantityList["Item$idx"] ?: emptyList()
                        usesNone -> noneHashMapQuantityList["Item$idx"] ?: emptyList()
                        else -> emptyList()
                    }

                if (quantities.isEmpty()) return@forEachIndexed

                val qtySum = GlobalMethods.sumBatchQuantity(idx, quantities as ArrayList<String>)

                val itemObj = JsonObject().apply {
                    addProperty("BaseEntry", line.DocEntry)
                    addProperty("ItemCode", line.ItemCode)
                    addProperty("BaseLine", line.LineNum)
                    addProperty("BaseType", line.BaseType)
                    addProperty("FromWarehouseCode", fromWarehouse)
                    addProperty("Price", line.Price)
                    addProperty("UnitPrice", line.UnitPrice)
                    addProperty("WarehouseCode", toWarehouse)
                    addProperty("U_Size", line.Size)
                    addProperty("Quantity", qtySum.toString())
                }

                // Prepare BatchNumbers or SerialNumbers
                val batchList = hashMapBatchList["Item$idx"] ?: emptyList()
                if (usesBatch && batchList.isNotEmpty()) {
                    val batchJsonArr = JsonArray()
                    batchList.forEachIndexed { bi, batchItem ->
                        val jo = JsonObject().apply {
                            addProperty("BatchNumber", batchItem.Batch)
                            addProperty("SystemSerialNumber", batchItem.SystemNumber)
                            addProperty("Quantity", quantities.getOrNull(bi) ?: "0")
                        }
                        batchJsonArr.add(jo)
                    }
                    itemObj.add("BatchNumbers", batchJsonArr)
                }

                // Add bin allocations if needed
                if (line.BinManaged.equals("Y", true)) {
                    if (line.binAllocationJSONs.isNullOrEmpty()) {
                        return showError("Please select a bin location before proceeding with batch item selection.")
                    }
                    val binArr = getJsonArray(line.binAllocationJSONs, batchList)
                    itemObj.add("StockTransferLinesBinAllocations", binArr)
                }

                // If it's serial-based item, build SerialNumbers array instead
                if (usesSerial && batchList.isNotEmpty()) {
                    val serialArr = JsonArray()
                    batchList.forEachIndexed { bi, s ->
                        serialArr.add(JsonObject().apply {
                            addProperty("SystemSerialNumber", s.SystemNumber)
                            addProperty("InternalSerialNumber", s.SerialNumber)
                            addProperty("Quantity", "1")
                        })
                    }
                    itemObj.add("SerialNumbers", serialArr)
                }

                // Add to parent array
                stockTransferArray.add(itemObj)
            }

            postedJson.add("StockTransferLines", stockTransferArray)
            Log.e("success--PayLoad==>", "==> ${GlobalMethods.toPrettyJson(postedJson)}")
        }

        if (false)
            return

        materialProgressDialog.show()
        var apiConfig = ApiConstantForURL()

        val networkClient = NetworkClients.create(this@InventoryTransferRequestLinesActivity)
        networkClient.dostockTransfer(postedJson).apply {
            enqueue(object : Callback<InventoryPostResponse> {
                override fun onResponse(
                    call: Call<InventoryPostResponse>, response: Response<InventoryPostResponse>
                ) {
                    try {
                        activityFormBinding.chipSave.isEnabled = true
                        activityFormBinding.chipSave.isCheckable = true

                        AppConstants.IS_SCAN = false
                        materialProgressDialog.dismiss()
                        Log.e("success---BP---", "==>" + response.code())
                        if (response.code() == 201 || response.code() == 200) {
                            Log.e("success------", "Successful!")
                            Log.d("Doc_Num", "onResponse: " + response.body()!!.DocNum.toString())
                            GlobalMethods.showSuccess(this@InventoryTransferRequestLinesActivity, "Post Successfully. " + response.body()!!.DocNum.toString())
                            finish()
                            /*GlobalMethods.showSuccessDialog(
                                context = this@InventoryTransferRequestLinesActivity,
                                title = "Inventory Request",
                                successMsg = "Inventory transfer request post successfully with docnum ",
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
                                        this@InventoryTransferRequestLinesActivity, mError.error.message.value
                                    )
                                }
                                if (mError.error.message.value != null) {
                                    GlobalMethods.showError(
                                        this@InventoryTransferRequestLinesActivity, mError.error.message.value
                                    )
                                    Log.e("json_error------", mError.error.message.value)
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                    } catch (e: Exception) {
                        activityFormBinding.chipSave.isEnabled = true
                        activityFormBinding.chipSave.isCheckable = true
                        materialProgressDialog.dismiss()
                        e.printStackTrace()
                        Log.e("catch---------", e.toString())
                    }

                }

                override fun onFailure(call: Call<InventoryPostResponse>, t: Throwable) {
                    activityFormBinding.chipSave.isEnabled = true
                    activityFormBinding.chipSave.isCheckable = true
                    Log.e("orderLines_failure-----", t.toString())
                    materialProgressDialog.dismiss()
                }

            })
        }
    }


    /*private fun getTotalQTY(list: ArrayList<PurchaseRequestModel.binAllocationJSONs>): Double {
        var total = 0.0

        if (list != null || !list.isEmpty()) {
            for (i in list.indices) {

                if (!list.get(i).Quantity.trim().isEmpty()) {
                    total += list.get(i).Quantity.trim().toDouble()

                }

            }
        }

        return total
    }*/


    private fun getJsonArray(
        allocations: ArrayList<InventoryRequestModel.binAllocationJSONs>,
        batchList: List<ScanedOrderBatchedItems.Value>
    ): JsonArray {
        val stockBin = JsonArray()

        batchList.forEachIndexed { batchIndex, batch ->
            // Filter allocations for the current batch
            allocations.forEach { allocation ->
                Log.i("INVENT_REQ", "binLocation Batch: ${allocation.BinLocation} (${batch.Batch})")
            }

            val batchAllocations = allocations.filter { it.BatchNum == batch.Batch }

            // Group and sum FROM bins for this batch
            val fromGrouped = batchAllocations.groupBy { it.BinAbsEntry }
                .mapValues { entry -> entry.value.sumOf { it.Quantity.toDouble() } }

            // Group and sum TO bins for this batch
            val toGrouped = batchAllocations.groupBy { it.ToBinAbsEntry }
                .mapValues { entry -> entry.value.sumOf { it.Quantity.toDouble() } }

            // Add FROM bins
            fromGrouped.forEach { (binAbsEntry, totalQty) ->
                val fromObject = JsonObject().apply {
                    addProperty("BinAbsEntry", binAbsEntry)
                    addProperty("Quantity", totalQty)
                    addProperty("BinActionType", "batFromWarehouse")
                    addProperty("SerialAndBatchNumbersBaseLine", batchIndex)
                }
                stockBin.add(fromObject)
            }

            // Add TO bins
            toGrouped.forEach { (toBinAbsEntry, totalQty) ->
                val toObject = JsonObject().apply {
                    addProperty("BinAbsEntry", toBinAbsEntry)
                    addProperty("Quantity", totalQty)
                    addProperty("BinActionType", "batToWarehouse")
                    addProperty("SerialAndBatchNumbersBaseLine", batchIndex)
                }
                stockBin.add(toObject)
            }
        }

        return stockBin
    }

    val handler = Handler(Looper.getMainLooper())


    //todo getting BPL_ID Number ....
    /*private fun getBPL_IDNumber() {
        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            try {
                materialProgressDialog.show()
                var apiConfig = ApiConstantForURL()

                NetworkClients.updateBaseUrlFromConfig(apiConfig)

                QuantityNetworkClient.updateBaseUrlFromConfig(apiConfig)

                val networkClient = NetworkClients.create(this@InventoryTransferRequestLinesActivity)
                var batch = "Quality"//WIP
                networkClient.doGetBplID(
                    "BusinessPlaceID,WarehouseCode", "WarehouseCode eq '" + sessionManagement.getWarehouseCode(this) + "'"
                ).apply {
                    enqueue(object : Callback<WarehouseBPL_IDModel> {
                        override fun onResponse(
                            call: Call<WarehouseBPL_IDModel>, response: Response<WarehouseBPL_IDModel>
                        ) {
                            try {
                                materialProgressDialog.dismiss()
                                if (response.isSuccessful) {
                                    var responseModel = response.body()!!
                                    if (!responseModel.value.isNullOrEmpty()) {
                                        BPLIDNum = responseModel.value[0].BusinessPlaceID
//                                        getWarehouseCode = responseModel.value[0].WarehouseCode
                                        sessionManagement.setWarehouseCode(
                                            this@InventoryTransferRequestLinesActivity, responseModel.value[0].WarehouseCode
                                        )
                                        setAdapter()
//                                        Toast.makeText(this@InventoryTransferLinesActivity, BPLIDNum.toString(), Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(
                                            this@InventoryTransferRequestLinesActivity, "Not Found!", Toast.LENGTH_SHORT
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
                                                this@InventoryTransferRequestLinesActivity, mError.error.message.value
                                            )
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(
                                                this@InventoryTransferRequestLinesActivity, mError.error.message.value
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

    }*/


    //todo getting api list response from adapter to activity trough interface...
    override fun passList(dataList: List<ScanedOrderBatchedItems.Value>) {
        batchList = dataList
    }

    //todo onActivity function override for qr code scanning in adapter..
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            Log.e("Result==>B", data?.getStringExtra("batch_code").toString())
            InventoryTransferItemAdapter.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getDocSeriesApi(bplidNum: String) {
        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            try {
                val customClient = ApiConstantForURL()
                QuantityNetworkClient.updateBaseUrlFromConfig(customClient, true)
                val customQtyClient = QuantityNetworkClient.create(this@InventoryTransferRequestLinesActivity)
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
                                            this@InventoryTransferRequestLinesActivity,
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
                                                this@InventoryTransferRequestLinesActivity,
                                                mError.error.message.value
                                            )
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(
                                                this@InventoryTransferRequestLinesActivity,
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

    //todo query for series..
    fun getSeriesValue(docDate: String): String {
        var series = ""
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                var resultSet =
                    statement.executeQuery("Select  T0. Series as SeriesCode, T0.SeriesName  From NNM1 T0 WHERE T0.ObjectCode ='60'  and T0.Indicator=(select distinct Indicator from OFPR where '$docDate' between F_RefDate and T_RefDate ) and T0.Locked='N'")
                while (resultSet.next()) {
                    Log.e("ConStatus", "Success=>" + resultSet.getString(1))
                    //todo remove zero digits from quantity...
                    series = resultSet.getString(1)
                    Log.e("series", "Success=>" + series)
                }

            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.e("Result=>", "Connection is null")
        }
        return series
    }

    //TODO set sql server for query...
    private fun setSqlServer() {
        val url = "jdbc:jtds:sqlserver://" + AppConstants.IP + ":" + AppConstants.PORT + "/" + AppConstants.COMPANY_DB
        ActivityCompat.requestPermissions(
            this as Activity, arrayOf<String>(Manifest.permission.INTERNET), PackageManager.PERMISSION_GRANTED
        )
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(AppConstants.Classes)
            connection = DriverManager.getConnection(url, AppConstants.USERNAME, AppConstants.PASSWORD)
            Log.e("ConStatus", "Success$connection")

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Log.e("ConStatus", "Error")
        } catch (e: SQLException) {
            e.printStackTrace()
            Log.e("ConStatus", "Failure")
        }
    }


    fun deleteCache(context: Activity) {
        try {
            val dir: File = context.getCacheDir()
            deleteDir(dir)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }


    private val barcode = StringBuffer()

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {

        if (event?.action == KeyEvent.ACTION_DOWN) {
            val pressedKey = event.unicodeChar.toChar()
            barcode.append(pressedKey)
        }
        if (event?.action == KeyEvent.ACTION_DOWN && event?.keyCode == KeyEvent.KEYCODE_ENTER) {
            barcode.delete(0, barcode.length)
        }

        return super.dispatchKeyEvent(event)
    }
}