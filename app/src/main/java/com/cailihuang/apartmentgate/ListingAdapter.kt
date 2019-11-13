package com.cailihuang.apartmentgate

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cailihuang.apartmentgate.api.ApartmentListing


class ListingAdapter(private val viewModel: ListViewModel)
    : ListAdapter<ApartmentListing, ListingAdapter.VH>(ApartmentDiff()) {

    class ApartmentDiff : DiffUtil.ItemCallback<ApartmentListing>() {

        override fun areItemsTheSame(oldItem: ApartmentListing, newItem: ApartmentListing): Boolean {
            return oldItem.direccion == newItem.direccion
        }

        override fun areContentsTheSame(oldItem: ApartmentListing, newItem: ApartmentListing): Boolean {
            return oldItem.nombre == newItem.nombre
                    && oldItem.beds == newItem.beds
                    && oldItem.price == newItem.price
        }
    }

    // ViewHolder pattern minimizes calls to findViewById
    inner class VH(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        var nameTextView = itemView.findViewById<TextView>(R.id.title)
        var addressTextView = itemView.findViewById<TextView>(R.id.addressTV)
        var rentTextView = itemView.findViewById<TextView>(R.id.rentTV)
        var bedTextView = itemView.findViewById<TextView>(R.id.bedTV)

        fun bind(item: ApartmentListing?) {
            if (item == null) return

            nameTextView.text = item.nombre
            addressTextView.text = item.direccion
            rentTextView.text = item.price
            bedTextView.text = item.beds

            nameTextView.setOnClickListener {
                // TODO one listing
            }

//            favView.setOnClickListener{
//                val position = adapterPosition
//                // Toggle Favorite
//                if(viewModel.isFav(getItem(position))) {
//                    viewModel.removeFav(getItem(position))
//                } else {
//                    viewModel.addFav(getItem(position))
//                }
//                notifyItemChanged(position)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_listing, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}