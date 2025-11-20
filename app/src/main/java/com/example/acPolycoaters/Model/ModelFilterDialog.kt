package com.example.acPolycoaters.Model


import com.google.gson.annotations.SerializedName

/**
{
"Dependency": "No",
"Status": "Info Received",
"DirectionType": "Outgoing",
"Direction": "Billing Done"
}
 */
data class ModelFilterDialog(
      @SerializedName("dateFrom")
      var dateFrom: String = "",
      @SerializedName("dateTo")
      var dateTo: String = ""
) {
      fun clear() {
            dateFrom = ""
            dateTo = ""
      }
}