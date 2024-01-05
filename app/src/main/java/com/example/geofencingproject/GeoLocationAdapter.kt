package com.example.geofencingproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GeoLocationAdapter(val list: List<GeoLocation>) :
    RecyclerView.Adapter<GeoLocationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val latET: TextView = itemView.findViewById(R.id.latET)
        val lagET: TextView = itemView.findViewById(R.id.lagET)
        val radiusET: TextView = itemView.findViewById(R.id.radiusET)

        fun bind(data: GeoLocation) {
            latET.text = data.latitude
            lagET.text = data.longitude
            radiusET.text = data.radius
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.geo_location_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }


}