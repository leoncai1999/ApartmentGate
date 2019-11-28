package com.cailihuang.apartmentgate

import android.app.Application
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cailihuang.apartmentgate.api.ApartmentListing


class ListingAdapter(private val viewModel: MainViewModel) : ListAdapter<ApartmentListing, ListingAdapter.VH>(ApartmentDiff()) {

    class ApartmentDiff : DiffUtil.ItemCallback<ApartmentListing>() {

        override fun areItemsTheSame(oldItem: ApartmentListing, newItem: ApartmentListing): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: ApartmentListing, newItem: ApartmentListing): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.bds == newItem.bds
                    && oldItem.rent == newItem.rent
        }
    }

    inner class VH(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        var nameTextView = itemView.findViewById<TextView>(R.id.title)
        var addressTextView = itemView.findViewById<TextView>(R.id.addressTV)
        var rentTextView = itemView.findViewById<TextView>(R.id.rentTV)
        var bedTextView = itemView.findViewById<TextView>(R.id.bedTV)
        var commuteTextView = itemView.findViewById<TextView>(R.id.commuteTimeTV)

        fun bind(item: ApartmentListing?) {
            if (item == null) return

            nameTextView.text = item.name
            addressTextView.text = item.address
            rentTextView.text = item.rent
            bedTextView.text = item.bds
            commuteTextView.text = viewModel.commuteTimes.get(item.address)?.text

            nameTextView.setOnClickListener {
                (it.context as MainActivity).setFragment(OneListingFragment.newInstance(item))
            }

            // favorites
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