package com.example.points.ui.common

import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.points.AppExecutors
import com.example.points.R
import com.example.points.databinding.PointItemBinding
import com.example.points.vo.Point

/**
 * A RecyclerView adapter for [Point] class.
 */
class RepoListAdapter(
    private val dataBindingComponent: DataBindingComponent,
    appExecutors: AppExecutors,
    private val showFullName: Boolean,
    private val repoClickCallback: ((Point) -> Unit)?
) : DataBoundListAdapter<Point, PointItemBinding>(
    appExecutors = appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Point>() {
        override fun areItemsTheSame(oldItem: Point, newItem: Point): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Point, newItem: Point): Boolean {
            return oldItem.x == newItem.x
                    && oldItem.y == newItem.y
        }
    }
) {

    override fun createBinding(parent: ViewGroup): PointItemBinding {
        val binding = DataBindingUtil.inflate<PointItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.point_item,
            parent,
            false,
            dataBindingComponent
        )
        binding.showFullName = showFullName
        binding.root.setOnClickListener {
            binding.repo?.let {
                repoClickCallback?.invoke(it)
            }
        }
        return binding
    }

    override fun bind(binding: PointItemBinding, item: Point) {
        binding.repo = item
    }
}
