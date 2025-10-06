package com.example.acPolycoaters.ui.deliveryOrderModule.Adapter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.StrictMode
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acPolycoaters.Global_Classes.AppConstants
import com.example.acPolycoaters.Global_Classes.GlobalMethods
import com.example.acPolycoaters.Global_Classes.GlobalMethods.toSimpleJson
import com.example.acPolycoaters.Global_Classes.MaterialProgressDialog
import com.example.acPolycoaters.Global_Notification.NetworkConnection
import com.example.acPolycoaters.Model.GetQuantityModel
import com.example.acPolycoaters.Model.OtpErrorModel
import com.example.acPolycoaters.R
import com.example.acPolycoaters.Retrofit_Api.ApiConstantForURL
import com.example.acPolycoaters.Retrofit_Api.NetworkClients
import com.example.acPolycoaters.Retrofit_Api.QuantityNetworkClient
import com.example.acPolycoaters.SessionManagement.SessionManagement
import com.example.acPolycoaters.databinding.DeliveryOrderLinesAdapterLayoutBinding
import com.example.acPolycoaters.ui.deliveryOrderModule.Model.DeliveryModel
import com.example.acPolycoaters.ui.deliveryOrderModule.UI.DeliveryDocumentLineActivity
import com.example.acPolycoaters.ui.home.HomeActivity
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ScanedOrderBatchedItems
import com.example.acPolycoaters.ui.issueForProductionOrder.UI.qrScannerUi.QRScannerActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

class DocumentOrderLineAdapter(
    val context: Context, val documentLineList_gl: ArrayList<DeliveryModel.DocumentLine>, private val chipSave: Chip,
    private val callback: AdapterCallback
) : RecyclerView.Adapter<DocumentOrderLineAdapter.ViewHolder>(), BatchItemsDeliveryAdapter.OnDeleteItemClickListener {

    //todo 19-05-23
    private var connection: Connection? = null
    val REQUEST_CODE = 100
    private lateinit var sessionManagement: SessionManagement
    var hashMap: HashMap<String, ArrayList<ScanedOrderBatchedItems.Value>> = HashMap<String, ArrayList<ScanedOrderBatchedItems.Value>>()
    var quantityHashMap: HashMap<String, ArrayList<String>> = HashMap<String, ArrayList<String>>()
    private var scanedBatchedItemsList_gl: ArrayList<ScanedOrderBatchedItems.Value> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private var pos: Int = 0
    private var itemCode = ""
    var batchItemsAdapter: BatchItemsDeliveryAdapter? = null
    lateinit var networkConnection: NetworkConnection
    lateinit var materialProgressDialog: MaterialProgressDialog
    lateinit var tvOpenQty: TextView
    var remainingOpenQuantity: Double = 0.0
    var width = 0.0
    var U_gsmso = 0.0
    lateinit var tvTotalScanQty: TextView
    lateinit var tvTotalScanGW: TextView
    lateinit var tvNoOfRolls: TextView

    init {
        sessionManagement = SessionManagement(context)
        networkConnection = NetworkConnection()
        materialProgressDialog = MaterialProgressDialog(context)
        //setSqlServer()

    }

    //todo interfaces...
    interface AdapterCallback {
        fun onApiResponse(response: HashMap<String, ArrayList<ScanedOrderBatchedItems.Value>>, quantityResponse: HashMap<String, ArrayList<String>>)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(documentLineList_gl[position]) {
                binding.tvItemCode.text = this.ItemCode
                binding.tvItemName.text = this.ItemDescription
                binding.tvOpenQty.text = this.RemainingOpenQuantity.toString()
                binding.tvWidth.text = this.Factor1.toString()
                binding.tvLength.text = this.Factor2.toString()
                binding.tvGSM.text = this.U_GSMSO.toString()
                tvNoOfRolls = binding.tvNoOfRolls
                binding.trTotalScanFields.visibility = View.VISIBLE


                sessionManagement.setWarehouseCode(context, this.WarehouseCode)

                //todo add adapter size in hashmap at once.
                var count = 0
                for (i in 0 until documentLineList_gl.size) {
                    //TODO set count adapter position size store in list for batch scan...
                    var itemList_gl: ArrayList<ScanedOrderBatchedItems.Value> = ArrayList()
                    if (hashMap.size != documentLineList_gl.size) {
                        hashMap.put("Item" + count, itemList_gl)
                    }

                    //TODO set count adapter position size store in list for batch quantity...
                    var stringList: ArrayList<String> = ArrayList()
                    if (quantityHashMap.size != documentLineList_gl.size) {
                        quantityHashMap.put("Item" + count, stringList)
                    }

                    count++
                }
                Log.e("SCAN_QTY", "quantityHashMap onBindViewHolder => ${quantityHashMap.get("Item$position")}")

                binding.switchMannualBatch.setOnCheckedChangeListener { compoundButton, isChecked ->
                    if (isChecked) {
                        binding.btnMannualBatch.isEnabled = true
                        binding.etMannualBatch.isEnabled = true
                        binding.btnMannualBatch.isClickable = true
                    } else {
                        binding.btnMannualBatch.isEnabled = false
                        binding.etMannualBatch.isEnabled = false
                        binding.btnMannualBatch.isClickable = false
                    }
                }
                //TODO for manual batch entry..
                binding.edBatchCodeScan.setOnEditorActionListener { v, actionId, event ->
                    if (actionId == KeyEvent.ACTION_DOWN && actionId == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND) {
                        var text = binding.edBatchCodeScan.text.toString().trim()
                        if (checkDuplicate(hashMap.get("Item" + position)!!, text)) {
                            recyclerView = binding.rvBatchItems
                            //TODO batch scan api..
                            Log.e("SCAN_QTY", "edBatchCodeScan.setOnEditorActionListener => SCAN QTY: ${binding.tvTotalScannQty.text}, OPEN QTY: ${binding.tvOpenQty.text}")
                            //if (binding.tvTotalScannQty.text.toString() <= binding.tvOpenQty.text.toString()) {
                            scanOrderLinesItem(
                                text, binding.rvBatchItems, adapterPosition, this.ItemCode, binding.tvOpenQty, this.RemainingOpenQuantity,
                                this.Factor1, this.U_GSMSO, binding.tvTotalScannQty, binding.tvTotalScanGw, binding.tvNoOfRolls
                            )
                            /*} else {
                                GlobalMethods.showError(context, "Total scanned quantity can't be greater than open quantity")
                            }*/
                        }
                        binding.edBatchCodeScan.setText("")

                        true

                    } else {
                        false
                    }
                }

                Log.d("scanner_type===>", sessionManagement.getScannerType(context).toString())
                binding.btnMannualBatch.setOnClickListener {
                    var text = binding.edBatchCodeScan.text.toString().trim()
                    recyclerView = binding.rvBatchItems
                    itemCode = this.ItemCode
                    tvOpenQty = binding.tvOpenQty
                    tvTotalScanQty = binding.tvTotalScannQty
                    tvTotalScanGW = binding.tvTotalScanGw
                    tvNoOfRolls = binding.tvNoOfRolls
                    width = this.Factor1
                    U_gsmso = this.U_GSMSO
                    remainingOpenQuantity = this.RemainingOpenQuantity
                    val batchCode = binding.etMannualBatch.text.toString()
                    if (checkDuplicate(hashMap.get("Item" + position)!!, batchCode)) {
                        //todo scan call api here...
                        Log.e("SCAN_QTY", "edBatchCodeScan.setOnKeyListener - if => SCAN QTY: ${binding.tvTotalScannQty.text}, OPEN QTY: ${binding.tvOpenQty.text}")
                        //if (binding.tvTotalScannQty.text.toString() <= binding.tvOpenQty.text.toString()) {
                        scanOrderLinesItem(
                            binding.etMannualBatch.text.toString(), binding.rvBatchItems, adapterPosition, this.ItemCode, binding.tvOpenQty, this.RemainingOpenQuantity,
                            this.Factor1, this.U_GSMSO, binding.tvTotalScannQty, binding.tvTotalScanGw, binding.tvNoOfRolls
                        )
                    }

                }
                //todo if leaser type choose..
                if (sessionManagement.getScannerType(context) == "LEASER") { //sessionManagement.getLeaserCheck()!! == 1 && sessionManagement.getQRScannerCheck()!! == 0
                    binding.ivScanBatchCode.visibility = View.GONE

                    binding.edBatchCodeScan.setOnTouchListener(android.view.View.OnTouchListener { view1: View, motionEvent: MotionEvent? ->
                        binding.edBatchCodeScan.setText("")
                        binding.edBatchCodeScan.setCursorVisible(true)
                        binding.edBatchCodeScan.setFocusableInTouchMode(true)
                        binding.edBatchCodeScan.requestFocus()
                        val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view1.windowToken, 0)
                        true
                    })

                    /*    binding.edBatchCodeScan.setOnFocusChangeListener { view1, hasFocus ->
                             if (hasFocus) {
                                 val imm1 = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                 if (imm1 != null) {
                                     imm1.hideSoftInputFromWindow(view1.windowToken, 0)
                                     binding.edBatchCodeScan.requestFocus()
                                     binding.edBatchCodeScan.setBackgroundResource(R.drawable.gradient_list_div_color)
                                 }
                                 true
                             } else {
                                 return@setOnFocusChangeListener
                             }
                         }*/

                    binding.edBatchCodeScan.setOnKeyListener { view1, keyCode, keyEvent ->
                        if (binding.edBatchCodeScan.isFocused() && binding.edBatchCodeScan.isCursorVisible() && binding.edBatchCodeScan.hasFocus()) {
                            var inputMethodManager = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.hideSoftInputFromWindow(binding.edBatchCodeScan.getWindowToken(), 0)
                        }
                        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                            var text = binding.edBatchCodeScan.text.toString().trim()
                            var x = text
                            if (x.contains(",")) { //todo validation for scan QR code from laser device and get batch code.
                                x = x.split(",")[0]
                                if (checkDuplicate(hashMap.get("Item" + position)!!, x)) {
                                    //todo scan call api here...
                                    Log.e("SCAN_QTY", "edBatchCodeScan.setOnKeyListener - if => SCAN QTY: ${binding.tvTotalScannQty.text}, OPEN QTY: ${binding.tvOpenQty.text}")
                                    //if (binding.tvTotalScannQty.text.toString() <= binding.tvOpenQty.text.toString()) {
                                    scanOrderLinesItem(
                                        x, binding.rvBatchItems, adapterPosition, this.ItemCode, binding.tvOpenQty, this.RemainingOpenQuantity,
                                        this.Factor1, this.U_GSMSO, binding.tvTotalScannQty, binding.tvTotalScanGw, binding.tvNoOfRolls
                                    )

                                    /*} else {
                                        GlobalMethods.showError(context, "Total scanned quantity can't be greater than open quantity")
                                    }*/
                                }
                            } else {
                                if (checkDuplicate(hashMap.get("Item" + position)!!, text)) {
                                    //todo scan call api here...
                                    Log.e("SCAN_QTY", "edBatchCodeScan.setOnKeyListener - else => SCAN QTY: ${binding.tvTotalScannQty.text}, OPEN QTY: ${binding.tvOpenQty.text}")
                                    //if (binding.tvTotalScannQty.text.toString() <= binding.tvOpenQty.text.toString()) {
                                    scanOrderLinesItem(
                                        text, binding.rvBatchItems, adapterPosition, this.ItemCode, binding.tvOpenQty, this.RemainingOpenQuantity,
                                        this.Factor1, this.U_GSMSO, binding.tvTotalScannQty, binding.tvTotalScanGw, binding.tvNoOfRolls
                                    )
                                    /*} else {
                                        GlobalMethods.showError(context, "Total scanned quantity can't be greater than open quantity")
                                    }*/
                                }
                            }
                            binding.edBatchCodeScan.setText("")
                            binding.edBatchCodeScan.requestFocus()
                        }
                        return@setOnKeyListener true
                    }

                }

                //todo is qr scanner type choose..
                else if (sessionManagement.getScannerType(context) == "QR_SCANNER" || sessionManagement.getScannerType(context) == null) { //|| sessionManagement.getLeaserCheck()!! == 0 && sessionManagement.getQRScannerCheck()!! == 1 || sessionManagement.getLeaserCheck()!! == 0 && sessionManagement.getQRScannerCheck()!! == 0
                    binding.ivScanBatchCode.visibility = View.VISIBLE

                    //TODO click on barcode scanner for popup..
                    binding.ivScanBatchCode.setOnClickListener {
                        var text = binding.edBatchCodeScan.text.toString().trim()
                        recyclerView = binding.rvBatchItems
                        itemCode = this.ItemCode
                        tvOpenQty = binding.tvOpenQty
                        tvTotalScanQty = binding.tvTotalScannQty
                        tvTotalScanGW = binding.tvTotalScanGw
                        tvNoOfRolls = binding.tvNoOfRolls
                        width = this.Factor1
                        U_gsmso = this.U_GSMSO
                        remainingOpenQuantity = this.RemainingOpenQuantity
                        if (sessionManagement.getScannerType(context) == null) {
                            showPopupNotChooseScanner()
                        } else if (sessionManagement.getScannerType(context) == "QR_SCANNER") {
                            val intent = Intent(context, QRScannerActivity::class.java)
                            pos = adapterPosition
                            (context as DeliveryDocumentLineActivity).startActivityForResult(intent, REQUEST_CODE)
                        }/* else if (sessionManagement.getLeaserCheck()!! == 1 && sessionManagement.getQRScannerCheck()!! == 0) {
                        //TODO laser popup
                         laserScannerPopupDialog(binding.rvBatchItems, adapterPosition, this.ItemCode, binding.tvOpenQty)
                        } */
                    }

                    //todo for manual batch entry..
                    binding.edBatchCodeScan.setOnEditorActionListener { v, actionId, event ->
                        //event.action == KeyEvent.ACTION_DOWN && actionId == KeyEvent.KEYCODE_ENTER ||
                        if (actionId == KeyEvent.ACTION_DOWN && actionId == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND) {
                            var text = binding.edBatchCodeScan.text.toString().trim()
                            if (checkDuplicate(hashMap.get("Item" + position)!!, text)) {
                                //todo scan call api here...
                                Log.e("SCAN_QTY", "edBatchCodeScan.setOnEditorActionListener=> SCAN QTY: ${binding.tvTotalScannQty.text}, OPEN QTY: ${binding.tvOpenQty.text}")
                                //if (binding.tvTotalScannQty.text.toString() <= binding.tvOpenQty.text.toString()) {
                                scanOrderLinesItem(
                                    text,
                                    binding.rvBatchItems,
                                    adapterPosition,
                                    this.ItemCode,
                                    binding.tvOpenQty,
                                    RemainingOpenQuantity,
                                    this.Factor1,
                                    this.U_GSMSO,
                                    binding.tvTotalScannQty,
                                    binding.tvTotalScanGw, binding.tvNoOfRolls
                                )
                                /*} else {
                                    GlobalMethods.showError(context, "Total scanned quantity can't be greater than open quantity")
                                }*/
                            }
                            binding.edBatchCodeScan.setText("")
                            true

                        } else {
                            false
                        }
                    }
                }


                //TODO save order lines listener by interface...
                chipSave.setOnClickListener {
                    callback.onApiResponse(hashMap, quantityHashMap)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return documentLineList_gl.size
    }

    //todo viewholder...
    class ViewHolder(val binding: DeliveryOrderLinesAdapterLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    //TODO scan item lines api here....
    private fun scanOrderLinesItem(
        text: String, rvBatchItems: RecyclerView, position: Int, itemCode: String?, tvOpenQty: TextView,
        remainingOpenQuantity: Double, factor1: Double, uGsm: Double, tvTotalScannQty: TextView, tvTotalScanGw: TextView, tvNoOfRolls: TextView
    ) {
        if (networkConnection.getConnectivityStatusBoolean(context)) {
            materialProgressDialog.show()
            val networkClient = NetworkClients.create(context)
            networkClient.doGetBatchNumScanDetails("Batch eq '" + text + "'" + " and ItemCode eq '" + itemCode + "'")
                .apply {
                    enqueue(object : Callback<ScanedOrderBatchedItems> {
                        override fun onResponse(
                            call: Call<ScanedOrderBatchedItems>,
                            response: Response<ScanedOrderBatchedItems>
                        ) {
                            try {
                                materialProgressDialog.dismiss()
                                if (response.isSuccessful) {

                                    var responseModel = response.body()!!
                                    Log.e("SCAN_QTY", "doGetBatchNumScanDetails => $responseModel")
                                    if (responseModel.value.size > 0 && !responseModel.value.isNullOrEmpty()) {

                                        //todo validation for line width and batch width----
                                        //if (factor1 == responseModel.value[0].U_Width || factor1 > responseModel.value[0].U_Width) {
                                        // if (uGsm == responseModel.value[0].U_GSM) {
                                        var modelResponse = responseModel.value
                                        scanedBatchedItemsList_gl.addAll(modelResponse)

                                        var itemList_gl: ArrayList<ScanedOrderBatchedItems.Value> = ArrayList()
                                        itemList_gl.addAll(hashMap.get("Item" + position)!!)
                                        Log.e("SCAN_QTY", "itemList_gl after add hashMap => ${itemList_gl.size}")
                                        var stringList: ArrayList<String> = ArrayList()
                                        stringList.addAll(quantityHashMap.get("Item" + position)!!)

                                        itemList_gl.add(responseModel.value[0])
                                        Log.e(
                                            "SCAN_QTY",
                                            "itemList_gl after add hashMap n doGetBatchNumScanDetails response => ${itemList_gl.size}\nResponse => ${toSimpleJson(responseModel.value)}"
                                        )
                                        if (!itemList_gl.isNullOrEmpty()) {

                                            Log.e("list_size-----", itemList_gl.size.toString())

                                            //todo quantity..
                                            getQuantityFromApi(
                                                text,
                                                itemList_gl[0].ItemCode,
                                                position,
                                                stringList,
                                                tvOpenQty,
                                                tvTotalScannQty,
                                                tvTotalScanGw,
                                                rvBatchItems,
                                                itemList_gl, tvNoOfRolls
                                            )
                                        }

                                        /*} else {
                                            GlobalMethods.showError(context, "GSM must be same of SO.")
                                        }*/

                                        /*} else {
                                            GlobalMethods.showError(context, "Invalid QR Code / Width not matched.")
                                        }*/
                                    } else {
                                        GlobalMethods.showError(context, "Invalid Batch Code, Not Found Data")
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
                                            GlobalMethods.showError(context, mError.error.message.value)
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(context, mError.error.message.value)
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
            Toast.makeText(context, "No Network Connection", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DeliveryOrderLinesAdapterLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    //TODO scan item lines api here....
    private fun getQuantityFromApi(
        batchCode: String,
        itemCode: String,
        position: Int,
        stringList: ArrayList<String>,
        tvOpenQty: TextView,
        tvTotalScannQty: TextView,
        tvTotalScanGw: TextView,
        rvBatchItems: RecyclerView,
        itemList_gl: ArrayList<ScanedOrderBatchedItems.Value>,
        tvNoOfRolls: TextView
    ) {
        if (networkConnection.getConnectivityStatusBoolean(context)) {
            materialProgressDialog.show()
            var apiConfig = ApiConstantForURL()
            QuantityNetworkClient.updateBaseUrlFromConfig(apiConfig, true)
            val networkClient = QuantityNetworkClient.create(context)
            networkClient.getQuantityValue(sessionManagement.getCompanyDB(context)!!, batchCode, itemCode, sessionManagement.getWarehouseCode(context)!!)
                .apply {
                    enqueue(object : Callback<GetQuantityModel> {
                        override fun onResponse(call: Call<GetQuantityModel>, response: Response<GetQuantityModel>) {
                            try {
                                materialProgressDialog.dismiss()
                                if (response.isSuccessful) {
                                    Log.e("response---------", response.body().toString())

                                    var responseModel = response.body()!!
                                    if (responseModel.value.size > 0 && !responseModel.value.isNullOrEmpty()) {

                                        Log.e(
                                            "SCAN_QTY",
                                            "Success => ${responseModel.value}\nquantityHashMap before add => $quantityHashMap\nitemList_gl size(${itemList_gl.size}) => ${
                                                toSimpleJson(itemList_gl)
                                            }"
                                        )
                                        /*val quantity = responseModel.value[0].Quantity.toDoubleOrNull() ?: 0.0
                                        if (quantity > 0.0) {
                                            stringList.add(responseModel.value[0].Quantity)
                                        }
//                                        stringList.add(responseModel.value[0].Quantity)
                                        quantityHashMap.put("Item" + position, stringList)

                                        Log.e("SCAN_QTY", "quantityHashMap after add => $quantityHashMap")
                                        val scannedList = quantityHashMap["Item$position"]
                                        val scannedQty = GlobalMethods.sumBatchQuantity(position, scannedList!!)
                                        val openQty = tvOpenQty.text.toString().toDouble()

                                        Log.e("SCAN_QTY", "getQuantityValue => SCAN QTY: $scannedQty, OPEN QTY: $openQty")

                                        if (scannedQty == 0.0 || scannedQty > openQty) {
                                            when {
                                                scannedQty == 0.0 -> {
                                                    GlobalMethods.showError(context, "Batch / Roll No. has zero Quantity of this SO.")
                                                    Log.e("SCAN_QTY", "getQuantityValue => Batch / Roll No. has zero Quantity of this SO.")
                                                }

                                                scannedQty > openQty -> {
                                                    // ✅ Remove last added quantity
                                                    if (scannedList.isNotEmpty()) {
                                                        scannedList.removeLast()
                                                        quantityHashMap["Item$position"] = scannedList

                                                        // Optionally update UI or adapter
                                                        batchItemsAdapter?.notifyDataSetChanged()
                                                    }

                                                    GlobalMethods.showError(context, "Total scanned quantity can't be greater than open quantity.")
                                                    Log.e("SCAN_QTY", "getQuantityValue => scannedQty > openQty: Removed last scanned value.")
                                                }
                                            }
                                            return
                                        }

                                        // ✅ Only set scanned qty if it’s valid
                                        tvTotalScannQty.text = scannedQty.toString()*/

                                        val quantityStr = responseModel.value[0].Quantity
                                        val quantity = quantityStr.toDoubleOrNull() ?: 0.0

                                        if (quantity <= 0.0) {
                                            GlobalMethods.showError(context, "Batch / Roll No. has zero Quantity of this SO.")
                                            Log.e("SCAN_QTY", "getQuantityValue => Batch / Roll No. has zero Quantity of this SO.")
                                            return
                                        }

                                        // Get current scanned list or create new one
                                        val stringList = quantityHashMap.getOrPut("Item$position") { arrayListOf() }

                                        // Clone list and add new quantity for validation
                                        val tempList = ArrayList(stringList)
                                        tempList.add(quantityStr)

                                        // Calculate new scanned total
                                        val scannedQty = GlobalMethods.sumBatchQuantity(position, tempList)
                                        val openQty = tvOpenQty.text.toString().toDouble()

                                        Log.e("SCAN_QTY", "getQuantityValue => SCAN QTY: $scannedQty, OPEN QTY: $openQty")

                                        /*if (scannedQty > openQty) {
                                            GlobalMethods.showError(context, "Total scanned quantity can't be greater than open quantity.")
                                            Log.e("SCAN_QTY", "getQuantityValue => scannedQty > openQty: Rejecting this scan.")
                                            return
                                        }*/

                                        // ✅ All checks passed, now commit the value
                                        stringList.add(quantityStr)
                                        quantityHashMap["Item$position"] = stringList

                                        Log.e("SCAN_QTY", "quantityHashMap after add => $quantityHashMap")

                                        // Update UI
                                        tvTotalScannQty.text = scannedQty.toString()


                                        if (quantityHashMap["Item$position"]?.isNotEmpty() == true) {
                                            val hasValidQty = quantityHashMap["Item$position"]?.any { it.toDoubleOrNull() ?: 0.0 > 0.0 } == true

                                            if (hasValidQty) {
                                                hashMap["Item$position"] = itemList_gl
                                                tvNoOfRolls.text = hashMap["Item$position"]?.size.toString()
                                                val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
                                                rvBatchItems.layoutManager = layoutManager
                                                batchItemsAdapter = BatchItemsDeliveryAdapter(
                                                    position,
                                                    context,
                                                    hashMap["Item$position"]!!,
                                                    quantityHashMap["Item$position"]!!,
                                                    "SalesOrder",
                                                    tvTotalScannQty,
                                                    rvBatchItems, tvNoOfRolls
                                                )

                                                Log.e("SCAN_QTY", "quantityHashMap in hasValidQty => $quantityHashMap")

                                                batchItemsAdapter?.setOnDeleteItemClickListener(this@DocumentOrderLineAdapter)
                                                rvBatchItems.adapter = batchItemsAdapter

                                                batchItemsAdapter?.notifyDataSetChanged()

                                                val totalGrossWeight = GlobalMethods.changeDecimal(
                                                    GlobalMethods.sumBatchGrossWeight(position, hashMap["Item$position"]!!).toString()
                                                )
                                                tvNoOfRolls.text = hashMap["Item$position"]?.size.toString()
                                                tvTotalScanGw.text = totalGrossWeight

                                            } else {
                                                batchItemsAdapter?.notifyDataSetChanged()
                                                GlobalMethods.showError(context, "Batch / Roll No. has zero Quantity of this SO.")
//                                                Log.e("SCAN_QTY", "No valid quantity (>0.0) found in quantityHashMap.")
                                            }
                                        } else {
                                            batchItemsAdapter?.notifyDataSetChanged()
                                            GlobalMethods.showError(context, "No Quantity Found of this SO.")
                                            Log.e("SCAN_QTY", "quantityHashMap['Item$position'] is empty or null.")
                                        }

                                        /*if (quantityHashMap.get("Item" + position)!!.size > 0) {
                                            if (!quantityHashMap.get("Item" + position)!!.contains("0.000000")) {
                                                hashMap.put("Item" + position, itemList_gl)

                                                val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
                                                rvBatchItems.layoutManager = layoutManager
                                                batchItemsAdapter = BatchItemsDeliveryAdapter(context, hashMap.get("Item" + position)!!, quantityHashMap.get("Item" + position)!!, "SalesOrder",tvTotalScannQty)
                                                //todo call setOnItemListener Interface Function...
                                                batchItemsAdapter?.setOnDeleteItemClickListener(this@DocumentOrderLineAdapter)
                                                rvBatchItems.adapter = batchItemsAdapter

                                                var totalGrossWeight = GlobalMethods.changeDecimal(GlobalMethods.sumBatchGrossWeight(position, hashMap.get("Item" + position)!!).toString())
                                                tvTotalScanGw.text = totalGrossWeight


                                            } else {
                                                batchItemsAdapter?.notifyDataSetChanged()
                                                GlobalMethods.showError(context, "Batch / Roll No. has zero Quantity of this SO.")
                                                Log.e("SCAN_QTY", "getQuantityValue else (!quantityHashMap.contains(0)) => Batch / Roll No. has zero Quantity of this SO.")
                                            }

                                        } else {
                                            batchItemsAdapter?.notifyDataSetChanged()
                                            GlobalMethods.showError(context, "No Quantity Found of this SO.")
                                            Log.e("SCAN_QTY", "getQuantityValue else (quantityHashMap.size==0) => No Quantity Found of this SO.")
                                        }*/


                                    } else {
                                        GlobalMethods.showError(context, "No Quantity")
                                        Log.e("SCAN_QTY", "getQuantityValue else responseModel.value.size==0 => No Quantity")
                                    }

                                } else {
                                    materialProgressDialog.dismiss()
                                    val gson1 = GsonBuilder().create()
                                    var mError: OtpErrorModel
                                    try {
                                        val s = response.errorBody()!!.string()
                                        mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                        if (mError.error.code.equals(400)) {
                                            GlobalMethods.showError(context, mError.error.message.value)
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(context, mError.error.message.value)
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

                        override fun onFailure(call: Call<GetQuantityModel>, t: Throwable) {
                            Log.e("scanItemApiFailed-----", t.toString())
                            materialProgressDialog.dismiss()
                        }

                    })
                }
        } else {
            materialProgressDialog.dismiss()
            Toast.makeText(context, "No Network Connection", Toast.LENGTH_SHORT).show()
        }
    }


    //TODO get quantity for batch code...
    private fun getQuanity(
        batchCode: String,
        itemCode: String,
        position: Int,
        stringList: ArrayList<String>,
        tvOpenQty: TextView,
        remainingOpenQuantity: Double,
        tvTotalScannQty: TextView
    ) {
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                Log.e(
                    "TAG=====>",
                    "getQuanity: " + "SELECT T0.[Quantity] FROM OBTQ T0  INNER JOIN OBTN T1 ON T0.[SysNumber] = T1.[SysNumber] and T0.ItemCode=T1.ItemCode WHERE T1.[DistNumber]   =  '$batchCode' AND  T1.[ItemCode] = '$itemCode' AND T0.[WhsCode] ='${
                        sessionManagement.getWarehouseCode(context)
                    }'"
                )
                val resultSet = statement.executeQuery(
                    "SELECT T0.[Quantity] FROM OBTQ T0  INNER JOIN OBTN T1 ON T0.[SysNumber] = T1.[SysNumber] and T0.ItemCode=T1.ItemCode WHERE T1.[DistNumber]   =  '$batchCode' AND  T1.[ItemCode] = '$itemCode' AND T0.[WhsCode] ='${
                        sessionManagement.getWarehouseCode(context)
                    }'"
                )
                while (resultSet.next()) {
                    Log.e("ConStatus", "Success=>" + resultSet.getString(1))
                    //todo remove zero digits from quantity...
                    stringList.add(GlobalMethods.changeDecimal(resultSet.getString(1))!!)
                    Log.e("stringList", "Success=>" + stringList)
                    quantityHashMap.put("Item" + position, stringList)
                    //TODO sum of quantity of batches..
                    tvOpenQty.text = "  : " + remainingOpenQuantity.toString()
                    tvTotalScannQty.text = GlobalMethods.sumBatchQuantity(position, quantityHashMap.get("Item" + position)!!).toString()
//                    tvOpenQty.text = GlobalMethods.sumBatchQuantity(position, quantityHashMap.get("Item" + position)!!).toString() //todo comment on 30-06

                }

            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.e("Result=>", "Connection is null")
        }
    }


    override fun onDeleteItemClick(
        parentPosition: Int,
        scanList: ArrayList<ScanedOrderBatchedItems.Value>,
        quantityHashMap1: ArrayList<String>,
        childPosition: Int,
        tvTotalScannQty: TextView,
        rvBatchItems: RecyclerView,
        tvNoOfRolls: TextView
    ) {
        if (childPosition < 0 || childPosition >= scanList.size || childPosition >= quantityHashMap1.size) {
            Log.e("SCAN_QTY", "Invalid position: $childPosition")
            return
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("Confirm...")
            .setMessage("Do you want to delete " + scanList[childPosition].Batch + " Batch Item .")
            .setIcon(R.drawable.ic_trash)
            .setPositiveButton("Confirm",
                DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i1: Int ->
                    /*Log.e("SCAN_QTY", "onDeleteItemClick() : before scan List size => ${scanList.size},  before qtyHashMap size => ${quantityHashMap1.size}")
                    Log.e("SCAN_QTY", "onDeleteItemClick() : Parent Position: $parentPosition,  Child Position: $pos")
                    Log.i("SCAN_QTY", "onDeleteItemClick() : before scan List => ${toSimpleJson(scanList)}\nbefore qtyHashMap => ${toSimpleJson(quantityHashMap1)}")
                    scanList.removeAt(pos)
                    quantityHashMap1.removeAt(pos)
                    batchItemsAdapter?.notifyDataSetChanged()
                    //batchItemsAdapter?.notifyItemRemoved(pos)

                    val totalQty = quantityHashMap1.sumOf { it.toDoubleOrNull() ?: 0.0 }
                    tvTotalScannQty.text = totalQty.toString()
                    Log.e("SCAN_QTY", "onDeleteItemClick() : after scan List size => ${scanList.size},  after qtyHashMap size => ${quantityHashMap1.size}")
                    Log.e("SCAN_QTY", "onDeleteItemClick() : Parent Position: $parentPosition,  Child Position: $pos")

                    Log.i("SCAN_QTY", "onDeleteItemClick() : after scan List => ${toSimpleJson(scanList)}\nqtyHashMap list => ${toSimpleJson(quantityHashMap1)}")*/
                    Log.d("DELETE_DEBUG", "onDeleteItemClick called: parent=$parentPosition, child=$childPosition")

                    val scannedItemsForParent = hashMap["Item$parentPosition"]
                    val scannedQuantitiesForParent = quantityHashMap["Item$parentPosition"]

                    if (scannedItemsForParent != null && scannedQuantitiesForParent != null) {
                        if (childPosition < scannedItemsForParent.size) {
                            val removedItem = scannedItemsForParent.removeAt(childPosition)
                            Log.d("DELETE_DEBUG", "Removed item: ${removedItem.Batch}, from parent $parentPosition")
                        }
                        if (childPosition < scannedQuantitiesForParent.size) {
                            val removedQty = scannedQuantitiesForParent.removeAt(childPosition)
                            Log.d("DELETE_DEBUG", "Removed qty: $removedQty, from parent $parentPosition")
                        }

                        // Update the HashMap with the modified lists (important!)
                        hashMap["Item$parentPosition"] = scannedItemsForParent
                        quantityHashMap["Item$parentPosition"] = scannedQuantitiesForParent

                        // 1. Update the child adapter's data directly if it uses a direct reference:
                        // You might need to cast rvBatchItems.adapter back to BatchItemsDeliveryAdapter
                        val currentChildAdapter = rvBatchItems.adapter as? BatchItemsDeliveryAdapter
                        currentChildAdapter?.apply {
                            // Update its internal data source if it's separate from the hashMaps
                            // For example, if BatchItemsDeliveryAdapter has its own list, update it here.
                            // If it directly uses the hashMaps, then it just needs to be notified.
                            notifyItemRemoved(childPosition)
                            // notifyItemRangeChanged is often good practice if items shift up
                            notifyItemRangeChanged(childPosition, scannedItemsForParent.size - childPosition)
                        }
                        Log.d("DELETE_DEBUG", "Child adapter notified for removal at $childPosition for parent $parentPosition")


                        // 2. Recalculate and update the parent's total scanned quantity and gross weight.
                        // You have direct access to tvTotalScannQty and tvTotalScanGw here!
                        val updatedScannedQty = GlobalMethods.sumBatchQuantity(parentPosition, scannedQuantitiesForParent)
                        tvTotalScannQty.text = updatedScannedQty.toString()
                        tvNoOfRolls.text = hashMap["Item$parentPosition"]?.size.toString()

                        Log.d("DELETE_DEBUG", "Parent totals updated: Qty=${tvTotalScannQty.text}")

                        // No need to call notifyItemChanged(parentPosition) on the parent adapter if you're
                        // directly updating the TextViews here and the child RecyclerView is updated.
                        // If there are other UI elements of the parent row that depend on the data
                        // and are NOT updated directly here, then you might still need notifyItemChanged.
                        // For now, let's remove it to avoid potential double updates/flickering.
                        // notifyItemChanged(parentPosition) // Consider removing this if direct updates suffice
                    }

                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
            .show()
    }


    //TODO duplicatcy checking from list...
    fun checkDuplicate(scanedBatchedItemsList_gl: ArrayList<ScanedOrderBatchedItems.Value>, batchCode: String): Boolean {
        var status: Boolean = true;
        for (items in scanedBatchedItemsList_gl) {
            if (items.Batch.equals(batchCode)) {
                status = false
                Toast.makeText(context, "Duplicate Roll No. / Batch no. Already Exists!", Toast.LENGTH_SHORT).show()
            }
        }
        return status
    }


    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringExtra("batch_code")
            Log.i("SCAN_QTY", "Scan Data: $result")
            //todo spilt string and get string at 0 index...
            if (checkDuplicate(hashMap.get("Item" + pos)!!, result.toString().split(",")[0])) {
                Log.e("SCAN_QTY", "onActivityResult => SCAN QTY: ${tvTotalScanQty.text}, OPEN QTY: ${tvOpenQty.text}")
                //if (tvTotalScanQty.text.toString().toDouble() <= tvOpenQty.text.toString().toDouble()) {
                scanOrderLinesItem(
                    result.toString().split(",")[0],
                    recyclerView,
                    pos,
                    itemCode,
                    tvOpenQty,
                    remainingOpenQuantity,
                    width,
                    U_gsmso,
                    tvTotalScanQty,
                    tvTotalScanGW,
                    tvNoOfRolls
                )
                /*} else {
                    Log.e("SCAN_QTY", "onActivityResult else => Total scanned quantity can't be greater than open quantity")
                    GlobalMethods.showError(context, "Total scanned quantity can't be greater than open quantity")
                }*/
            }

        }
    }

    //TODO set sql server for query...
    private fun setSqlServer() {
        val url = "jdbc:jtds:sqlserver://" + AppConstants.IP + ":" + AppConstants.PORT + "/" + sessionManagement.getCompanyDB(context)!!
        ActivityCompat.requestPermissions(context as Activity, arrayOf<String>(Manifest.permission.INTERNET), PackageManager.PERMISSION_GRANTED)
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

    //TODO POPUP DIALOG...

    //todo show popup when not selected scanner type button click popup.
    private fun showPopupNotChooseScanner() {
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
        val view = LayoutInflater.from(context).inflate(R.layout.custom_popup_alert, null)
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
            var intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
            builder.dismiss()
            notifyDataSetChanged()
        }

        builder.setCancelable(true)
        builder.show()
    }
}