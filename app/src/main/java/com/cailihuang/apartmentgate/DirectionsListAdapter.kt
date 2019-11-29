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
        var detailsText = itemView.findViewById<TextView>(R.id.detailsText)
        val distanceText = itemView.findViewById<TextView>(R.id.distanceText)
        val durationText = itemView.findViewById<TextView>(R.id.durationText)
        var departureStop = itemView.findViewById<TextView>(R.id.departureStop)
        var arrivalStop = itemView.findViewById<TextView>(R.id.arrivalStop)
        var numberStops = itemView.findViewById<TextView>(R.id.numberStops)
        var transitLineDetails = itemView.findViewById<TextView>(R.id.transitLine)

        fun bind(item: DirectionsApi.Steps) {
            detailsText.text = item.html_instructions
            if (item.travel_mode == "WALKING") {
                departureStop.visibility = View.GONE
                arrivalStop.visibility = View.GONE
                numberStops.visibility = View.GONE
                transitLineDetails.visibility = View.GONE
                distanceText.text = "Distance: " + item.distance.text
                durationText.text = "Time: " + item.duration.text
                transitImage.setImageResource(R.drawable.ic_directions_walk_black_24dp)
            } else if (item.travel_mode == "TRANSIT") {
                distanceText.visibility = View.GONE
                durationText.visibility = View.GONE

                val transitDetails = item.transit_details
                val transitLine = transitDetails.line

                departureStop.text = "Departue Stop: " + transitDetails.departure_stop.name
                arrivalStop.text = "Arrival Stop: " + transitDetails.arrival_stop.name
                numberStops.text = "Number of Stops: " + transitDetails.num_stops.toString()

                var agencyName = ""
                if (transitLine.agencies[0].name == "San Francisco Municipal Transportation Agency") {
                    agencyName = "MUNI"
                } else if (transitLine.agencies[0].name == "Bay Area Rapid Transit") {
                    agencyName = "BART"
                }
                transitLineDetails.text = "Line: ".plus(agencyName).plus(" ").plus(transitLine.vehicle.name)
                        .plus(" ").plus(transitLine.name).plus(" ")
                        .plus(transitLine.short_name)

                val vehicleType = transitLine.vehicle.type
                if (vehicleType == "SUBWAY") {
                    transitImage.setImageResource(R.drawable.ic_subway_black_24dp)
                } else if (vehicleType == "BUS") {
                    transitImage.setImageResource(R.drawable.ic_directions_bus_black_24dp)
                } else if (vehicleType == "TRAM") {
                    transitImage.setImageResource(R.drawable.ic_tram_black_24dp)
                } else {
                    transitImage.setImageResource(R.drawable.ic_directions_transit_black_24dp)
                }
            }
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