package com.example.acPolycoaters.Global_Classes

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ScanedOrderBatchedItems
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import es.dmoral.toasty.Toasty
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object GlobalMethods {
    const val pullRefreshTime = 2000
    open fun showMessage(context: Context?, message: String?) {
        Toasty.warning(context!!, message!!, Toast.LENGTH_SHORT).show()
    }

    open fun showError(context: Context?, message: String?) {
        Toasty.error(context!!, message!!, Toast.LENGTH_SHORT).show()
    }

    open fun showSuccess(context: Context?, message: String?){
        Toasty.success(context!!, message!!, Toast.LENGTH_SHORT).show()
    }

    //todo remove digits after decimal..
    open fun changeDecimal(input: String): String? {
        val df = DecimalFormat("#.###")
        return df.format(input.toDouble())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun getCurrentDate_dd_MM_yyyy(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }

    fun <T> toPrettyJson(data: T): String {
        val gsonPretty: Gson = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convert_yyyy_MM_dd_T_hh_mm_ss_into_ddMMYYYY(data: String): String {
        val inputDate = "2024-07-25T00:00:00"
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        // Parse the input date string to LocalDateTime
        val dateTime = LocalDateTime.parse(data)

        // Format the LocalDateTime to the desired format
        val formattedDate = dateTime.format(formatter)

        return formattedDate
    }

    fun disablePastDates(context: Context, editText: EditText, isPastDateDisable: Boolean = false) {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val s = "$dayOfMonth-${monthOfYear + 1}-$year"
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                try {
                    val strDate = dateFormatter.parse(s)
                    editText.setText(dateFormatter.format(strDate))
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }, mYear, mMonth, mDay
        )
        if (isPastDateDisable)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.setMessage(editText.hint.toString())
        datePickerDialog.show()
    }

    fun datePicker(context: Context, editText: EditText) {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val s = "$dayOfMonth-${monthOfYear + 1}-$year"
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                try {
                    val strDate = dateFormatter.parse(s)
                    editText.setText(dateFormatter.format(strDate))
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }, mYear, mMonth, mDay
        )

        //datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.setMessage(editText.hint.toString())
        datePickerDialog.show()
    }


    fun <T> toSimpleJson(data: T): String {
        val gson = Gson() // No pretty printing
        return gson.toJson(data)
    }

    fun convert_dd_MM_yyyy_into_yyyy_MM_dd(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy")
        val outputFormat = SimpleDateFormat("yyyy-MM-dd")

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun getCurrentDateFormatted(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDate.format(formatter)
    }

    open fun sumBatchQuantity(position: Int, quantityHashMap: ArrayList<String>):Double {
        var quantity = 0.000
        //TODO sum of order line batch quantities and store it in open quantity..
        var batchQuantityList: ArrayList<String>
//        batchQuantityList = quantityHashMap.get("Item" + position)!!
        batchQuantityList = quantityHashMap
        for (i in 0 until batchQuantityList.size) {
            var temp = batchQuantityList[i].toDouble()
            quantity += temp
        }
        return quantity
    }

    open fun sumBatchGrossWeight(position: Int, valueArrayList: ArrayList<ScanedOrderBatchedItems.Value>):Double {
        var quantity = 0.000
        //TODO sum of order line batch quantities and store it in open quantity..
        var batchQuantityList : ArrayList<ScanedOrderBatchedItems.Value>
        batchQuantityList = valueArrayList
        for (i in 0 until batchQuantityList.size) {
            var temp = batchQuantityList[i].U_GW
            quantity += temp
        }
        return quantity
    }

    fun handleFailureError(context: Context, t: Throwable) {
        when (t) {
            // Network-related errors
            is java.net.ConnectException -> {
                showError(context = context, "Connection failed. ${t.localizedMessage}")
                // Optionally log the full error: Log.e("NetworkError", "Connection failed", t)
            }

            is java.net.SocketTimeoutException -> {
                showError(context = context, "Request timed out. ${t.localizedMessage}")
            }

            is java.net.UnknownHostException -> {
                showError(context = context, "No internet connection or server not found. Please check your network settings.")
            }


            // Application-specific runtime errors
            is NullPointerException -> {
                // This usually indicates a programming error.
                // For production, avoid showing raw NPE to users.
                // Instead, provide a generic error and log the details for debugging.
                showError(context = context, "An unexpected error occurred. Please try again.")
                // Log.e("AppError", "Null Pointer Exception", t)
                // Optionally, send crash report to a crash analytics tool like Crashlytics
            }

            is IndexOutOfBoundsException -> {
                showError(context = context, "There was an issue processing data. Please try again.")
                // Log.e("AppError", "Index Out Of Bounds Exception", t)
            }

            is IllegalArgumentException -> {
                showError(context = context, "Invalid input provided. ${t.localizedMessage}")
                // Log.e("AppError", "Illegal Argument Exception", t)
            }

            is IllegalStateException -> {
                showError(context = context, "The application is in an unexpected state. ${t.localizedMessage}")
                // Log.e("AppError", "Illegal State Exception", t)
            }
            // Fallback for unhandled runtime errors or other Throwables
            else -> {
                // For any other unexpected error, provide a generic message.
                // Always log the full exception for debugging purposes.
                showError(context = context, "An unkown error occurred: ${t.localizedMessage}. Please try again later.")
                // Log.e("GenericError", "Unhandled exception", t)
            }
        }
    }
}