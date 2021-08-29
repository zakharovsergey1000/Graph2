/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.points.ui.point

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.android.example.points.AppExecutors
import com.android.example.points.R
import com.android.example.points.binding.FragmentDataBindingComponent
import com.android.example.points.databinding.RepoFragmentBinding
import com.android.example.points.di.Injectable
import com.android.example.points.ui.common.RepoListAdapter
import com.android.example.points.ui.search.SearchFragmentDirections
import com.android.example.points.util.autoCleared
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.snackbar.Snackbar
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


/**
 * The UI Controller for displaying a Github Repo's information with its contributors.
 */
class PointsFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val pointsViewModel: PointsViewModel by viewModels {
        viewModelFactory
    }

    @Inject
    lateinit var appExecutors: AppExecutors

    // mutable for testing
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    var binding by autoCleared<RepoFragmentBinding>()

    private val params by navArgs<PointsFragmentArgs>()
    var adapter by autoCleared<RepoListAdapter>()

    private fun initPointList(viewModel: PointsViewModel) {
        viewModel.points.observe(viewLifecycleOwner, Observer { listResource ->
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (viewModel.listResource != listResource) {
                viewModel.listResource = listResource
                val entries: MutableList<Entry> = ArrayList()
                for (data in listResource) {
                    // turn your data into Entry objects
                    entries.add(Entry(data.x, data.y))
                }
                viewModel.dataSet = LineDataSet(entries, "Label") // add entries to dataset
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                if (sharedPref!!.getInt(
                        getString(R.string.line_data_set_mode_key),
                        LineDataSet.Mode.LINEAR.ordinal
                    ) == LineDataSet.Mode.CUBIC_BEZIER.ordinal
                ) {
                    viewModel.dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER)
                    viewModel.dataSet.setCubicIntensity(0.2f)
                } else {
                    viewModel.dataSet.setMode(LineDataSet.Mode.LINEAR)
                }

//            dataSet.setColor(...);
//            dataSet.setValueTextColor(...); // styling, ...
            }
            adapter.submitList(viewModel.listResource)
            val lineData = LineData(viewModel.dataSet)
            binding.chart1.setData(lineData)
            binding.chart1.invalidate() // refresh
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<RepoFragmentBinding>(
            inflater,
            R.layout.repo_fragment,
            container,
            false
        )
        binding = dataBinding
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.move)
        return dataBinding.root
    }

    private fun saveToPicture() {
        val dateTimeStr = DateFormat.format("yyyyMMdd_hhmmss", Date())
        val fileName = "points_" + dateTimeStr
        var success = binding.chart1.saveToGallery(fileName)
        if (success) {
            Snackbar.make(binding.chart1, "Saved as: " + fileName, 3000).show()
        } else {
            Snackbar.make(binding.chart1, "Unable to save", 3000).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        pointsViewModel.setId(params.points)
        binding.lifecycleOwner = viewLifecycleOwner
        val rvAdapter = RepoListAdapter(
            dataBindingComponent = dataBindingComponent,
            appExecutors = appExecutors,
            showFullName = true
        ) { repo ->
            findNavController().navigate(
                SearchFragmentDirections.showRepo(repo.count.toString())
            )
        }
        binding.repoList.adapter = rvAdapter
        adapter = rvAdapter

        initPointList(pointsViewModel)
    }

    override fun onCreateOptionsMenu(menu: Menu,  menuInflater:MenuInflater) {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.points, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.save -> {
                saveToPicture()
                true
            }
            R.id.toggle_basic_cubic -> {
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                if (sharedPref!!.getInt(getString(R.string.line_data_set_mode_key), LineDataSet.Mode.LINEAR.ordinal) == LineDataSet.Mode.LINEAR.ordinal) {
                    with (sharedPref.edit()) {
                        putInt(getString(R.string.line_data_set_mode_key), LineDataSet.Mode.CUBIC_BEZIER.ordinal)
                        apply()
                    }
                    pointsViewModel.dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER)
                    pointsViewModel.dataSet.setCubicIntensity(0.2f)
                } else {
                    with (sharedPref.edit()) {
                        putInt(getString(R.string.line_data_set_mode_key), LineDataSet.Mode.LINEAR.ordinal)
                        apply()
                    }
                    pointsViewModel.dataSet.setMode(LineDataSet.Mode.LINEAR)
                }
                val lineData = LineData(pointsViewModel.dataSet)
                binding.chart1.setData(lineData)
                binding.chart1.invalidate() // refresh
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerView() {


    }

}
