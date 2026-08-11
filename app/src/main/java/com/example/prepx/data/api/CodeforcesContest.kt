package com.example.prepx.data.api

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object representing a contest returned from Codeforces API.
 */
data class CodeforcesContest(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String?,

    @SerializedName("phase")
    val phase: String, // e.g. "BEFORE", "CODING", "FINISHED"

    @SerializedName("frozen")
    val frozen: Boolean = false,

    @SerializedName("durationSeconds")
    val durationSeconds: Long,

    @SerializedName("startTimeSeconds")
    val startTimeSeconds: Long?,

    @SerializedName("relativeTimeSeconds")
    val relativeTimeSeconds: Long?
)
