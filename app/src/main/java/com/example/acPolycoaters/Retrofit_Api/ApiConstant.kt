package com.example.acPolycoaters.Retrofit_Api

object ApiConstant {
    //    internal const val BASE_URL = "http://103.107.67.172:50001/b1s/v1/"
//    internal const val QUANTITY_BASE_URL = "http://103.107.67.172:8000/ACPOLY/"
    internal const val BASE_URL = "http://103.197.76.73:50001/b1s/v1/"
    /*internal const val QUANTITY_BASE_URL = "http://103.197.76.73:8000/ACPOLY/"
    internal const val QUANTITY_BASE_URL_NEW = "http://103.197.76.72:9090/api/"*/

    internal const val LOGIN = "Login"
    internal const val LOGOUT = "Logout"

    //todo ISSUE ORDER API..
    internal const val PRODUCTION_ORDERS = "ProductionOrders"
    internal const val PRODUCTION_ORDER = "Values/Production"

    internal const val INVENTORY_GEN_EXITS = "InventoryGenExits"
    internal const val INVENTORY_TRANSFER_REQ = "InventroyTransferRequestList"  //  InventroyTransferRequestList

    internal const val BATCH_NUMBER_DETAILS = "BatchNumberDetails"
    internal const val BPLID_WAREHOUSE = "Warehouses"

    //todo DELIVERY ORDER API..
    internal const val DELIVERY_ORDER = "Orders"
    internal const val DELIVERY_NOTES = "DeliveryNotes"


    const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"

    //const val GET_QUANTITY = "General/GetQuantity.xsjs?"
    const val GET_QUANTITY = "GetQuantity"
    const val GET_WAREHOUSE_QUANTITY = "WareHouseQuantity/WareHouseQuantity?"

    internal const val STOCK_TRANSFER = "StockTransfers"
    internal const val GET_WAREHOUSE = "GetWareHouse"
    internal const val GET_SERIES_INVT = "InvtSeries"
    internal const val GET_SERIES = "GetSeries"

    /****** Branch  *****/ // added by Vinod Pal @21Apr,2025
    internal const val GET_ALL_BRANCHES = "GetBranchList"
    internal const val GET_DB_LIST = "DBList"

}