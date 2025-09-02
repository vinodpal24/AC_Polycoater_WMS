package com.example.acPolycoaters.ui.issueForProductionOrder.Adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.acPolycoaters.Global_Classes.GlobalMethods
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ScanedOrderBatchedItems
import com.example.acPolycoaters.databinding.BatchItemsScannedLayoutBinding
import com.example.acPolycoaters.databinding.BatchItemsScannedLayoutIssueProdBinding
import com.google.android.material.textfield.TextInputEditText

class BatchItemsAdapter(
    private val context: Context,
    private val scanedBatchedItemsList_gl: ArrayList<ScanedOrderBatchedItems.Value>,
    private val quantityHashMap: ArrayList<String>,
    private var parentPosition: Int,
    private var rvBatchItems: RecyclerView,
    private var openQty: Double,
    private var onTextChanged: (String, Int, EditText) -> Unit
) : RecyclerView.Adapter<BatchItemsAdapter.ViewHolder>() {


    private var onDeleteItemClick: OnDeleteItemClickListener? = null

    interface OnDeleteItemClickListener {
        fun onDeleteItemClick(list: ArrayList<ScanedOrderBatchedItems.Value>, quantityHashMap: ArrayList<String>, pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BatchItemsScannedLayoutIssueProdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(scanedBatchedItemsList_gl[position]) {
                binding.tvRollNo.text = this.Batch
                binding.tvDocEntry.text = this.DocEntry
                binding.tvItemCode.text = this.ItemCode
                binding.tvItemDesc.text = this.ItemDescription
                binding.tvWidth.text = this.U_Width.toString()
                binding.tvLength.text = this.U_Length.toString()
                binding.tvGsm.text = this.U_GSM.toString()
                binding.tvGrossWeight.text = this.U_GW.toString()
                binding.etQuantity.setText(openQty.toString())
                binding.ivDelete.setOnClickListener {
                    onDeleteItemClick?.onDeleteItemClick(scanedBatchedItemsList_gl, quantityHashMap, position)
                }


                binding.etQuantity.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                        quantityHashMap[adapterPosition] = charSequence.toString()
                        onTextChanged(charSequence.toString(), adapterPosition, binding.etQuantity)

                    }

                    override fun afterTextChanged(editable: Editable) {}
                })


            }
        }
    }

    override fun getItemCount(): Int {
        return scanedBatchedItemsList_gl.size
    }

    class ViewHolder(val binding: BatchItemsScannedLayoutIssueProdBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setOnDeleteItemClickListener(listener: OnDeleteItemClickListener) {
        onDeleteItemClick = listener
    }

}