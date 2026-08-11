package com.example.prepx.data.api

import retrofit2.http.GET

/**
 * Free public Retrofit service interface for Kontests API endpoints.
 * Requires no API keys or subscription tokens.
 */
interface KontestsApiService {

    @GET("api/v1/all")
    suspend fun getAllContests(): List<KontestsContestDto>

    @GET("api/v1/leetcode")
    suspend fun getLeetCodeContests(): List<KontestsContestDto>

    @GET("api/v1/code_chef")
    suspend fun getCodeChefContests(): List<KontestsContestDto>

    @GET("api/v1/codeforces")
    suspend fun getCodeforcesContests(): List<KontestsContestDto>
}
