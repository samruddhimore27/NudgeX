package com.example.prepx.data.api

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object matching payload format from free Kontests REST API.
 */
data class KontestsContestDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("start_time")
    val startTime: String,

    @SerializedName("end_time")
    val endTime: String?,

    @SerializedName("duration")
    val duration: String?,

    @SerializedName("site")
    val site: String?,

    @SerializedName("in_24_hours")
    val in24Hours: String?,

    @SerializedName("status")
    val status: String?
)
