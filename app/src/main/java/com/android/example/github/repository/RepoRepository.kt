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

package com.android.example.github.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.android.example.github.AppExecutors
import com.android.example.github.api.ApiResponse
import com.android.example.github.api.ApiSuccessResponse
import com.android.example.github.api.GithubService
import com.android.example.github.api.RepoSearchResponse
import com.android.example.github.db.GithubDb
import com.android.example.github.db.RepoDao
import com.android.example.github.testing.OpenForTesting
import com.android.example.github.util.AbsentLiveData
import com.android.example.github.util.RateLimiter
import com.android.example.github.vo.Repo
import com.android.example.github.vo.RepoSearchResult
import com.android.example.github.vo.Resource
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles Repo instances.
 *
 * unfortunate naming :/ .
 * Repo - value object name
 * Repository - type of this class.
 */
@Singleton
@OpenForTesting
class RepoRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val db: GithubDb,
    private val repoDao: RepoDao,
    private val githubService: GithubService
) {

    private val repoListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun loadRepos(owner: String): LiveData<Resource<List<Repo>>> {
        return object : NetworkBoundResource<List<Repo>, List<Repo>>(appExecutors) {
            override fun saveCallResult(item: List<Repo>) {
                repoDao.insertRepos(item)
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return data == null || data.isEmpty() || repoListRateLimit.shouldFetch(owner)
            }

            override fun loadFromDb() = repoDao.loadRepositories(owner)

            override fun createCall(): LiveData<ApiResponse<List<Repo>>> {
                throw Exception("Not implemented")
            }

            override fun onFetchFailed() {
                repoListRateLimit.reset(owner)
            }
        }.asLiveData()
    }

    fun searchNextPage(query: String): LiveData<Resource<Boolean>> {
        val fetchNextSearchPageTask = FetchNextSearchPageTask(
            query = query,
            githubService = githubService,
            db = db
        )
        appExecutors.networkIO().execute(fetchNextSearchPageTask)
        return fetchNextSearchPageTask.liveData
    }

    fun addRepos(repos: List<Repo>): List<Repo> {
        val ids = repoDao.insertRepos(repos)
        return repoDao.getReposFromRowids(ids)
    }

    fun search(query: String): LiveData<Resource<List<Repo>>> {
        return object : NetworkBoundResource<List<Repo>, RepoSearchResponse>(appExecutors) {

            override fun saveCallResult(item: RepoSearchResponse) {
                val count = item.items.count()
                item.items.forEach { repo ->  repo.count = count}
                db.runInTransaction {
                   item.items = addRepos(item.items)
                    val repoIds = item.items.map { it.id }
                    val repoSearchResult = RepoSearchResult(
                        query = query,
                        repoIds = repoIds,
                        totalCount = item.total,
                        next = item.nextPage
                    )
                    repoDao.insert(repoSearchResult)
                }
            }

            override fun shouldFetch(data: List<Repo>?) = data == null

            override fun loadFromDb(): LiveData<List<Repo>> {
                return repoDao.search(query).switchMap { searchData ->
                    if (searchData == null) {
                        AbsentLiveData.create()
                    } else {
                        repoDao.loadOrdered(searchData.repoIds)
                    }
                }
            }

            override fun createCall() = githubService.searchRepos(query)

            override fun processResponse(response: ApiSuccessResponse<RepoSearchResponse>)
                    : RepoSearchResponse {
                val body = response.body
                body.nextPage = response.nextPage
                return body
            }
        }.asLiveData()
    }
}
