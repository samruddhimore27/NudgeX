package com.example.prepx.data.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for Codeforces endpoints.
 */
interface CodeforcesApiService {

    /**
     * Fetches public list of competitive programming contests from Codeforces API.
     * @param includeGym Optional flag to include gym contests. Default is false.
     */
    @GET("contest.list")
    suspend fun getContestList(
        @Query("gym") includeGym: Boolean = false
    ): CodeforcesResponse
}
