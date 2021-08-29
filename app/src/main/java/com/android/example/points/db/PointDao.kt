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

package com.android.example.points.db

import android.util.SparseIntArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.example.points.testing.OpenForTesting
import com.android.example.points.vo.Point
import com.android.example.points.vo.PointSearchResult

/**
 * Interface for database access on Point related operations.
 */
@Dao
@OpenForTesting
abstract class PointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg points: Point)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPoints(points: List<Point>): LongArray

    @Query("SELECT * FROM Point WHERE ROWID in (:rowid)")
    abstract fun getPointsFromRowids(rowid: LongArray): List<Point>

    @Query(
        """
        SELECT * FROM Point
        WHERE count = :count
        ORDER BY x ASC"""
    )
    abstract fun loadPoints(count: String): LiveData<List<Point>>

    @Query(
        """
        DELETE FROM Point
        WHERE count = :count"""
    )
    abstract fun deletePoints(count: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(result: PointSearchResult)

    @Query("SELECT * FROM PointSearchResult WHERE `query` = :query")
    abstract fun search(query: String): LiveData<PointSearchResult?>

    fun loadOrdered(repoIds: List<Int>): LiveData<List<Point>> {
        val order = SparseIntArray()
        repoIds.withIndex().forEach {
            order.put(it.value, it.index)
        }
        return loadById(repoIds).map { repositories ->
            repositories.sortedWith(compareBy { order.get(it.id) })
        }
    }

    @Query("SELECT * FROM Point WHERE id in (:repoIds)")
    protected abstract fun loadById(repoIds: List<Int>): LiveData<List<Point>>

    @Query("SELECT * FROM PointSearchResult WHERE `query` = :query")
    abstract fun findSearchResult(query: String): PointSearchResult?
}
