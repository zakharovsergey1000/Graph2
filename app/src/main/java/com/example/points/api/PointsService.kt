package com.example.points.api

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * REST API access points
 */
interface PointsService {
    @GET("api/test/points")
    fun getPoints(@Query("count") query: String): LiveData<ApiResponse<GetPointsResponse>>

    @GET("api/test/points")
    fun getPoints(@Query("count") query: String, @Query("page") page: Int): Call<GetPointsResponse>
}
