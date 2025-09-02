package com.example.acPolycoaters.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acPolycoaters.Adapter.DemoAdapter
import com.example.acPolycoaters.Model.ListData
import com.example.acPolycoaters.databinding.ActivityDemoBinding

class DemoActivity : AppCompatActivity() {
    lateinit var demoBinding: ActivityDemoBinding
    var list: List<ListData> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        demoBinding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(demoBinding.root)

        demoBinding.recyclerView.layoutManager = LinearLayoutManager(this)

        val data = ArrayList<ListData>()
        for (i in 1..20) {
            data.add(ListData( "Item " + i, 0))
        }

        // This will pass the ArrayList to our Adapter
        val adapter = DemoAdapter(data)

        // Setting the Adapter with the recyclerview
        demoBinding.recyclerView.adapter = adapter

    }

    fun bindList(){
        list = arrayListOf()
    }
}