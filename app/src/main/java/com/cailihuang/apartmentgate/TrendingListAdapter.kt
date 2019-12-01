package com.cailihuang.apartmentgate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cailihuang.apartmentgate.glide.Glide

class TrendingListAdapter(private val viewModel: MainViewModel): RecyclerView.Adapter<TrendingListAdapter.VH>() {

    private var neighborhoodsList = viewModel.getNeighborhoods().value

    inner class VH(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        var neighborhoodImage = itemView.findViewById<ImageView>(R.id.neighborhoodImage)
        var negihborhoodName = itemView.findViewById<TextView>(R.id.neighborhoodName)
        var neighborhoodRent = itemView.findViewById<TextView>(R.id.neighborhoodRent)

        fun bind(item: Neighborhood) {
            Glide.glideFetch(item.image_url, neighborhoodImage)
            negihborhoodName.text = item.name
            neighborhoodRent.text = "Average Rent: $" + item.average_rent.toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_neighborhood, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(neighborhoodsList!![holder.adapterPosition])
    }

    fun submitList(items: List<Neighborhood>) {
        neighborhoodsList = items
        notifyDataSetChanged()
    }

    override fun getItemCount() = neighborhoodsList!!.size

}