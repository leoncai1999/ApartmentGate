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
import java.util.concurrent.Semaphore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class ListingAdapter(private val viewModel: MainViewModel) : ListAdapter<ApartmentListing, ListingAdapter.VH>(ApartmentDiff()) {

    class ApartmentDiff : DiffUtil.ItemCallback<ApartmentListing>() {

        override fun areItemsTheSame(oldItem: ApartmentListing, newItem: ApartmentListing): Boolean {
            return oldItem.address1 == newItem.address1
        }

        override fun areContentsTheSame(oldItem: ApartmentListing, newItem: ApartmentListing): Boolean {
            return oldItem.about == newItem.about
                    && oldItem.beds == newItem.beds
                    && oldItem.rent == newItem.rent
        }
    }

    inner class VH(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        var nameTextView = itemView.findViewById<TextView>(R.id.title)
        var addressTextView = itemView.findViewById<TextView>(R.id.addressTV)
        var rentTextView = itemView.findViewById<TextView>(R.id.rentTV)
        var bedTextView = itemView.findViewById<TextView>(R.id.bedTV)
        var sizeTextView = itemView.findViewById<TextView>(R.id.sizeTV)
        var commuteTextView = itemView.findViewById<TextView>(R.id.commuteTimeTV)
        var favView = itemView.findViewById<ImageView>(R.id.rowFav)

        fun bind(item: ApartmentListing?) {
            if (item == null) return

            nameTextView.text = item.address1
            addressTextView.text = item.address2
            rentTextView.text = item.rent.toString()
            bedTextView.text = item.beds.toString()
            if (bedTextView.text == "0") {
                bedTextView.text = "Studio"
            }
            sizeTextView.text = """${item.size} sq ft"""
            commuteTextView.text = viewModel.commuteTimes.get(item.address1)?.text

            //println("LIST ADAPTER ITEM --- " + item.name)

            if (viewModel.isFav(item)) {
                println("ITEM IS FAV --- " + item.address1)
                favView.setImageResource(R.drawable.ic_favorite_black_24dp)
            }

            nameTextView.setOnClickListener {
                (it.context as MainActivity).setFragment(OneListingFragment.newInstance(item))
            }

            // favorites
            favView.setOnClickListener{
                val position = adapterPosition
                val listing = getItem(position)
                // Toggle Favorite
                if (viewModel.isFav(listing)) {
                    println("REMOVE FAV ITEM --- " + item.address1)

                    viewModel.removeFav(listing)
                    viewModel.updateTrendingNeighborhoods(listing, true)
                } else {
                    println("ADD FAV ITEM --- " + item.address1)

                    viewModel.addFav(listing)
                    viewModel.updateTrendingNeighborhoods(listing, false)
                }
                notifyItemChanged(position)
            }
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