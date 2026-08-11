package com.example.prepx.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.prepx.data.api.KontestsContestDto
import com.example.prepx.databinding.ItemContestCardBinding
import com.example.prepx.util.DateTimeUtils

/**
 * RecyclerView Adapter displaying multi-platform contests (LeetCode, CodeChef, Codeforces)
 * with platform badges, direct browser links, and 1-tap planner import.
 */
class ContestsAdapter(
    private val onAddClick: (KontestsContestDto) -> Unit,
    private val onViewUrlClick: (KontestsContestDto) -> Unit
) : ListAdapter<KontestsContestDto, ContestsAdapter.ContestViewHolder>(ContestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContestViewHolder {
        val binding = ItemContestCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContestViewHolder(private val binding: ItemContestCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contest: KontestsContestDto) {
            binding.textContestName.text = contest.name

            val startTimeMs = DateTimeUtils.parseIsoToEpochMillis(contest.startTime)
            binding.textContestTime.text = if (startTimeMs > 0) {
                "Starts: " + DateTimeUtils.formatDateTime(startTimeMs)
            } else {
                "Starts: ${contest.startTime}"
            }

            val durationSeconds = try {
                contest.duration?.toDouble()?.toLong() ?: 7200L
            } catch (e: Exception) {
                7200L
            }
            binding.textDuration.text = "Duration: " + DateTimeUtils.formatDuration(durationSeconds)

            // Platform styling
            val platformName = getPlatformDisplayName(contest.site, contest.name)
            binding.chipPlatform.text = platformName
            stylePlatformChip(platformName)

            binding.buttonAddToPlanner.setOnClickListener {
                onAddClick(contest)
            }

            binding.buttonViewUrl.setOnClickListener {
                onViewUrlClick(contest)
            }
        }

        private fun getPlatformDisplayName(site: String?, name: String): String {
            val combined = ((site ?: "") + " " + name).lowercase()
            return when {
                combined.contains("leetcode") -> "LEETCODE"
                combined.contains("code_chef") || combined.contains("codechef") -> "CODECHEF"
                combined.contains("codeforces") -> "CODEFORCES"
                else -> site?.uppercase() ?: "CONTEST"
            }
        }

        private fun stylePlatformChip(platform: String) {
            val (bgColor, textColor) = when (platform) {
                "LEETCODE" -> Pair("#FFA116", "#FFFFFF")   // LeetCode Orange
                "CODECHEF" -> Pair("#5B4638", "#FFFFFF")   // CodeChef Brown
                "CODEFORCES" -> Pair("#1F8ACB", "#FFFFFF") // Codeforces Blue
                else -> Pair("#6200EE", "#FFFFFF")
            }

            binding.chipPlatform.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(bgColor))
            binding.chipPlatform.setTextColor(Color.parseColor(textColor))
            binding.chipPlatform.chipStrokeWidth = 0f
        }
    }

    class ContestDiffCallback : DiffUtil.ItemCallback<KontestsContestDto>() {
        override fun areItemsTheSame(oldItem: KontestsContestDto, newItem: KontestsContestDto): Boolean {
            return oldItem.name == newItem.name && oldItem.startTime == newItem.startTime
        }

        override fun areContentsTheSame(oldItem: KontestsContestDto, newItem: KontestsContestDto): Boolean {
            return oldItem == newItem
        }
    }
}
