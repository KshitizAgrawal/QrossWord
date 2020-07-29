package com.kshitiz.crosswords

import RecyclerViewAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CrossWordSeriesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recyclerview_item)

        val dataList = ArrayList<Data>()

        for(i in 1 until 8) {
            val viewType = i%2
            dataList.add(Data(viewType, "QrossWord $i"))
        }

        val adapter = RecyclerViewAdapter(this, dataList)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

}