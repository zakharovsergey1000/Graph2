package com.example.points.vo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Point(
    @PrimaryKey(autoGenerate = true) val id: Int,
    var count: Int,
    val x: Float,
    val y: Float
)