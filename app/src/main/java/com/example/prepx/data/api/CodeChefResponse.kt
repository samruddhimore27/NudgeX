package com.example.prepx.data.api

import com.google.gson.annotations.SerializedName

/**
 * DTO matching official CodeChef API response structure.
 */
data class CodeChefResponse(
    @SerializedName("status")
    val status: String?,

    @SerializedName("future_contests")
    val futureContests: List<CodeChefContestDto>? = null,

    @SerializedName("present_contests")
    val presentContests: List<CodeChefContestDto>? = null
)

data class CodeChefContestDto(
    @SerializedName("contest_code")
    val contestCode: String,

    @SerializedName("contest_name")
    val contestName: String,

    @SerializedName("contest_start_date_iso")
    val contestStartDateIso: String?,

    @SerializedName("contest_duration")
    val contestDuration: String?
)
