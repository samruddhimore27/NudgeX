package com.example.prepx.data.api

import retrofit2.http.GET
import retrofit2.http.Headers

/**
 * Official Retrofit interface for CodeChef API.
 */
interface CodeChefApiService {

    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    @GET("api/list/contests/all?sort_by=START&sorting_order=asc")
    suspend fun getContests(): CodeChefResponse
}
