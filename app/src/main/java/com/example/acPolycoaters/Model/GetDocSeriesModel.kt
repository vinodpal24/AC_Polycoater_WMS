package com.example.acPolycoaters.Model

data class GetDocSeriesModel(
    val value: List<Value>
) {
    data class Value(
        val Indicator: String,
        val Series: String,
        val SeriesName: String
    )
}