package com.example.acPolycoaters.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.acPolycoaters.Model.ListData
import com.example.acPolycoaters.databinding.DemoLayoutBinding
import java.lang.String
import kotlin.Int
import kotlin.with

class DemoAdapter(var list: ArrayList<ListData>): RecyclerView.Adapter<DemoAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DemoLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DemoAdapter.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
      return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.textView.text = this.name

                binding.total.setText(String.valueOf(this.quantity));

                binding.minus.setOnClickListener {
                    if (this.quantity > 0) {
                        this.quantity--
                        binding.total.setText(String.valueOf( this.quantity))
                    }
                }

                binding.plus.setOnClickListener {
                    quantity++
                    binding.total.setText(String.valueOf( this.quantity))
                }


            }
        }
    }

    class ViewHolder(val binding: DemoLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}