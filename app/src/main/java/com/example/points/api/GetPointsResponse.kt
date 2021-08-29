package com.example.points.api


import com.example.points.vo.Point
import com.google.gson.annotations.SerializedName

/**
 * Simple object to hold repo search responses. This is different from the Entity in the database
 * because we are keeping a search result in 1 row and denormalizing list of results into a single
 * column.
 */
data class GetPointsResponse(
    @SerializedName("total_count")
    val total: Int = 0,
    @SerializedName("points")
    var items: List<Point>
) {
    var nextPage: Int? = null
}
