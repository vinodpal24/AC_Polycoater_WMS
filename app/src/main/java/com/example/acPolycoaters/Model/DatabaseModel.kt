package com.example.acPolycoaters.Model


import com.google.gson.annotations.SerializedName

data class DatabaseModel(
    @SerializedName("error")
    val error: String,
    @SerializedName("value")
    val value: ArrayList<Value>
) {
    data class Value(
        @SerializedName("cmpName")
        val cmpName: String,
        @SerializedName("dbname")
        val dbname: String
    )
}