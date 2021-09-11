package com.example.points.ui.point

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.example.points.AppExecutors
import com.example.points.R
import com.example.points.binding.FragmentDataBindingComponent
import com.example.points.databinding.PointsFragmentBinding
import com.example.points.di.Injectable
import com.example.points.ui.common.PointListAdapter
import com.example.points.ui.search.SearchFragmentDirections
import com.example.points.util.autoCleared
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
    var binding by autoCleared<PointsFragmentBinding>()

    private val params by navArgs<PointsFragmentArgs>()
    var adapter by autoCleared<PointListAdapter>()

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                saveToPicture()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Snackbar.make(
                    binding.chart1,
                    getString(R.string.permission_request_to_write_to_external_storage),
                    3000
                ).show()
            }
        }

    fun showRequestPermissionRationaleDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog = RequestPermissionRationaleDialogFragment()
        dialog.show(parentFragmentManager, "RequestPermissionRationaleDialogFragment")
    }

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
                    viewModel.dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                    viewModel.dataSet.cubicIntensity = 0.2f
                } else {
                    viewModel.dataSet.mode = LineDataSet.Mode.LINEAR
                }

//            dataSet.setColor(...);
//            dataSet.setValueTextColor(...); // styling, ...
            }
            adapter.submitList(viewModel.listResource)
            val lineData = LineData(viewModel.dataSet)
            binding.chart1.data = lineData
            binding.chart1.invalidate() // refresh
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener("requestKey", { requestKey: String, bundle: Bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getInt("bundleKey")
            // Do something with the result
            when (result) {
                R.string.ok -> {
                    requestPermission()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<PointsFragmentBinding>(
            inflater,
            R.layout.points_fragment,
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
        val rvAdapter = PointListAdapter(
            dataBindingComponent = dataBindingComponent,
            appExecutors = appExecutors,
            showFullName = true,
            null
        )
        binding.pointList.adapter = rvAdapter
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
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // You can use the API that requires the permission.
                        saveToPicture()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                        // In an educational UI, explain to the user why your app requires this
                        // permission for a specific feature to behave as expected. In this UI,
                        // include a "cancel" or "no thanks" button that allows the user to
                        // continue using your app without granting the permission.
                        showRequestPermissionRationaleDialog()

                    }
                    else -> {
                        // You can directly ask for the permission.
                        // The registered ActivityResultCallback gets the result of this request.
                        requestPermission()
                    }
                }
                true
            }
            R.id.toggle_basic_cubic -> {
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                if (sharedPref!!.getInt(getString(R.string.line_data_set_mode_key), LineDataSet.Mode.LINEAR.ordinal) == LineDataSet.Mode.LINEAR.ordinal) {
                    with (sharedPref.edit()) {
                        putInt(getString(R.string.line_data_set_mode_key), LineDataSet.Mode.CUBIC_BEZIER.ordinal)
                        apply()
                    }
                    pointsViewModel.dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                    pointsViewModel.dataSet.cubicIntensity = 0.2f
                } else {
                    with (sharedPref.edit()) {
                        putInt(getString(R.string.line_data_set_mode_key), LineDataSet.Mode.LINEAR.ordinal)
                        apply()
                    }
                    pointsViewModel.dataSet.mode = LineDataSet.Mode.LINEAR
                }
                val lineData = LineData(pointsViewModel.dataSet)
                binding.chart1.data = lineData
                binding.chart1.invalidate() // refresh
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun initRecyclerView() {


    }

}
