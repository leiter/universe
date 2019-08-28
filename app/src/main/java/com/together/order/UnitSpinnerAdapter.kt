package com.together.order

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.together.R
import com.together.base.UiState

class UnitSpinnerAdapter(context: Context, var data: List<UiState.Unit>) : ArrayAdapter<UiState.Unit>(context, R.layout.item_unit){


    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_unit,parent,false)
        v.findViewById<TextView>(R.id.unit_head).text = data[position].id
        v.findViewById<TextView>(R.id.unit_sub).text = data[position].amount

        return v
    }
}