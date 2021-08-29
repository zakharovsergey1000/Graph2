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
    private val _repoId: MutableLiveData<String> = MutableLiveData()
    val repoId: LiveData<String>
        get() = _repoId

    val points: LiveData<List<Point>> =_repoId.switchMap { input ->
        repository.loadPoints(input)
    }

    fun setId(owner: String) {
        if (_repoId.value != owner) {
            _repoId.value = owner
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
