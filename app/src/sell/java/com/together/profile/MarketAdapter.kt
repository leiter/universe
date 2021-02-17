package com.together.profile

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.together.R
import com.together.base.UiState

class MarketAdapter(context: Context,
                    private val markets: MutableList<UiState.Market>,
                    private val openDialog: (UiState.Market) -> Unit) :
    ArrayAdapter<UiState.Market>(context, R.layout.item_market, markets) {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = LayoutInflater.from(context).inflate(R.layout.item_market, parent, false)
        v.findViewById<TextView>(R.id.place_name).text = markets[position].name
        v.findViewById<TextView>(R.id.street).text = markets[position].street
        v.findViewById<TextView>(R.id.house).text = markets[position].houseNumber
        v.findViewById<TextView>(R.id.zip_code).text = markets[position].zipCode
        v.findViewById<TextView>(R.id.city).text = markets[position].city
        v.findViewById<TextView>(R.id.weekdays).text = markets[position].dayOfWeek
        v.findViewById<TextView>(R.id.begin).text = markets[position].begin
        v.findViewById<TextView>(R.id.end).text = markets[position].end
        v.findViewById<MaterialButton>(R.id.edit_pickup_place).setOnClickListener { openDialog(markets[position]) }
        return v
    }


}