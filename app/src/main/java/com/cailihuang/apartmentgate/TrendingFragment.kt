package com.cailihuang.apartmentgate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TrendingFragment: Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var trendingAdapter: TrendingListAdapter

    companion object {
        fun newInstance(): TrendingFragment {
            return TrendingFragment()
        }
    }

    private fun initAdapter(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.neighborhoodsRV)
        trendingAdapter = TrendingListAdapter(viewModel)
        rv.adapter = trendingAdapter
        rv.layoutManager = LinearLayoutManager(context)
        rv.addItemDecoration(DividerItemDecoration(rv.getContext(), DividerItemDecoration.VERTICAL))
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run {
            ViewModelProviders.of(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        val root = inflater.inflate(R.layout.fragment_trending, container, false)
        initAdapter(root)
        viewModel.populateNeighborhoods()
        viewModel.getNeighborhoods().observe(this, Observer {
            trendingAdapter.submitList(it)
        })

        return root
    }

}