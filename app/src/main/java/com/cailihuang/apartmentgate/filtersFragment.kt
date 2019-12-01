package com.cailihuang.apartmentgate

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Button
import androidx.lifecycle.ViewModelProviders


class FiltersFragment: Fragment() {

    private lateinit var viewModel: MainViewModel

    companion object {
        fun newInstance(): FiltersFragment {
            return FiltersFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_filters, container, false)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        initializeLayoutElems(root)
        return root
    }

    private fun initializeLayoutElems(root: View) {

        val commuteTimeArrayAdapter = ArrayAdapter.createFromResource(
            context!!,
            R.array.commute_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            root.findViewById<Spinner>(R.id.maxCommuteTimeSpinner).adapter = adapter
        }

        root.findViewById<Spinner>(R.id.maxCommuteTimeSpinner).onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.resetFilters()
                val res: Resources = resources
                val commuteTimeArray = res.getStringArray(R.array.commute_array)
                viewModel.commuteTimeLimit = commuteTimeArray[position]
                viewModel.populateListings()
            }
        }

        val minSizeArrayAdapter = ArrayAdapter.createFromResource(
            context!!,
            R.array.size_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            root.findViewById<Spinner>(R.id.minSizeSpinner).adapter = adapter
        }

        root.findViewById<Spinner>(R.id.minSizeSpinner).onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.resetFilters()
                val res: Resources = resources
                val minSizeArray = res.getStringArray(R.array.size_array)
                viewModel.minSize = minSizeArray[position].toInt()
                viewModel.populateListings()
            }
        }

        val rentLimitArrayAdapterView = ArrayAdapter.createFromResource(
            context!!,
            R.array.rents_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            root.findViewById<Spinner>(R.id.maxRentSpinner).adapter = adapter
        }

        root.findViewById<Spinner>(R.id.maxRentSpinner).onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.resetFilters()
                val res: Resources = resources
                val maxRentArray = res.getStringArray(R.array.rents_array)
                if (maxRentArray[position] != "None") {
                    viewModel.rentLimit = maxRentArray[position].toInt()
                } else {
                    viewModel.rentLimit = 0
                }
                viewModel.populateListings()
            }
        }

        val minBedsArrayAdapter = ArrayAdapter.createFromResource(
                context!!,
        R.array.min_beds_array,
        android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            root.findViewById<Spinner>(R.id.minBedsSpinner).adapter = adapter
        }

        root.findViewById<Spinner>(R.id.minBedsSpinner).onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.resetFilters()
                val res: Resources = resources
                val minBedsArray = res.getStringArray(R.array.min_beds_array)
                viewModel.minBeds = minBedsArray[position].toInt()
                viewModel.populateListings()
            }
        }

        root.findViewById<Spinner>(R.id.maxCommuteTimeSpinner).setSelection(commuteTimeArrayAdapter.getPosition(viewModel.commuteTimeLimit))
        root.findViewById<Spinner>(R.id.maxRentSpinner).setSelection(rentLimitArrayAdapterView.getPosition(viewModel.rentLimit.toString()))
        root.findViewById<Spinner>(R.id.minSizeSpinner).setSelection(minSizeArrayAdapter.getPosition(viewModel.minSize.toString()))
        root.findViewById<Spinner>(R.id.minBedsSpinner).setSelection(minBedsArrayAdapter.getPosition(viewModel.minSize.toString()))

        root.findViewById<Button>(R.id.setFiltersButton).setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }

}