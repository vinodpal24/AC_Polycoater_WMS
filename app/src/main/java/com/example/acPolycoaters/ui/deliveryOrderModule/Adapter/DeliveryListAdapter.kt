package com.example.acPolycoaters.ui.deliveryOrderModule.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.acPolycoaters.databinding.DeliveryOrderListAdapterLayoutBinding
import com.example.acPolycoaters.ui.deliveryOrderModule.Model.DeliveryModel
import com.example.acPolycoaters.ui.issueForProductionOrder.Model.ProductionListModel

class DeliveryListAdapter(private var deliveryModelList: List<DeliveryModel.Value>, private val callBack: ((List<DeliveryModel.Value>, pos: Int) -> Unit)) : RecyclerView
.Adapter<DeliveryListAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: DeliveryOrderListAdapterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeliveryModel.Value, position: Int) = with(binding) {
            tvDocNo.text = item.DocNum
            tvCardName.text = item.CardName
            cardItemView.setOnClickListener {
                callBack(deliveryModelList, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DeliveryOrderListAdapterLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(deliveryModelList[position], position)
    }

    override fun getItemCount(): Int = deliveryModelList.size

    //todo filter search list call this function whenever the search query changes and list update..

    fun setFilteredItems(filteredItems: ArrayList<DeliveryModel.Value>) {
        deliveryModelList = filteredItems
        notifyDataSetChanged()
    }

}