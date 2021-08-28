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
import com.android.example.github.AppExecutors
import com.android.example.github.api.ApiSuccessResponse
import com.android.example.github.api.PointsService
import com.android.example.github.api.GetPointsResponse
import com.android.example.github.db.PointsDb
import com.android.example.github.db.PointDao
import com.android.example.github.testing.OpenForTesting
import com.android.example.github.vo.Point
import com.android.example.github.vo.PointSearchResult
import com.android.example.github.vo.Resource
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
class PointsRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val db: PointsDb,
    private val pointDao: PointDao,
    private val pointsService: PointsService
) {

    fun loadPoints(owner: String): LiveData<List<Point>> {
        return pointDao.loadPoints(owner)
    }

    fun searchNextPage(query: String): LiveData<Resource<Boolean>> {
        val fetchNextSearchPageTask = FetchNextSearchPageTask(
            query = query,
            pointsService = pointsService,
            db = db
        )
        appExecutors.networkIO().execute(fetchNextSearchPageTask)
        return fetchNextSearchPageTask.liveData
    }

    private fun addPoints(points: List<Point>): List<Point> {
        val count = points.count()
        val list = mutableListOf<Point> ()
        for (i in 0..count step 999) {
            val ids = pointDao.insertPoints(points.subList(i, Math.min(i + 999, count) ))
            list.addAll(pointDao.getPointsFromRowids(ids))
        }
        return list
    }

    fun search(query: String): LiveData<Resource<List<Point>>> {
        return object : NetworkBoundResource<List<Point>, GetPointsResponse>(appExecutors) {

            override fun saveCallResult(item: GetPointsResponse) {
                val count = item.items.count()
                item.items.forEach { repo ->  repo.count = count}
                db.runInTransaction {
                    pointDao.deletePoints(count.toString())
                    item.items = addPoints(item.items)
                    val repoIds = item.items.map { it.id }
                    val repoSearchResult = PointSearchResult(
                        query = query,
                        repoIds = repoIds,
                        totalCount = item.total,
                        next = item.nextPage
                    )
                    pointDao.insert(repoSearchResult)
                }
            }

            override fun shouldFetch(data: List<Point>?) = true

            override fun loadFromDb(): LiveData<List<Point>> {
                return pointDao.loadPoints(query)
            }

            override fun createCall() = pointsService.getPoints(query)

            override fun processResponse(response: ApiSuccessResponse<GetPointsResponse>)
                    : GetPointsResponse {
                val body = response.body
                body.nextPage = response.nextPage
                return body
            }
        }.asLiveData()
    }
}
