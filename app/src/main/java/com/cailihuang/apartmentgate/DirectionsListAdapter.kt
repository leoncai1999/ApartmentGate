package com.cailihuang.apartmentgate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cailihuang.apartmentgate.api.DirectionsApi

class DirectionsListAdapter(private val viewModel: MainViewModel): RecyclerView.Adapter<DirectionsListAdapter.VH>() {

    private var directionsList = viewModel.observeDirections().value

    inner class VH(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        var transitImage = itemView.findViewById<ImageView>(R.id.transitImage)
        var directionsText = itemView.findViewById<TextView>(R.id.directionsText)
        var distanceText = itemView.findViewById<TextView>(R.id.distanceText)
        var timeText = itemView.findViewById<TextView>(R.id.timeText)

        fun bind(item: DirectionsApi.Steps) {
            if (item.travel_mode == "WALKING") {
                transitImage.setImageResource(R.drawable.ic_directions_walk_black_24dp)
            } else if (item.travel_mode == "TRANSIT") {
                transitImage.setImageResource(R.drawable.ic_directions_transit_black_24dp)
            } else if (item.travel_mode == "DRIVING") {
                transitImage.setImageResource(R.drawable.ic_directions_car_black_24dp)
            }
            directionsText.text = item.html_instructions
            distanceText.text = "Distance: " + item.distance.text
            timeText.text = "Time: " + item.duration.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_directions, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(directionsList!![holder.adapterPosition])
    }

    fun submitList(items: List<DirectionsApi.Steps>) {
        directionsList = items
        notifyDataSetChanged()
    }

    override fun getItemCount() = directionsList!!.size

}