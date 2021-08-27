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

package com.android.example.github.ui.repo

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
import com.android.example.github.AppExecutors
import com.android.example.github.R
import com.android.example.github.binding.FragmentDataBindingComponent
import com.android.example.github.databinding.RepoFragmentBinding
import com.android.example.github.di.Injectable
import com.android.example.github.ui.common.RepoListAdapter
import com.android.example.github.ui.search.SearchFragmentDirections
import com.android.example.github.util.autoCleared
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
class RepoFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val repoViewModel: RepoViewModel by viewModels {
        viewModelFactory
    }

    @Inject
    lateinit var appExecutors: AppExecutors

    // mutable for testing
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    var binding by autoCleared<RepoFragmentBinding>()

    private val params by navArgs<RepoFragmentArgs>()
    var adapter by autoCleared<RepoListAdapter>()

    private fun initPointList(viewModel: RepoViewModel) {
        viewModel.points.observe(viewLifecycleOwner, Observer { listResource ->
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            adapter.submitList(listResource)
            val entries: MutableList<Entry> = ArrayList()
            for (data in listResource) {
                // turn your data into Entry objects
                entries.add(Entry(data.x, data.y))
            }
            val dataSet: LineDataSet =  LineDataSet(entries, "Label") // add entries to dataset
//            dataSet.setColor(...);
//            dataSet.setValueTextColor(...); // styling, ...
            val lineData = LineData(dataSet)
            binding.chart1.setData(lineData)
            binding.chart1.invalidate() // refresh

        })
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
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
            else -> super.onOptionsItemSelected(item)
        }
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
        repoViewModel.setId(params.points)
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

        initPointList(repoViewModel)
    }

    private fun initRecyclerView() {


    }

}
