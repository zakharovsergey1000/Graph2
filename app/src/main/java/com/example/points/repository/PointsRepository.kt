package com.example.points.repository

import androidx.lifecycle.LiveData
import com.example.points.AppExecutors
import com.example.points.api.ApiSuccessResponse
import com.example.points.api.GetPointsResponse
import com.example.points.api.PointsService
import com.example.points.db.PointDao
import com.example.points.db.PointsDb
import com.example.points.testing.OpenForTesting
import com.example.points.vo.Point
import com.example.points.vo.PointSearchResult
import com.example.points.vo.Resource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles Point instances.
 *
 * Point - value object name
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
