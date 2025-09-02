package com.example.acPolycoaters.Global_Classes

import com.example.acPolycoaters.BuildConfig
import com.example.acPolycoaters.ui.inventoryTransferStandalone.model.LocalListForGoods

object AppConstants {
    //todo api response fields keys store..o
    const val SESSION_TIMEOUT = "SessionTimeout"
    const val SESSION_ID = "SessionId"
    var IS_SCAN  = false
    var isTestEnvUIVisible = BuildConfig.IS_DEVELOPMENT // true if test env ui is visible else false
    var isTestForClient = BuildConfig.IS_DEVELOPMENT_CLIENT // true if test or false if live for client
    var BPLID  = "_BPLID"
    var IS_TEST_ENVIRONMENT="_IS_TEST_ENVIRONMENT"
    var FromWhere = "Login"
    var WHAREHOUSE = "Warehouse"
    var SCANNER_CHECK = "scanner_check"
    var LEASER_CHECK = "leaser_check"
    const val isFirstTime = "false"
    var SCANNER_TYPE  = "scanner_type"
    var USER_PASSWORD  = "Password"
    var USER_NAME  = "UserName"

    var ISSUE_FOR_PRODUCTION = "ISSUE_FOR_PRODUCTION"
    var SCAN_AND_VIEW = "SCAN_AND_VIEW"
    var INVENTORY_REQ = "INVENTORY_REQ"
    var GOODS_ISSUE ="GOODS_ISSUE"
    var INVENTORY_TRANSFER_GRPO="INVENTORY_TRANSFER_GRPO"
    var GOODS_RECEIPT_PO = "GOODS_RECEIPT_PO"
    var SALE_TO_INVOICE ="SALE_TO_INVOICE"
    var RECEIPT_FROM_PRODUCTION ="RECEIPT_FROM_PRODUCTION"
    var PICK_LIST = "PICK_LIST"
    var RETURN_COMPONENTS ="RETURN_COMPONENTS"
    var GOODS_RECEIPT = "GOODS_RECEIPT"
    var INVENTORY_TRANSFER_STANDALONE = "INVENTORY_TRANSFER_STANDALONE"
    var SALE_TO_DELIVERY="SALE_TO_DELIVERY"

    //todo SQL server credentials..
    const val IP = "220.158.165.54"

    //todo Test
   // const val IP = "103.194.8.40"
    const val PORT = "1433"

    //todo Live

    var scannedItemForGood = mutableListOf<LocalListForGoods>()


//    const val PORT = "65430"

//    const val COMPANY_DB = "Innotex_Live_Databases"
    const val COMPANY_DB = "ACPL"
//    const val COMPANY_DB = "TEST_ACPL_27062024"
   // const val COMPANY_DB = "Innotex_Test"  //17122022 -> 14042023 -->  TEST_25042023  //Test
//    const val USERNAME = "sa"
//    const val PASSWORD: String = "$" + "V$9Y$&5E$&Z"

    const val USERNAME = "B1ADMIN"
    const val PASSWORD: String = "Cinntra#@123"
    //const val PASSWORD: String = "SqLSrvR@190923"
    const val Classes = "net.sourceforge.jtds.jdbc.Driver"

}