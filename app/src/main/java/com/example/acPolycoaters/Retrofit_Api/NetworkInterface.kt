package com.example.acPolycoaters.Retrofit_Api

import com.example.acPolycoaters.Model.*
import com.example.acPolycoaters.ui.deliveryOrderModule.Model.DeliveryModel
import com.example.acPolycoaters.ui.inventoryTransferRequest.model.InventoryRequestModel
import com.example.acPolycoaters.ui.inventoryTransferStandalone.model.GetSeriesModel
import com.example.acPolycoaters.ui.inventoryTransferStandalone.model.InventoryPostResponse
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.InventoryGenExitsModel
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ProductionListModel
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ScanedOrderBatchedItems
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.WarehouseBPL_IDModel
import com.example.acPolycoaters.ui.login.Model.LoginResponseModel
import com.example.acPolycoaters.ui.setting.model.ModelGetBranch
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface NetworkInterface {

    //todo login API---
    @POST(ApiConstant.LOGIN)
    @Headers("Content-Type:application/json;charset=UTF-8")
    fun doGetLoginCall(@Body jsonObject: JsonObject): Call<LoginResponseModel>

    @POST(ApiConstant.LOGOUT)
    fun doGetLogoutCall(
        @Header("Cookie") cookieToken: String
    ): Call<LoginResponseModel>

    //todo production list api...
    @GET(ApiConstant.PRODUCTION_ORDERS)
    fun doGetProductionList(
//        @Query("filter") longitude: String,
    ): Call<ProductionListModel>

    //todo production list api test...
    @GET(ApiConstant.PRODUCTION_ORDERS)
    fun doGetProductionListCount(
        /* @Header("Prefer") Prefer: String,*/
        @Query("$" + "filter") filter: String,
        @Query("$" + "skip") skip: String,
        @Query("$" + "orderby") orderby: String,
        @Query("\$top") top: Int
    ): Call<ProductionListModel>

    //todo production list api...
    @GET(ApiConstant.PRODUCTION_ORDER)
    fun doGetProductionList(
        @Query("DBName") DBName: String,
        @Query("BPLId") BPLId: String,
        @Query("DocNum") docNum: String
    ): Call<ProductionListModel>

    // ADDED BY VINOD KUMAR PAL @17NOV,2025
    @GET(ApiConstant.GET_DB_LIST)
    fun getDatabaseList(): Call<DatabaseModel>

    //todo login API---
    @POST(ApiConstant.INVENTORY_GEN_EXITS)
    @Headers("Content-Type:application/json;charset=UTF-8")
    fun doGetInventoryGenExits(@Body jsonObject: JsonObject): Call<InventoryGenExitsModel>

    //todo scan item lines api...
    @GET(ApiConstant.BATCH_NUMBER_DETAILS)
    fun doGetBatchNumScanDetails(
        @Query("$" + "filter") filter: String,
    ): Call<ScanedOrderBatchedItems>

    //todo get BPL id api...
    @GET(ApiConstant.BPLID_WAREHOUSE)
    fun doGetBplID(
        @Query("$" + "select") select: String,
        @Query("$" + "filter") filter: String,
    ): Call<WarehouseBPL_IDModel>

    //todo get delivery order list..
    @GET(ApiConstant.DELIVERY_ORDER)
    fun deliveryOrder(
        /*@Header("Prefer") Prefer: String,
        @Query("$" + "filter") filter: String,
        @Query("$" + "orderby") orderby: String,*/
        @Query("\$filter") filter: String,
        @Query("\$orderby") orderby: String,
        @Query("\$skip") skip: Int,
        @Query("\$top") top: Int
    ): Call<DeliveryModel>

    //todo post delivery order items...
    @POST(ApiConstant.DELIVERY_NOTES)
    fun doGetDeliveryNotes(@Body jsonObject: JsonObject): Call<InventoryGenExitsModel>


    //todo get quantity values--
    @GET(ApiConstant.GET_QUANTITY)
    fun getQuantityValue(
        @Query("DBName") DBName: String,
        @Query("Batch") Batch: String,
        @Query("ItemCode") ItemCode: String,
        @Query("Warehouse") Warehouse: String

    ): Call<GetQuantityModel>

    @GET(ApiConstant.GET_WAREHOUSE_QUANTITY)
    fun getQuantityGoodsWithWareHouseCode(
        @Query("DBName") DBName: String,
        @Query("ItemCode") ItemCode: String,
        @Query("ItemType") itemType: String,
        @Query("BatchOrSerial") BatchOrSerial: String,

        ): Call<GetQuantityModel>

    @POST(ApiConstant.STOCK_TRANSFER)
    fun dostockTransfer(@Body jsonObject: JsonObject): Call<InventoryPostResponse>

    @GET(ApiConstant.GET_WAREHOUSE)
    fun getWarehouse(): Call<GetWarehouseModel>

    @GET(ApiConstant.GET_SERIES_INVT)
    fun getSeries(@Query("BPLID") BPLId: String): Call<GetSeriesModel>

    @GET(ApiConstant.GET_SERIES)
    fun getDocSeries(
        @Query("BPLId") BPLId: String,
        @Query("ObjCode") ObjCode: String
    ): Call<GetDocSeriesModel>

    @GET(ApiConstant.GET_ALL_BRANCHES)
    fun getBranchList(): Call<ModelGetBranch>

    @GET(ApiConstant.INVENTORY_TRANSFER_REQ)
    fun getInventoryRequestList(
        @Query("DBName") DBName: String,
        @Query("BPLId") BPLId: String,
        @Query("DocNum") docNum: String
    ): Call<InventoryRequestModel>

}