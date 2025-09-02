package com.example.acPolycoaters.ui.issueForProductionOrder.UI.productionOrderLines


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acPolycoaters.Global_Classes.AppConstants
import com.example.acPolycoaters.Global_Classes.GlobalMethods
import com.example.acPolycoaters.Global_Classes.GlobalMethods.toPrettyJson
import com.example.acPolycoaters.Global_Classes.GlobalMethods.toSimpleJson
import com.example.acPolycoaters.Global_Classes.MaterialProgressDialog
import com.example.acPolycoaters.Model.OtpErrorModel
import com.example.acPolycoaters.R
import com.example.acPolycoaters.Retrofit_Api.ApiConstantForURL
import com.example.acPolycoaters.Retrofit_Api.NetworkClients
import com.example.acPolycoaters.Retrofit_Api.QuantityNetworkClient
import com.example.acPolycoaters.SessionManagement.SessionManagement
import com.example.acPolycoaters.databinding.ActivityListProductionBinding
import com.example.acPolycoaters.ui.DemoActivity
import com.example.acPolycoaters.ui.deliveryOrderModule.Adapter.DeliveryListAdapter
import com.example.acPolycoaters.ui.deliveryOrderModule.Model.DeliveryModel
import com.example.acPolycoaters.ui.deliveryOrderModule.UI.DeliveryDocumentLineActivity
import com.example.acPolycoaters.ui.issueForProductionOrder.Adapter.IssueOderAdapter
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ProductionListModel
import com.example.acPolycoaters.ui.login.LoginActivity
import com.google.gson.GsonBuilder
import com.pixplicity.easyprefs.library.Prefs
import com.webapp.internetconnection.CheckNetwoorkConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.math.BigDecimal


class ProductionListActivity : AppCompatActivity() {
    private lateinit var activityListBinding: ActivityListProductionBinding
    private lateinit var issueOderAdapter: IssueOderAdapter
    private var productionListModel_gl: ArrayList<ProductionListModel.Value> = ArrayList()
    lateinit var materialProgressDialog: MaterialProgressDialog
    lateinit var checkNetwoorkConnection: CheckNetwoorkConnection
    private var deliveryModelList_gl: ArrayList<DeliveryModel.Value> = ArrayList()
    private lateinit var deliveryAdapter: DeliveryListAdapter
    private lateinit var sessionManagement: SessionManagement
    private var isBack = false
    var apicall: Boolean = true
    var isScrollingpage: Boolean = false
    var limit = 100
    var flag: String = ""

    private var page = 0
    private val pageSize = 20
    private var isLoading = false
    private var isLastPage = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityListBinding = ActivityListProductionBinding.inflate(layoutInflater)
        setContentView(activityListBinding.root)

//        clearCache(this)
        deleteCache(this)

        supportActionBar?.setDisplayShowHomeEnabled(true)

        materialProgressDialog = MaterialProgressDialog(this@ProductionListActivity)
        checkNetwoorkConnection = CheckNetwoorkConnection(application)
        sessionManagement = SessionManagement(this@ProductionListActivity)


        //todo get arguments from previous activity...
        val myIntent = intent
        flag = myIntent.getStringExtra("flag")!!
        Log.d("onCreate====>", "onCreate")
        if (flag.equals("Issue_Order")) {

            title = "Issue Production"

            //todo loading initial list items and calling adapter-----
            Log.e("loadMoreListItems==>", "Items_loading...")


            isLastPage = false
            productionListModel_gl.clear()
            setIssueOrderAdapter()
            loadIssueOrderListItems(page)

            //todo recycler view scrollListener for add more items in list...
            activityListBinding.rvProductionList.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= pageSize
                        ) {
                            page++
                            loadIssueOrderListItems(page)
                        }
                    }
                }
            })

        } else if (flag.equals("Delivery_Order")) {
            title = "Delivery Order"
            Log.e("DELIVERY_ORDER", "BackFlag : $isBack")
            isLastPage = false
            deliveryModelList_gl.clear()
            setDeliveryOrderAdapter()
            loadDeliveryOrderListItems(page)
            activityListBinding.rvProductionList.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= pageSize
                        ) {
                            page++
                            loadDeliveryOrderListItems(page)
                        }
                    }
                }
            })

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {  // check requestCode also
            isBack = data?.getBooleanExtra("isBack", false) ?: false
            Log.e("DELIVERY_ORDER", "BackFlag From onActivityResult: $isBack")
        }
    }


    fun clearCache(context: Activity) {
        try {
            // Get the cache directory for your application
            val cacheDir = context.cacheDir

            // Check if the cache directory exists
            if (cacheDir != null && cacheDir.isDirectory) {
                // Delete all files and subdirectories in the cache directory
                val children = cacheDir.list()
                for (child in children) {
                    val cacheFile = File(cacheDir, child)
                    cacheFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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


    override fun onRestart() {
        super.onRestart()
        Log.d("Restart====>", "Restart")
        productionListModel_gl.clear()
        deliveryModelList_gl.clear()

    }

    fun totalskipCount(curret: Int): Int {
        var total = limit * page
        return limit * page;
    }

    lateinit var layoutManager: RecyclerView.LayoutManager

    //todo set adapter...
    fun setIssueOrderAdapter() {

        if (!::issueOderAdapter.isInitialized) {
            issueOderAdapter = IssueOderAdapter(productionListModel_gl, callBack = { list, pos ->
                var productionValueList = list[pos]
                var productionLinesList = list[pos].ProductionOrderLines

                Log.i("PO_LIST", "productionValueList: $productionValueList")
                Log.w("PO_LIST", "productionLinesList: $productionLinesList")

                CoroutineScope(Dispatchers.IO).launch {
                    var intent: Intent = Intent(this@ProductionListActivity, ProductionOrderLinesActivity::class.java)
                    intent.putExtra("productionLinesList", productionLinesList as Serializable)
                    intent.putExtra("productionValueList", productionValueList as Serializable)
                    intent.putExtra("pos", pos)
                    startActivityForResult(intent, 1001)

                    withContext(Dispatchers.Main) {

                        if (productionLinesList.size > 0) {
                            sessionManagement.setWarehouseCode(this@ProductionListActivity, productionLinesList[0].Warehouse)
                        } else {
                            sessionManagement.setWarehouseCode(this@ProductionListActivity, productionValueList.Warehouse)
                        }
                    }
                }
            })
            activityListBinding.rvProductionList.apply {
                layoutManager = LinearLayoutManager(this@ProductionListActivity)
                adapter = issueOderAdapter
            }
        } else {
            issueOderAdapter.notifyDataSetChanged()
        }
    }


    //todo ISSUE ORDER API LIST load next production item list....
    fun loadIssueOrderListItems(page: Int) {
        checkNetwoorkConnection.observe(this) { isConnected ->
            if (isConnected) {
                materialProgressDialog.show()
                val skip = page * pageSize
                var apiConfig = ApiConstantForURL()

                QuantityNetworkClient.updateBaseUrlFromConfig(apiConfig, true)
                val networkClient = QuantityNetworkClient.create(this)
                val bplId = if (Prefs.getString(AppConstants.BPLID, "").isNotEmpty()) Prefs.getString(AppConstants.BPLID, "") else ""
                Log.i("BRANCH", "BPLId: $bplId")
                networkClient.doGetProductionList(
                    sessionManagement.getCompanyDB(this)!!, bplId, ""
                ).apply { // ,"" + skip
                    enqueue(object : Callback<ProductionListModel> {
                        override fun onResponse(
                            call: Call<ProductionListModel>,
                            response: Response<ProductionListModel>
                        ) {
                            try {
                                if (response.isSuccessful) {
                                    Log.e("api_hit_response===>", response.toString())
                                    materialProgressDialog.dismiss()
                                    var productionListModel1 = response.body()!!
                                    var productionList_gl = productionListModel1.value
                                    Toast.makeText(this@ProductionListActivity, "Successfully!", Toast.LENGTH_SHORT)
                                    if (!productionList_gl.isNullOrEmpty() && productionList_gl.size > 0) {
                                        activityListBinding.ivNoDataFound.visibility = View.GONE
                                        activityListBinding.rvProductionList.visibility = View.VISIBLE
                                        Log.e("page---->", page.toString())
                                        val filteredOrders = productionList_gl.map { order ->
                                            order.copy(
                                                ProductionOrderLines = order.ProductionOrderLines?.filter {
                                                    val planned = it.PlannedQuantity?.toBigDecimal() ?: BigDecimal.ZERO
                                                    val issued = it.IssuedQuantity?.toBigDecimal() ?: BigDecimal.ZERO
                                                    planned > issued   // precise comparison
                                                } as ArrayList<ProductionListModel.ProductionOrderLine>
                                            )
                                        }.filter { it.ProductionOrderLines?.isNotEmpty() == true }
                                        Log.i("PO_LIST", "productionValueList Default: ${toSimpleJson(filteredOrders)}")

                                        if (filteredOrders.isNotEmpty()) {
                                            activityListBinding.ivNoDataFound.visibility = View.GONE
                                            activityListBinding.rvProductionList.visibility = View.VISIBLE
                                        } else {
                                            activityListBinding.ivNoDataFound.visibility = View.VISIBLE
                                            activityListBinding.rvProductionList.visibility = View.GONE
                                        }


                                        // Filter out duplicates by DocEntry
                                        val uniqueItems = filteredOrders.filter { newItem ->
                                            productionListModel_gl.none { existing -> existing.DocumentNumber == newItem.DocumentNumber }
                                        }

                                        if (uniqueItems.isNotEmpty()) {
                                            val startPos = productionListModel_gl.size
                                            productionListModel_gl.addAll(uniqueItems)
                                            Log.i("ISSUE_ORDER", "Issue Order List size : ${productionListModel_gl.size}")
                                            issueOderAdapter.notifyItemRangeInserted(startPos, uniqueItems.size)
                                        } else {
                                            isLastPage = true // no unique items left
                                        }
                                        isLastPage = false

                                    }else {
                                        activityListBinding.ivNoDataFound.visibility = View.VISIBLE
                                        activityListBinding.rvProductionList.visibility = View.GONE
                                    }

                                } else {
                                    materialProgressDialog.dismiss()
                                    val gson1 = GsonBuilder().create()
                                    var mError: OtpErrorModel
                                    try {
                                        val s = response.errorBody()!!.string()
                                        mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                        if (mError.error.code == 400) {
                                            GlobalMethods.showError(
                                                this@ProductionListActivity,
                                                mError.error.message.value
                                            )
                                        }
                                        if (mError.error.code == 306 && mError.error.message.value != null) {
                                            GlobalMethods.showError(
                                                this@ProductionListActivity,
                                                mError.error.message.value
                                            )
                                            val mainIntent = Intent(
                                                this@ProductionListActivity,
                                                LoginActivity::class.java
                                            )
                                            startActivity(mainIntent)
                                            finish()
                                        }
                                        /*if (mError.error.message.value != null) {
                                            AppConstants.showError(this@ProductionListActivity, mError.error.message.value)
                                            Log.e("json_error------", mError.error.message.value)
                                        }*/
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(call: Call<ProductionListModel>, t: Throwable) {
                            Log.e("issueCard_failure-----", t.toString())
                            materialProgressDialog.dismiss()
                        }
                    })
                }

            } else {
                materialProgressDialog.dismiss()
                GlobalMethods.showError(this, "No Network Connection")
            }
        }

    }

    //todo DELIVERY ORDER API LIST ITEMS BIND....
    fun loadDeliveryOrderListItems(page: Int) {
        checkNetwoorkConnection.observe(this) { isConnected ->

            if (isConnected) {
                isLoading = true
                materialProgressDialog.show()

                val skip = page * pageSize

                val networkClient = NetworkClients.create(this)
                val bplId = if (Prefs.getString(AppConstants.BPLID, "").isNotEmpty()) Prefs.getString(AppConstants.BPLID, "") else ""
                Log.i("BRANCH", "BPLId: $bplId")
                networkClient.deliveryOrder(
                    "DocumentStatus eq 'bost_Open' and BPL_IDAssignedToInvoice eq $bplId",
                    "DocNum desc",
                    skip, pageSize,
                ).apply {
                    enqueue(object : Callback<DeliveryModel> {
                        override fun onResponse(
                            call: Call<DeliveryModel>,
                            response: Response<DeliveryModel>
                        ) {
                            materialProgressDialog.dismiss()
                            try {
                                if (response.isSuccessful) {
                                    val listResponse = response.body()!!
                                    /*val newItems = listResponse.value

                                    if (newItems.isNotEmpty()) {
                                        val startPos = deliveryModelList_gl.size
                                        deliveryModelList_gl.addAll(newItems)
                                        Log.i("DELIVERY_ORDER", "Delivery Order List size : ${deliveryModelList_gl.size}")
                                        deliveryAdapter.notifyItemRangeInserted(startPos, newItems.size)
                                    } else {
                                        isLastPage = true // no more data
                                    }*/

                                    val newItems = listResponse.value

                                    if (newItems.isNotEmpty()) {
                                        activityListBinding.ivNoDataFound.visibility = View.GONE
                                        activityListBinding.rvProductionList.visibility = View.VISIBLE
                                    } else {
                                        activityListBinding.ivNoDataFound.visibility = View.VISIBLE
                                        activityListBinding.rvProductionList.visibility = View.GONE
                                    }

                                    // Filter out duplicates by DocEntry
                                    val uniqueItems = newItems.filter { newItem ->
                                        deliveryModelList_gl.none { existing -> existing.DocEntry == newItem.DocEntry }
                                    }

                                    if (uniqueItems.isNotEmpty()) {

                                        val startPos = deliveryModelList_gl.size
                                        deliveryModelList_gl.addAll(uniqueItems)
                                        Log.i("DELIVERY_ORDER", "Delivery Order List size : ${deliveryModelList_gl.size}")
                                        deliveryAdapter.notifyItemRangeInserted(startPos, uniqueItems.size)
                                    } else {
                                        isLastPage = true // no unique items left
                                    }
                                    isLastPage = false
                                } else {
                                    isLastPage = true
                                    //handleErrorResponse(response)
                                    materialProgressDialog.dismiss()
                                    val gson1 = GsonBuilder().create()
                                    var mError: OtpErrorModel
                                    try {
                                        val s = response.errorBody()!!.string()
                                        mError = gson1.fromJson(s, OtpErrorModel::class.java)
                                        if (mError.error.code == 400) {
                                            GlobalMethods.showError(
                                                this@ProductionListActivity,
                                                mError.error.message.value
                                            )
                                        }
                                        if (mError.error.code == 306 && mError.error.message.value != null) {
                                            GlobalMethods.showError(
                                                this@ProductionListActivity,
                                                mError.error.message.value
                                            )
                                            val mainIntent = Intent(
                                                this@ProductionListActivity,
                                                LoginActivity::class.java
                                            )
                                            startActivity(mainIntent)
                                            finish()
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }

                        override fun onFailure(call: Call<DeliveryModel>, t: Throwable) {
                            Log.e("delivery_failure-----", t.toString())
                            materialProgressDialog.dismiss()
                            isLoading = false
                        }
                    })
                }
            } else {
                materialProgressDialog.dismiss()
                GlobalMethods.showError(this, "No Network Connection")
            }
        }

    }

    //todo bind delivery order adapter...
    fun setDeliveryOrderAdapter() {
        /* layoutManager = LinearLayoutManager(this)
         activityListBinding.rvProductionList.layoutManager = layoutManager
         deliveryAdapter = DeliveryListAdapter(deliveryModelList_gl, callBack = {valueList, pos ->
             var deliveryValueList = valueList[pos]
             var documentLineList = valueList[pos].DocumentLines
             var intent: Intent = Intent(this, DeliveryDocumentLineActivity::class.java)
             intent.putExtra("documentLineList", documentLineList as Serializable)
             intent.putExtra("deliveryValueList", deliveryValueList as Serializable)
             intent.putExtra("pos", pos)
             startActivity(intent)
         })
         activityListBinding.rvProductionList.adapter = deliveryAdapter
         deliveryAdapter?.notifyDataSetChanged()*/

        if (!::deliveryAdapter.isInitialized) {
            deliveryAdapter = DeliveryListAdapter(deliveryModelList_gl, callBack = { valueList, pos ->
                var deliveryValueList = valueList[pos]
                var documentLineList = valueList[pos].DocumentLines
                var intent: Intent = Intent(this, DeliveryDocumentLineActivity::class.java)
                intent.putExtra("documentLineList", documentLineList as Serializable)
                intent.putExtra("deliveryValueList", deliveryValueList as Serializable)
                intent.putExtra("pos", pos)
                startActivityForResult(intent, 1001)
            })
            activityListBinding.rvProductionList.apply {
                layoutManager = LinearLayoutManager(this@ProductionListActivity)
                adapter = deliveryAdapter
            }
        } else {
            deliveryAdapter.notifyDataSetChanged()
        }
    }


    //todo delivery document order line adapter on paricular item click listener...
    /*override fun onItemClick(valueList: List<DeliveryModel.Value>, pos: Int) {
        var deliveryValueList = valueList[pos]
        var documentLineList = valueList[pos].DocumentLines
        var intent: Intent = Intent(this, DeliveryDocumentLineActivity::class.java)
        intent.putExtra("documentLineList", documentLineList as Serializable)
        intent.putExtra("deliveryValueList", deliveryValueList as Serializable)
        intent.putExtra("pos", pos)
        startActivity(intent)
    }*/


    //todo set search icon on activity...
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_icon -> {
                //todo Handle icon click
                return true
            }

            R.id.list_icon -> {
                //todo Handle icon click
                var intent = Intent(this@ProductionListActivity, DemoActivity::class.java)
                startActivity(intent)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.my_menu, menu)
        val item = menu.findItem(R.id.search_icon)
        val searchView = SearchView((this@ProductionListActivity).supportActionBar!!.themedContext)

        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_IF_ROOM)
        item.actionView = searchView
        searchView.queryHint = "Search Here"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                handleSearch(newText)
                return true
            }
        })
        return true
    }

    //todo search filter..
    private fun handleSearch(query: String) {
        if (flag.equals("Issue_Order")) {
            val filteredList = issueSearchList(query)
            issueOderAdapter?.setFilteredItems(filteredList)
        } else if (flag.equals("Delivery_Order")) {
            val deliveryFilterList = deliverySearchList(query)
            deliveryAdapter?.setFilteredItems(deliveryFilterList)
        }
    }

    //todo this function filter issue for production list based on text...
    fun issueSearchList(query: String): ArrayList<ProductionListModel.Value> {
        val filteredList = ArrayList<ProductionListModel.Value>()
        for (item in productionListModel_gl) {
            if (item.ItemNo.contains(
                    query,
                    ignoreCase = true
                ) || item.DocumentNumber.contains(query, ignoreCase = true)
            ) {
                filteredList.add(item)
            }
        }

        return filteredList
    }

    //todo this function filter delivery order list based on text...
    fun deliverySearchList(query: String): ArrayList<DeliveryModel.Value> {
        val filteredList = ArrayList<DeliveryModel.Value>()
        for (item in deliveryModelList_gl) {
            if (item.DocNum.contains(query, ignoreCase = true) || item.CardCode.contains(
                    query,
                    ignoreCase = true
                )
            ) {
                filteredList.add(item)
            }
        }
        return filteredList
    }


    override fun onBackPressed() {
        super.onBackPressed()
        /* var intent = Intent(this@ProductionListActivity, HomeActivity::class.java)
         intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
         startActivity(intent)*/
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (::deliveryAdapter.isInitialized) {
            deliveryModelList_gl.clear()
            deliveryAdapter.notifyDataSetChanged()
            page = 0
            isLastPage = false
            loadDeliveryOrderListItems(page)
        } else if (::issueOderAdapter.isInitialized) {
            productionListModel_gl.clear()
            issueOderAdapter.notifyDataSetChanged()
            page = 0
            isLastPage = false
            loadIssueOrderListItems(page)
        }

    }


}