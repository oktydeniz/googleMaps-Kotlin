package com.oktydeniz.kharitalar

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomAdapter(private val placeList: ArrayList<Place>, private val context: Activity) :
    ArrayAdapter<Place>(context, R.layout.row_item, placeList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val customView = inflater.inflate(R.layout.row_item, null, true)
        val textView: TextView = customView.findViewById(R.id.placeName)
        textView.text = placeList[position].address
        return customView
    }

}