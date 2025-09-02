package com.example.acPolycoaters.Model

class GetQuantityModel (
    var value: List<Value>,
){
    data class Value(
        var Quantity: String,
    )

}