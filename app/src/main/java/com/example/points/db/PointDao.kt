package com.example.points.db

import android.util.SparseIntArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.points.testing.OpenForTesting
import com.example.points.vo.Point
import com.example.points.vo.PointSearchResult

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

    fun loadOrdered(pointIds: List<Int>): LiveData<List<Point>> {
        val order = SparseIntArray()
        pointIds.withIndex().forEach {
            order.put(it.value, it.index)
        }
        return loadById(pointIds).map { repositories ->
            repositories.sortedWith(compareBy { order.get(it.id) })
        }
    }

    @Query("SELECT * FROM Point WHERE id in (:pointIds)")
    protected abstract fun loadById(pointIds: List<Int>): LiveData<List<Point>>

    @Query("SELECT * FROM PointSearchResult WHERE `query` = :query")
    abstract fun findSearchResult(query: String): PointSearchResult?
}
