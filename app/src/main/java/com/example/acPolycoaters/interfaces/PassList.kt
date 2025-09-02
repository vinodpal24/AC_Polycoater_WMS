package com.example.acPolycoaters.interfaces

import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ScanedOrderBatchedItems

interface PassList {
    fun passList(dataList : List<ScanedOrderBatchedItems.Value>)
}