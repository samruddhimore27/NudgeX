package com.example.prepx.data.repository

import android.util.Log
import com.example.prepx.data.api.CodeChefApiService
import com.example.prepx.data.api.CodeforcesApiService
import com.example.prepx.data.api.KontestsApiService
import com.example.prepx.data.api.KontestsContestDto
import com.example.prepx.data.api.RetrofitClient
import com.example.prepx.data.db.PlannerDao
import com.example.prepx.data.model.ItemType
import com.example.prepx.data.model.PlannerItem
import com.example.prepx.data.model.PlatformType
import com.example.prepx.data.model.Source
import com.example.prepx.util.ContestScheduleGenerator
import com.example.prepx.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Single source of truth repository managing Room DB and multi-platform contest APIs.
 * Combines direct official APIs and robust schedule generators for Codeforces, CodeChef, and LeetCode.
 */
class PlannerRepository(
    private val plannerDao: PlannerDao,
    private val apiService: CodeforcesApiService = RetrofitClient.apiService,
    private val codeChefApiService: CodeChefApiService = RetrofitClient.codeChefApiService,
    private val kontestsApiService: KontestsApiService = RetrofitClient.kontestsApiService
) {

    companion object {
        private const val TAG = "PrepX_Repository"
    }

    /**
     * Flow emitting the complete ordered list of planner items from local Room storage.
     */
    val allItems: Flow<List<PlannerItem>> = plannerDao.getAllItemsSortedByDate()

    /**
     * Flow emitting completed goals used for daily streak calculations.
     */
    val completedGoals: Flow<List<PlannerItem>> = plannerDao.getCompletedGoals()

    /**
     * Inserts a new planner item into local storage.
     */
    suspend fun insertItem(item: PlannerItem): Long = withContext(Dispatchers.IO) {
        val newId = plannerDao.insertItem(item)
        Log.d(TAG, "Inserted PlannerItem id=$newId, title=${item.title}")
        newId
    }

    /**
     * Updates an existing planner item.
     */
    suspend fun updateItem(item: PlannerItem) = withContext(Dispatchers.IO) {
        plannerDao.updateItem(item)
        Log.d(TAG, "Updated PlannerItem id=${item.id}")
    }

    /**
     * Deletes a planner item.
     */
    suspend fun deleteItem(item: PlannerItem) = withContext(Dispatchers.IO) {
        plannerDao.deleteItem(item)
        Log.d(TAG, "Deleted PlannerItem id=${item.id}")
    }

    /**
     * Toggles item completion status.
     */
    suspend fun toggleCompletion(id: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        plannerDao.updateCompletionStatus(id, isCompleted)
        Log.d(TAG, "Toggled completion for id=$id to $isCompleted")
    }

    /**
     * Fetches multi-platform contests (LeetCode, CodeChef, Codeforces) using direct APIs and fallback schedule generators.
     */
    suspend fun fetchMultiPlatformContests(platformFilter: PlatformType = PlatformType.ALL): Result<List<KontestsContestDto>> = withContext(Dispatchers.IO) {
        val combinedList = mutableListOf<KontestsContestDto>()

        // 1. Fetch Codeforces Contests
        if (platformFilter == PlatformType.ALL || platformFilter == PlatformType.CODEFORCES) {
            try {
                val cfResponse = apiService.getContestList(includeGym = false)
                if (cfResponse.status == "OK" && cfResponse.result != null) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val cfDtos = cfResponse.result
                        .filter { it.phase == "BEFORE" }
                        .map { contest ->
                            val startTimeMs = (contest.startTimeSeconds ?: 0L) * 1000L
                            KontestsContestDto(
                                name = contest.name,
                                url = "https://codeforces.com/contests/${contest.id}",
                                startTime = sdf.format(Date(startTimeMs)),
                                endTime = sdf.format(Date(startTimeMs + (contest.durationSeconds * 1000L))),
                                duration = contest.durationSeconds.toString(),
                                site = "Codeforces",
                                in24Hours = "No",
                                status = "BEFORE"
                            )
                        }
                    combinedList.addAll(cfDtos)
                    Log.d(TAG, "Fetched ${cfDtos.size} Codeforces contests.")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Codeforces fetch exception: ${e.localizedMessage}")
            }
        }

        // 2. Fetch CodeChef Contests (Official API + Generator Fallback)
        if (platformFilter == PlatformType.ALL || platformFilter == PlatformType.CODECHEF) {
            var ccAdded = false
            try {
                val ccResponse = codeChefApiService.getContests()
                val ccFuture = ccResponse.futureContests ?: emptyList()
                val ccPresent = ccResponse.presentContests ?: emptyList()
                val allCc = (ccFuture + ccPresent).distinctBy { it.contestCode }

                if (allCc.isNotEmpty()) {
                    val ccDtos = allCc.map { cc ->
                        val startTimeIso = cc.contestStartDateIso ?: ""
                        val durationSecs = (cc.contestDuration?.toLongOrNull() ?: 120L) * 60L
                        KontestsContestDto(
                            name = cc.contestName,
                            url = "https://www.codechef.com/${cc.contestCode}",
                            startTime = startTimeIso,
                            endTime = null,
                            duration = durationSecs.toString(),
                            site = "CodeChef",
                            in24Hours = "No",
                            status = "BEFORE"
                        )
                    }
                    combinedList.addAll(ccDtos)
                    ccAdded = true
                    Log.d(TAG, "Fetched ${ccDtos.size} CodeChef contests from official API.")
                }
            } catch (e: Exception) {
                Log.w(TAG, "CodeChef fetch exception: ${e.localizedMessage}")
            }

            if (!ccAdded) {
                val ccGenerated = ContestScheduleGenerator.getCodeChefContests()
                combinedList.addAll(ccGenerated)
                Log.d(TAG, "Added ${ccGenerated.size} generated CodeChef contests.")
            }
        }

        // 3. Fetch/Generate LeetCode Contests
        if (platformFilter == PlatformType.ALL || platformFilter == PlatformType.LEETCODE) {
            val leetCodeContests = ContestScheduleGenerator.getLeetCodeContests()
            combinedList.addAll(leetCodeContests)
            Log.d(TAG, "Added ${leetCodeContests.size} LeetCode contests.")
        }

        // Filter out past contests & sort by start time ascending
        val nowMs = System.currentTimeMillis() - 3600000L // Include contests active in last hour
        val sorted = combinedList
            .filter { dto ->
                val startTimeMs = DateTimeUtils.parseIsoToEpochMillis(dto.startTime)
                startTimeMs == 0L || startTimeMs >= nowMs
            }
            .sortedBy { DateTimeUtils.parseIsoToEpochMillis(it.startTime) }

        if (sorted.isNotEmpty()) {
            Result.success(sorted)
        } else {
            // Fallback: return generated schedule for all
            val fallbackAll = ContestScheduleGenerator.getLeetCodeContests() + ContestScheduleGenerator.getCodeChefContests()
            Result.success(fallbackAll)
        }
    }

    /**
     * Synchronizes upcoming Codeforces contests into Room local storage.
     */
    suspend fun refreshContests(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching Codeforces contest list...")
            val response = apiService.getContestList(includeGym = false)

            if (response.status != "OK" || response.result == null) {
                val errorMsg = response.comment ?: "Codeforces API returned error status."
                Log.w(TAG, "Codeforces API failure: $errorMsg")
                return@withContext Result.failure(Exception(errorMsg))
            }

            val upcomingContests = response.result.filter { it.phase == "BEFORE" }
            var insertedCount = 0

            for (contest in upcomingContests) {
                val externalId = contest.id.toString()
                val existing = plannerDao.getItemByExternalId(externalId)
                
                if (existing == null) {
                    val startTimeMs = (contest.startTimeSeconds ?: 0L) * 1000L
                    val item = PlannerItem(
                        title = contest.name,
                        description = "Contest (${contest.durationSeconds / 3600}h duration)",
                        type = ItemType.CONTEST,
                        dateTime = if (startTimeMs > 0) startTimeMs else System.currentTimeMillis() + 86400000L,
                        isCompleted = false,
                        reminderEnabled = true,
                        reminderTime = if (startTimeMs > 0) startTimeMs - 3600000L else null,
                        source = Source.CODEFORCES,
                        externalId = externalId,
                        url = "https://codeforces.com/contests/${contest.id}"
                    )
                    plannerDao.insertItem(item)
                    insertedCount++
                }
            }

            Log.d(TAG, "Codeforces sync complete. Added $insertedCount new upcoming contests.")
            Result.success(insertedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Network exception while refreshing Codeforces contests: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}
