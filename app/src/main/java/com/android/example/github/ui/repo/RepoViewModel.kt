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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.android.example.github.repository.RepoRepository
import com.android.example.github.testing.OpenForTesting
import com.android.example.github.util.AbsentLiveData
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import javax.inject.Inject

@OpenForTesting
class RepoViewModel @Inject constructor(repository: RepoRepository) : ViewModel() {
    private val _repoId: MutableLiveData<String> = MutableLiveData()
    val repoId: LiveData<String>
        get() = _repoId

    val points: LiveData<List<Repo>> =_repoId.switchMap { input ->
        repository.loadRepos(input)
    }

    fun setId(owner: String) {
        _repoId.value = owner
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
