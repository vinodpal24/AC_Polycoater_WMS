package com.example.acPolycoaters.ui.deliveryOrderModule.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acPolycoaters.Global_Classes.GlobalMethods
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ScanedOrderBatchedItems
import com.example.acPolycoaters.databinding.BatchItemsScannedLayoutDeliveryBinding

class BatchItemsDeliveryAdapter(
    private val parentPosition: Int,
    private val context: Context,
    private val scanedBatchedItemsList_gl: ArrayList<ScanedOrderBatchedItems.Value>,
    private val quantityHashMap: ArrayList<String>,
    private val flag: String,
    private val tvTotalScannQty: TextView,
    private val rvBatchItems: RecyclerView,
    private val tvNoOfRolls: TextView
) : RecyclerView.Adapter<BatchItemsDeliveryAdapter.ViewHolder>() {


    private var onDeleteItemClick: OnDeleteItemClickListener? = null

    interface OnDeleteItemClickListener {
        fun onDeleteItemClick(
            parentPosition: Int,
            list: ArrayList<ScanedOrderBatchedItems.Value>,
            quantityHashMap: ArrayList<String>,
            pos: Int,
            tvTotalScannQty: TextView,
            rvBatchItems: RecyclerView,
            tvNoOfRolls: TextView
        )
    }

    interface OnDeleteItemRefreshListener {
        fun onDeleteItemRefresh(pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BatchItemsScannedLayoutDeliveryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*with(holder) {
            with(scanedBatchedItemsList_gl[position]) {
                binding.tvRollNo.text = this.Batch
                binding.tvDocEntry.text = this.DocEntry
                binding.tvItemCode.text = this.ItemCode
                binding.tvItemDesc.text = this.ItemDescription
                binding.tvWidth.text = this.U_Width.toString()
                binding.tvLength.text = this.U_Length.toString()
                binding.tvGsm.text = this.U_GSM.toString()
                binding.tvGrossWeight.text = this.U_GW.toString()

                binding.ivDelete.setOnClickListener {
                    onDeleteItemClick?.onDeleteItemClick(scanedBatchedItemsList_gl, quantityHashMap, position,tvTotalScannQty)
                }

                Log.i("SCAN_QTY", "quantityHashMap -> BatchItemsDeliveryAdapter => $quantityHashMap\nquantityHashMap.size = ${quantityHashMap.size}\nscanedBatchedItemsList_gl.size = ${scanedBatchedItemsList_gl.size}")
                with(quantityHashMap[position]){
                    val qty = GlobalMethods.changeDecimal(this)
                    Log.i("SCAN_QTY", "tvBatchQuantity [$position] -> BatchItemsDeliveryAdapter => $this ($qty)")
                    binding.tvBatchQuantity.text = qty
                }


            }
        }*/
        val item = scanedBatchedItemsList_gl[position]
        val qtyRaw = quantityHashMap[position] ?: "0.0"
        val qtyFormatted = GlobalMethods.changeDecimal(qtyRaw)

        with(holder.binding) {
            tvRollNo.text = item.Batch.orEmpty()
            tvDocEntry.text = item.DocEntry.orEmpty()
            tvItemCode.text = item.ItemCode.orEmpty()
            tvItemDesc.text = item.ItemDescription.orEmpty()
            tvWidth.text = item.U_Width.toString()
            tvLength.text = item.U_Length.toString()
            tvGsm.text = item.U_GSM.toString()
            tvGrossWeight.text = item.U_GW.toString()
            tvBatchQuantity.text = qtyFormatted

            ivDelete.setOnClickListener {
                onDeleteItemClick?.onDeleteItemClick(parentPosition, scanedBatchedItemsList_gl, quantityHashMap, position, tvTotalScannQty, rvBatchItems,tvNoOfRolls)
            }

            Log.i("SCAN_QTY", "Item [$position] → Qty Raw: $qtyRaw | Formatted: $qtyFormatted")
            Log.i("SCAN_QTY", "HashMap → $quantityHashMap")
        }

    }

    override fun getItemCount(): Int {
        return scanedBatchedItemsList_gl.size
    }

    class ViewHolder(val binding: BatchItemsScannedLayoutDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setOnDeleteItemClickListener(listener: OnDeleteItemClickListener) {
        onDeleteItemClick = listener
    }

}