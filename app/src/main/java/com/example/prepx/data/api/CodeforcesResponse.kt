package com.example.prepx.data.api

import com.google.gson.annotations.SerializedName

/**
 * Top-level wrapper response for Codeforces API calls.
 */
data class CodeforcesResponse(
    @SerializedName("status")
    val status: String, // "OK" or "FAILED"

    @SerializedName("result")
    val result: List<CodeforcesContest>? = null,

    @SerializedName("comment")
    val comment: String? = null
)
