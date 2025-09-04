package com.example.acPolycoaters.ui.inventoryTransferRequest.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.acPolycoaters.Global_Classes.GlobalMethods
import com.example.acPolycoaters.databinding.IssueOrderListAdapterLayoutBinding
import com.example.acPolycoaters.ui.inventoryTransferRequest.model.InventoryRequestModel
import kotlin.collections.ArrayList

class InventoryRequestAdapter(var list: ArrayList<InventoryRequestModel.Value>) :
    RecyclerView.Adapter<InventoryRequestAdapter.ViewHolder>() {

    //TODO comment interface declare...
    private var onItemClickListener: ((List<InventoryRequestModel.Value>, pos: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = IssueOrderListAdapterLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.tvItemNoLabel.text = "Document Date"
                binding.tvProductionNo.text = this.DocNum

                /*binding.docNum.text = "Doc Entry  : "
                binding.tvProd.text = this.DocEntry*/

                binding.tvProd.text =
                    GlobalMethods.convert_yyyy_MM_dd_T_hh_mm_ss_into_ddMMYYYY(this.DocDate)

                //TODO comment interface...
                binding.cvListItem.setOnClickListener {
                    onItemClickListener?.let { click ->
                        click(list, position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class ViewHolder(val binding: IssueOrderListAdapterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun OnItemClickListener(listener: (List<InventoryRequestModel.Value>, pos: Int) -> Unit) {
        onItemClickListener = listener
    }

    //todo filter search list call this function whenever the search query changes and list update..

    fun setFilteredItems(filteredItems: ArrayList<InventoryRequestModel.Value>) {
        list = filteredItems
        notifyDataSetChanged()
    }

    fun clearItems() {
        list.clear()
        Log.e("Clear==>", "" + list.size)
        notifyDataSetChanged()
    }

}