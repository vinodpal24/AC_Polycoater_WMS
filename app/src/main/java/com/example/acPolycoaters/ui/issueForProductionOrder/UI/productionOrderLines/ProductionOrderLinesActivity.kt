package com.example.acPolycoaters.ui.issueForProductionOrder.UI.productionOrderLines

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acPolycoaters.Global_Classes.AppConstants
import com.example.acPolycoaters.ui.issueForProductionOrder.Adapter.ProductionOrderLinesAdapter
import com.example.acPolycoaters.Global_Classes.GlobalMethods
import com.example.acPolycoaters.Global_Classes.GlobalMethods.toSimpleJson
import com.example.acPolycoaters.Global_Classes.MaterialProgressDialog
import com.example.acPolycoaters.Global_Notification.NetworkConnection
import com.example.acPolycoaters.Model.*
import com.example.acPolycoaters.Retrofit_Api.ApiConstantForURL
import com.example.acPolycoaters.Retrofit_Api.NetworkClients
import com.example.acPolycoaters.Retrofit_Api.QuantityNetworkClient
import com.example.acPolycoaters.SessionManagement.SessionManagement
import com.example.acPolycoaters.databinding.ActivityProductionOrderLinesBinding
import com.example.acPolycoaters.interfaces.PassList
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.InventoryGenExitsModel
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ProductionListModel
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ScanedOrderBatchedItems
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.WarehouseBPL_IDModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "ProductionOrderLinesAct"

class ProductionOrderLinesActivity : AppCompatActivity(), PassList,
    ProductionOrderLinesAdapter.AdapterCallback {

    private lateinit var activityFormBinding: ActivityProductionOrderLinesBinding
    private lateinit var productionOrderLinesAdapter: ProductionOrderLinesAdapter
    private lateinit var productionOrderLineList_gl: ArrayList<ProductionListModel.ProductionOrderLine>

    //    private lateinit var productionOrderValueList_gl: ArrayList<ProductionListModel.Value>
    private lateinit var productionOrderValueList_gl: ProductionListModel.Value
    var position: Int? = 0
    lateinit var networkConnection: NetworkConnection
    lateinit var materialProgressDialog: MaterialProgressDialog
    private lateinit var sessionManagement: SessionManagement
    private var BPLIDNum = ""
    private var valueList: List<ProductionListModel.Value> = ArrayList()
    private var connection: Connection? = null
    private var series: String = ""
    var openQty = 0.0

    //todo batch scan and quantity list interface override...
    var hashMapBatchList: HashMap<String, ArrayList<ScanedOrderBatchedItems.Value>> =
        HashMap<String, ArrayList<ScanedOrderBatchedItems.Value>>()
    var hashmapBatchQuantityList: HashMap<String, ArrayList<String>> =
        HashMap<String, ArrayList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFormBinding = ActivityProductionOrderLinesBinding.inflate(layoutInflater)
        setContentView(activityFormBinding.root)
        title = "Form Screen"


        //todo initialization...
        networkConnection = NetworkConnection()
        materialProgressDialog = MaterialProgressDialog(this@ProductionOrderLinesActivity)
        sessionManagement = SessionManagement(this@ProductionOrderLinesActivity)


        deleteCache(this)

        supportActionBar?.hide()

        /*   val myTask = BolicyTask()
           myTask.execute()*/

        /*  if (checkPermission(Manifest.permission.INTERNET)) {
              // You already have permission, do something with the internet
              configureStrictMode()
          } else {
              // You don't have permission, request it
              requestInternetPermission()
          }*/

        /*val delayMillis = 1000 // 1 second

        handler.postDelayed({
            setSqlServer()
        }, delayMillis.toLong())*/


        //todo get arguments data...
        try {
            val intent = intent
            productionOrderLineList_gl = intent.getSerializableExtra("productionLinesList") as ArrayList<ProductionListModel.ProductionOrderLine>
            productionOrderValueList_gl =
                intent.getSerializableExtra("productionValueList") as ProductionListModel.Value //todo getting list selected item values and lines only not all size data..
            position = intent.extras?.getInt("pos")

            valueList = listOf(productionOrderValueList_gl)

            Log.w("PO_LIST", "productionOrderLineList_gl in POLinesActivity: ${toSimpleJson(productionOrderLineList_gl)}")

        } catch (e: IOException) {
            Log.e(TAG, "onCreate:===> " + e.message)
            e.printStackTrace()
        }

//        getWarehouseCode = intent.extras?.getString("warehouseCode")!!


        //todo calling BPLID here...
        //getBPL_IDNumber()
        setAdapter()
        BPLIDNum = if (Prefs.getString(AppConstants.BPLID, "").isNotEmpty()) Prefs.getString(AppConstants.BPLID, "") else ""
        getDocSeriesApi(BPLIDNum)

        activityFormBinding.tvItemNo.text = productionOrderValueList_gl.ItemNo


        activityFormBinding.ivOnback.setOnClickListener {
            onBackPressed()
        }


        //todo cancel lines...
        activityFormBinding.chipCancel.setOnClickListener {
            onBackPressed()
        }


    }

    var tempList: ArrayList<ProductionListModel.ProductionOrderLine> = ArrayList()

    //todo set adapter....
    fun setAdapter() {
        //todo removing order line if BaseQuantity value in negative.... and also this way is removing IndexOutOfBoundException from list....
        productionOrderLineList_gl.forEach { line ->
            val plannedQty = line.PlannedQuantity
            val issuedQty = line.IssuedQuantity
            val openQty = plannedQty - issuedQty

            // Apply condition if needed
            // if (line.BaseQuantity > 0.0 && openQty > 0.0) {
            tempList.add(line)
            // }
        }
        Log.i("PO_LIST", "PO List temp: ${tempList}")


        var width = productionOrderValueList_gl.U_Width
        var length = productionOrderValueList_gl.U_Length
        var gsm = productionOrderValueList_gl.U_GSM

        if (tempList.size > 0) {
            activityFormBinding.ivNoDataFound.visibility = View.GONE
            activityFormBinding.rvProductionOrderList.visibility = View.VISIBLE
            activityFormBinding.btnLinearLayout.visibility = View.VISIBLE

            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
            activityFormBinding.rvProductionOrderList.layoutManager = layoutManager
            //todo parse save button in adapter constructor for click listener on adapter...
            productionOrderLinesAdapter = ProductionOrderLinesAdapter(
                this@ProductionOrderLinesActivity,
                tempList,
                networkConnection,
                materialProgressDialog,
                this@ProductionOrderLinesActivity,
                activityFormBinding.chipSave,
                width,
                length,
                gsm
            )//getWarehouseCode
            activityFormBinding.rvProductionOrderList.adapter = productionOrderLinesAdapter
        } else {
            activityFormBinding.ivNoDataFound.visibility = View.VISIBLE
            activityFormBinding.rvProductionOrderList.visibility = View.GONE
            activityFormBinding.btnLinearLayout.visibility = View.GONE
        }


    }

    //todo getting batch order lines and quantity data from adapter to activity...
    override fun onApiResponse(
        response: HashMap<String, ArrayList<ScanedOrderBatchedItems.Value>>,
        quantityResponse: HashMap<String, ArrayList<String>>
    ) {
        /*for ((k, v) in response) {
            Log.e("adpResponse---->", k + "-->" + v.toString())
            hashMapBatchList.put(k, v)
        }*/
        hashMapBatchList = response
        hashmapBatchQuantityList = quantityResponse
        Log.e("hashmap--->", hashMapBatchList.toString())
        Log.e("batchQuantityList-->", hashmapBatchQuantityList.toString())
        saveProductionOrderLinesItems()

    }

    lateinit var batchList: List<ScanedOrderBatchedItems.Value>
    lateinit var batchQuantityList: ArrayList<String>

    //todo here save issue for production lines items of order...
    private fun saveProductionOrderLinesItems() {
        var comments = valueList[0].Remarks.toString()
        var docDate = valueList[0].PostingDate
        var absoluteEntry = valueList[0].AbsoluteEntry

        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            materialProgressDialog.show()

            var postedJson: JsonObject = JsonObject()
            postedJson.addProperty("BPL_IDAssignedToInvoice", BPLIDNum)
            postedJson.addProperty("Comments", comments)
            postedJson.addProperty("DocDate", docDate) //todo current date will send here---
            postedJson.addProperty("Series", series)

            val DocumentLinesArray = JsonArray()

            for (i in tempList.indices) {
                //TODO sum of order line batch quantities and compare with line quantity..
                var quantity = 0.000
                quantity =
                    GlobalMethods.sumBatchQuantity(i, hashmapBatchQuantityList.get("Item" + i)!!)


                val jsonObject = JsonObject()
                jsonObject.addProperty("BaseEntry", absoluteEntry)
                jsonObject.addProperty("BaseLine", tempList[i].LineNumber)
                jsonObject.addProperty("BaseType", "202")
                jsonObject.addProperty("Quantity", quantity)
                jsonObject.addProperty("WarehouseCode", sessionManagement.getWarehouseCode(this))


                var u_width = 0.0
                var u_length = 0.0
                var u_gsm = 0.0
                var BatchNumbersArray = JsonArray()

                batchList = hashMapBatchList.get("Item" + i)!!
                batchQuantityList = hashmapBatchQuantityList.get("Item" + i)!!
                for (i in batchList.indices) {
                    for (j in i until batchQuantityList.size) {
                        var jsonLinesObject = JsonObject()
                        u_width = batchList[i].U_Width
                        u_length = batchList[i].U_Length
                        u_gsm = batchList[i].U_GSM

                        jsonLinesObject.addProperty("BatchNumber", batchList[i].Batch)
                        jsonLinesObject.addProperty("SystemSerialNumber", batchList[i].SystemNumber)
                        jsonLinesObject.addProperty("Quantity", batchQuantityList[j]) //batchQuantityList[j])

                        BatchNumbersArray.add(jsonLinesObject)
                        break
                    }

                }

                jsonObject.addProperty("Factor1", u_width)
                jsonObject.addProperty("Factor2", u_length)
                jsonObject.addProperty("Factor3", 1.0) //todo batchList[0].U_RG.toDouble()--> change by SAP factor3 value always send 1 according to batch count..
                jsonObject.addProperty("Factor4", u_gsm)

                jsonObject.add("BatchNumbers", BatchNumbersArray)
                if (batchList.size > 0)
                    DocumentLinesArray.add(jsonObject)
            }

            postedJson.add("DocumentLines", DocumentLinesArray)
            Log.e("ISSUE_PRODUCTION", "FINAL POST JSON: ${postedJson}")
            val networkClient = NetworkClients.create(this@ProductionOrderLinesActivity)
            networkClient.doGetInventoryGenExits(postedJson).apply {
                enqueue(object : Callback<InventoryGenExitsModel> {
                    override fun onResponse(
                        call: Call<InventoryGenExitsModel>,
                        response: Response<InventoryGenExitsModel>
                    ) {
                        try {
                            materialProgressDialog.dismiss()
                            if (response.isSuccessful) {
                                if (response.code() == 201) {
                                    Log.e("success------", "Successful!")
                                    GlobalMethods.showSuccess(
                                        this@ProductionOrderLinesActivity,
                                        "Issue Production Order Post Successfully."
                                    )
                                }
                                onBackPressed()
                            } else {
                                materialProgressDialog.dismiss()
                                val gson1 = GsonBuilder().create()
                                var mError: OtpErrorModel
                                try {
                                    val s = response.errorBody()!!.string()
                                    mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                    if (mError.error.code.equals(400)) {
                                        GlobalMethods.showError(
                                            this@ProductionOrderLinesActivity,
                                            mError.error.message.value
                                        )
                                    }
                                    if (mError.error.message.value != null) {
                                        GlobalMethods.showError(
                                            this@ProductionOrderLinesActivity,
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

                    override fun onFailure(call: Call<InventoryGenExitsModel>, t: Throwable) {
                        Log.e("orderLines_failure-----", t.toString())
                        materialProgressDialog.dismiss()
                    }

                })
            }

        } else {
            materialProgressDialog.dismiss()
            handler.post {
                Toast.makeText(
                    this@ProductionOrderLinesActivity,
                    "No Network Connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val handler = Handler(Looper.getMainLooper())


    //todo getting BPL_ID Number ....
    private fun getBPL_IDNumber() {
        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            try {
                materialProgressDialog.show()
                val networkClient = NetworkClients.create(this@ProductionOrderLinesActivity)
                var batch = "Quality"//WIP
                networkClient.doGetBplID(
                    "BusinessPlaceID,WarehouseCode",
                    "WarehouseCode eq '" + sessionManagement.getWarehouseCode(this) + "'"
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
                                        //getDocSeriesApi(BPLIDNum.toString())
                                        sessionManagement.setWarehouseCode(
                                            this@ProductionOrderLinesActivity,
                                            responseModel.value[0].WarehouseCode
                                        )
                                        setAdapter()
//                                        Toast.makeText(this@ProductionOrderLinesActivity, BPLIDNum.toString(), Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(
                                            this@ProductionOrderLinesActivity,
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
                                                this@ProductionOrderLinesActivity,
                                                mError.error.message.value
                                            )
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(
                                                this@ProductionOrderLinesActivity,
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

    private fun getDocSeriesApi(bplidNum: String) {
        if (networkConnection.getConnectivityStatusBoolean(applicationContext)) {
            try {
                val customClient = ApiConstantForURL()
                QuantityNetworkClient.updateBaseUrlFromConfig(customClient, true)
                val customQtyClient = QuantityNetworkClient.create(this@ProductionOrderLinesActivity)
                customQtyClient.getDocSeries(bplidNum, "60").apply {
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
                                            this@ProductionOrderLinesActivity,
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
                                                this@ProductionOrderLinesActivity,
                                                mError.error.message.value
                                            )
                                        }
                                        if (mError.error.message.value != null) {
                                            GlobalMethods.showError(
                                                this@ProductionOrderLinesActivity,
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


    //todo getting api list response from adapter to activity trough interface...
    override fun passList(dataList: List<ScanedOrderBatchedItems.Value>) {
        batchList = dataList
    }

    //todo onActivity function override for qr code scanning in adapter..
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            Log.e("Result==>", data?.getStringExtra("batch_code").toString())
            productionOrderLinesAdapter.onActivityResult(requestCode, resultCode, data)
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
        val url = "jdbc:jtds:sqlserver://" + AppConstants.IP + ":" + AppConstants.PORT + "/" + sessionManagement.getCompanyDB(this)!!
        ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.INTERNET), PackageManager.PERMISSION_GRANTED)
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

    inner class BolicyTask : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            return "Task completed"
        }

        override fun onPostExecute(result: String) {
            //setSqlServer()
        }
    }


    suspend fun makeNetworkRequest(): String {
        // Use Retrofit or another networking library here
        // For simplicity, let's pretend we're using a simple HTTPURLConnection
        val url = URL("jdbc:jtds:sqlserver://" + AppConstants.IP + ":" + AppConstants.PORT + "/" + sessionManagement.getCompanyDB(this)!!)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            val inputStream = connection.inputStream
            val result = inputStream.bufferedReader().readText()
            result
        } finally {
            connection.disconnect()
        }
    }


    companion object {
        private const val INTERNET_PERMISSION_REQUEST_CODE = 1
    }


    private fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestInternetPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), INTERNET_PERMISSION_REQUEST_CODE)
    }

    private fun configureStrictMode() {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        GlobalScope.launch(Dispatchers.IO) {
            // Perform your network operation here
            // For example, make an HTTP request using a library like Retrofit
            val url = makeNetworkRequest()
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
            // Once the operation is complete, you can update the UI if needed
            launch(Dispatchers.Main) {

            }
        }


        /*  val url = "jdbc:jtds:sqlserver://" + AppConstants.IP + ":" + AppConstants.PORT + "/" + AppConstants.COMPANY_DB
          Log.e("QSL==>", "Success$url")
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
          }*/

        // Now, you can perform network-related operations with the INTERNET permission
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == INTERNET_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, configure StrictMode and perform network-related operations
                configureStrictMode()
            } else {
                // Permission denied, handle the denial (e.g., show a message to the user)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        var intent = Intent()
        intent.putExtra("isBack", true)
        setResult(RESULT_OK, intent)
        finish()

    }

}