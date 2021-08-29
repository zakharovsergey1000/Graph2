package com.example.points.db


import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.points.vo.Point
import com.example.points.vo.PointSearchResult

/**
 * Main database description.
 */
@Database(
    entities = [
        Point::class,
        PointSearchResult::class],
    version = 8,
    exportSchema = false
)
abstract class PointsDb : RoomDatabase() {
    abstract fun repoDao(): PointDao
}
