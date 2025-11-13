package com.example.acPolycoaters.ui.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.GridLayoutManager
import com.example.acPolycoaters.Adapter.HomeAdapter
import com.example.acPolycoaters.BuildConfig
import com.example.acPolycoaters.Global_Classes.AppConstants
import com.example.acPolycoaters.Global_Classes.GlobalMethods
import com.example.acPolycoaters.Global_Classes.MaterialProgressDialog
import com.example.acPolycoaters.Global_Notification.NetworkConnection
import com.example.acPolycoaters.Model.HomeItem
import com.example.acPolycoaters.R
import com.example.acPolycoaters.Retrofit_Api.ApiConstantForURL
import com.example.acPolycoaters.Retrofit_Api.NetworkClients
import com.example.acPolycoaters.Retrofit_Api.QuantityNetworkClient
import com.example.acPolycoaters.SessionManagement.SessionManagement
import com.example.acPolycoaters.databinding.ActivityHomeBinding
import com.example.acPolycoaters.ui.inventoryTransferRequest.ui.InventoryTransferRequestActivity
import com.example.acPolycoaters.ui.inventoryTransferStandalone.ui.InventoryTransferStandaloneActivity
import com.example.acPolycoaters.ui.login.LoginActivity
import com.example.acPolycoaters.ui.issueForProductionOrder.UI.productionOrderLines.ProductionListActivity
import com.example.acPolycoaters.ui.login.Model.LoginResponseModel
import com.example.acPolycoaters.ui.setting.SettingActivity
import com.pixplicity.easyprefs.library.Prefs
import com.webapp.internetconnection.CheckNetwoorkConnection
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    private lateinit var homeBinding: ActivityHomeBinding
    lateinit var networkConnection: NetworkConnection
    lateinit var materialProgressDialog: MaterialProgressDialog
    lateinit var sessionManagement: SessionManagement
    lateinit var checkNetwoorkConnection: CheckNetwoorkConnection
    private var isLoggingOut = false
    var flag: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)
        //todo set title on header...
        title = "Menu Screen"
        supportActionBar?.setDisplayShowHomeEnabled(true)

        networkConnection = NetworkConnection()
        materialProgressDialog = MaterialProgressDialog(this@HomeActivity)
        checkNetwoorkConnection = CheckNetwoorkConnection(application)
        sessionManagement = SessionManagement(this)
        homeBinding.tvDBName.text = "DB:- ${sessionManagement.getCompanyDB(this@HomeActivity)}"
        homeBinding.tvAppVersion.text = BuildConfig.FORCED_VERSION_NAME //getString(R.string.VersionName)

        val items = listOf(
            HomeItem(R.drawable.issue_prod_icon, "Issue for Production", AppConstants.ISSUE_FOR_PRODUCTION, "Y"),
            //HomeItem(R.drawable.ic_scan_view, "Scan & View", AppConstants.SCAN_AND_VIEW, "Y"),
            HomeItem(R.drawable.ic_inventory_req, "Inventory Req.", AppConstants.INVENTORY_REQ, "Y"),
            HomeItem(R.drawable.delivery_icon, "Goods Issue", AppConstants.GOODS_ISSUE, "N"),
            HomeItem(R.drawable.receipt_prod_icon, "Inventory Transfer (GRPO)", AppConstants.INVENTORY_TRANSFER_GRPO, "N"),
            HomeItem(R.drawable.receipt_prod_icon, "Goods Receipt PO", AppConstants.GOODS_RECEIPT_PO, "N"),
            HomeItem(R.drawable.receipt_prod_icon, "Sale To Invoice", AppConstants.SALE_TO_INVOICE, "N"),
            HomeItem(R.drawable.receipt_prod_icon, "Receipt from Production", AppConstants.RECEIPT_FROM_PRODUCTION, "N"),
            HomeItem(R.drawable.receipt_prod_icon, "Pick List", AppConstants.PICK_LIST, "N"),
            HomeItem(R.drawable.receipt_prod_icon, "Return Components", AppConstants.RETURN_COMPONENTS, "N"),
            HomeItem(R.drawable.receipt_prod_icon, "Goods Receipt", AppConstants.GOODS_RECEIPT, "N"),
            HomeItem(R.drawable.receipt_prod_icon, "Sale To Delivery", AppConstants.SALE_TO_DELIVERY, "Y"),
            HomeItem(R.drawable.receipt_prod_icon, "Inventory Transfer", AppConstants.INVENTORY_TRANSFER_STANDALONE, "Y")
        )

        val filterItems = items.filter { it.status == "Y" }
        setHomeItemAdapter(filterItems)

        homeBinding.issueCard.setOnClickListener {
            //callIssueCardApi()
            flag = "Issue_Order"
            var intent: Intent = Intent(this@HomeActivity, ProductionListActivity::class.java)
            intent.putExtra("flag", flag)
            startActivity(intent)
        }

        homeBinding.returnCard.setOnClickListener {
            GlobalMethods.showMessage(this, "Work In Process.")
            /*  var intent: Intent = Intent(this, ProductionListActivity::class.java)
              startActivity(intent)*/
        }

        homeBinding.receiptCard.setOnClickListener {
            GlobalMethods.showMessage(this, "Work In Process.")
            /* var intent: Intent = Intent(this, InventoryActivity::class.java)
             startActivity(intent)*/
        }
        homeBinding.deliveryCard.setOnClickListener {
            flag = "Delivery_Order"
            var intent: Intent = Intent(this, ProductionListActivity::class.java)
            intent.putExtra("flag", flag)
            startActivity(intent)
        }


    }

    private fun setHomeItemAdapter(filterItems: List<HomeItem>) {
        homeBinding.rvHomeItems.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 2, GridLayoutManager.VERTICAL, false)
            val homeAdapter = HomeAdapter(filterItems) { clickedId ->
                handleItemClick(clickedId)
            }
            adapter = homeAdapter
        }
    }


    private fun handleItemClick(clickedId: String) {
        when (clickedId) {
            AppConstants.ISSUE_FOR_PRODUCTION -> {
                flag = "Issue_Order"
                var intent: Intent = Intent(this@HomeActivity, ProductionListActivity::class.java)
                intent.putExtra("flag", flag)
                startActivity(intent)
            }

            AppConstants.SCAN_AND_VIEW -> {
                /*var intent: Intent = Intent(this, ScanQRViewActivity::class.java)
                startActivity(intent)*/
            }

            AppConstants.INVENTORY_REQ -> {
                var intent: Intent = Intent(this, InventoryTransferRequestActivity::class.java)
                // intent.putExtra("flag",flag)
                startActivity(intent)
            }

            AppConstants.GOODS_ISSUE -> {
                /*var intent: Intent = Intent(this, GoodsOrderActivity::class.java)
                startActivity(intent)*/
            }

            AppConstants.INVENTORY_TRANSFER_GRPO -> {
                /*var intent: Intent = Intent(this, InventoryOrderActivity_ITR_GRPO::class.java)
                startActivity(intent)*/
            }

            AppConstants.GOODS_RECEIPT_PO -> {
                /*var intent: Intent = Intent(this, PurchaseOrderActivity::class.java)
                startActivity(intent)*/
            }

            AppConstants.SALE_TO_INVOICE -> {
                /*flag = AppConstants.SALE_TO_INVOICE
                var intent: Intent = Intent(this, SaleToInvoiceActivity::class.java)
                intent.putExtra("flag", flag)
                startActivity(intent)*/
            }

            AppConstants.RECEIPT_FROM_PRODUCTION -> {
                /*var intent: Intent = Intent(this, RFPActivity::class.java)
                startActivity(intent)*/
            }

            AppConstants.PICK_LIST -> {
                /*var intent = Intent(this, PickListActivity::class.java)
                startActivity(intent)*/
            }

            AppConstants.RETURN_COMPONENTS -> {
                /*flag = "Issue_Order"
                var intent = Intent(this@HomeActivity, ReturnComponentListActivity::class.java)
                intent.putExtra("flag", flag)
                startActivity(intent)*/
            }

            AppConstants.GOODS_RECEIPT -> {
                /*var intent: Intent = Intent(this, GoodsReceiptActivity::class.java)
                startActivity(intent)*/
            }

            AppConstants.INVENTORY_TRANSFER_STANDALONE -> {
                var intent: Intent = Intent(this, InventoryTransferStandaloneActivity::class.java)
                startActivity(intent)
            }

            AppConstants.SALE_TO_DELIVERY -> {
                flag = "Delivery_Order"
                var intent: Intent = Intent(this, ProductionListActivity::class.java)
                intent.putExtra("flag", flag)
                startActivity(intent)
            }

        }
    }

    //todo set search icon on activity...
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                //todo Handle icon click
                dialog()
                return true
            }

            R.id.settings -> {
                //chooseScannerPopupDialog()
                startActivity(Intent(this, SettingActivity::class.java))
                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.logout_menu, menu)
        val item = menu.findItem(R.id.logout)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_IF_ROOM)

        return true
    }

    fun dialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.logout_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.dismiss()

        var cancel = dialog.findViewById<Button>(R.id.cancelbtn)
        var logoutbtn = dialog.findViewById<Button>(R.id.logoutbtn)


        cancel.setOnClickListener {
            dialog.dismiss()
        }

        logoutbtn.setOnClickListener {
            logoutApiHit()
        }
        dialog.show()
    }


    //todo issue for production api calling...
    /*fun logoutApiHit() {
        checkNetwoorkConnection.observe(this) { isConnected ->
            if (isConnected) {
                materialProgressDialog.show()
                val networkClient = NetworkClients.create(this)
                networkClient.doGetLogoutCall("B1SESSION=" + sessionManagement.getSessionId(this)).apply {
                    enqueue(object : Callback<LoginResponseModel> {
                        override fun onResponse(call: Call<LoginResponseModel>, response: Response<LoginResponseModel>) {
                            try {
                                var i: Intent = Intent(this@HomeActivity, LoginActivity::class.java)
                                startActivity(i)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponseModel>, t: Throwable) {
                            Log.e("issueCard_failure-----", t.toString())
                            materialProgressDialog.dismiss()
                        }

                    })
                }

            } else {
                materialProgressDialog.dismiss()
                Toast.makeText(this@HomeActivity, "No Network Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    fun logoutApiHit() {
        checkNetwoorkConnection.observe(this) { isConnected ->
            if (isConnected) {
                if (isLoggingOut) return@observe
                isLoggingOut = true

               /* if (Prefs.getString(AppConstants.AppIP, "").isEmpty()) {
                    startLoginActivity()
                } else {*/
                    materialProgressDialog.show()

                    val networkClient = NetworkClients.create(this)
                    networkClient.doGetLogoutCall("B1SESSION=" + sessionManagement.getSessionId(this))
                        .enqueue(object : Callback<LoginResponseModel> {
                            override fun onResponse(
                                call: Call<LoginResponseModel>,
                                response: Response<LoginResponseModel>
                            ) {
                                try {
                                    clearSessionAndNavigate()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    isLoggingOut = false
                                }
                            }

                            override fun onFailure(call: Call<LoginResponseModel>, t: Throwable) {
                                Log.e("Logout_failure", t.toString())
                                Toasty.error(this@HomeActivity, t.toString())
                                materialProgressDialog.dismiss()
                                clearSessionAndNavigate()
                            }
                        })
                //}
            } else {
                materialProgressDialog.dismiss()
                Toast.makeText(this@HomeActivity, "No Network Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearSessionAndNavigate() {
        Prefs.putString(AppConstants.BPLID, "")
        sessionManagement.setSessionId(this, "")
        sessionManagement.setSessionTimeout(this, "")
        startLoginActivity()
    }

    private fun startLoginActivity() {
        if (!isFinishing) {
            val intent = Intent(this@HomeActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


    //todo choose scanner type..
    @SuppressLint("MissingInflatedId")
    private fun chooseScannerPopupDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val view = LayoutInflater.from(this).inflate(R.layout.scanner_custom_alert, null)
        builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        builder.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        builder.window?.setGravity(Gravity.CENTER)
        builder.setView(view)

        //todo set ui text ...
        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group)
        val radioLaser = view.findViewById<RadioButton>(R.id.radioLaser)
        val radioQrScanner = view.findViewById<RadioButton>(R.id.radioQrScanner)
        val goBtn = view.findViewById<AppCompatButton>(R.id.goBtn)

        //todo get radio buttons selected id..
        var checkGender = ""

        radioGroup?.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            var radioButton = group.findViewById<RadioButton>(checkedId)
            checkGender = radioButton.text.toString()
            when (checkedId) {
                R.id.radioLaser -> {
                    radioLaser.isChecked = true
                }

                R.id.radioQrScanner -> {
                    radioQrScanner.isChecked = true
                }
            }
            /*  if (radioButton != null && checkedId != -1) {
                  Toast.makeText(this, radioButton.text, Toast.LENGTH_SHORT).show()
              } else {
                  return@OnCheckedChangeListener
              }*/
        })

        //todo validation for toggle..
        if (sessionManagement.getScannerType(this) == "LEASER") {
            radioLaser.isChecked = true
        } else if (sessionManagement.getScannerType(this) == "QR_SCANNER") {
            radioQrScanner.isChecked = true
        }

        //todo go btn..
        goBtn?.setOnClickListener {
            if (checkGender.equals("L")) {
//                sessionManagement.setLaser(1)
//                sessionManagement.setQRScanner(0)
                sessionManagement.setScannerType(this, "LEASER")
            } else if (checkGender.equals("S")) {
//                sessionManagement.setLaser(0)
//                sessionManagement.setQRScanner(1)
                sessionManagement.setScannerType(this, "QR_SCANNER")
            }
            builder.dismiss()
        }

        builder.setCancelable(true)
        builder.show()

    }

    override fun onBackPressed() {
        showExitDialog()
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                finishAffinity() // Close all activities and exit app
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}