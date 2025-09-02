package com.example.acPolycoaters.ui.inventoryTransferStandalone.model

data class GetSeriesModel(
    val value: ArrayList<Value>
) {
    data class Value(
        val Series: String,
        val SeriesName: String
    )
}