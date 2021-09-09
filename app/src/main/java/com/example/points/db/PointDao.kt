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
}
