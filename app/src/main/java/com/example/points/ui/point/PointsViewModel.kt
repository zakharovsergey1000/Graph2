package com.example.points.ui.point

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.points.repository.PointsRepository
import com.example.points.testing.OpenForTesting
import com.example.points.util.AbsentLiveData
import com.example.points.vo.Point
import com.github.mikephil.charting.data.LineDataSet
import javax.inject.Inject

@OpenForTesting
class PointsViewModel @Inject constructor(repository: PointsRepository) : ViewModel() {
    lateinit var dataSet: LineDataSet
    var listResource: List<Point>? = null
    private val _pointId: MutableLiveData<String> = MutableLiveData()
    val pointId: LiveData<String>
        get() = _pointId

    val points: LiveData<List<Point>> =_pointId.switchMap { input ->
        repository.loadPoints(input)
    }

    fun setId(owner: String) {
        if (_pointId.value != owner) {
            _pointId.value = owner
        }
    }

    data class RepoId(val owner: String, val name: String) {
        fun <T> ifExists(f: (String, String) -> LiveData<T>): LiveData<T> {
            return if (owner.isBlank() || name.isBlank()) {
                AbsentLiveData.create()
            } else {
                f(owner, name)
            }
        }
    }
}
