package com.example.prepx.ui.contests

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prepx.data.api.KontestsContestDto
import com.example.prepx.data.db.AppDatabase
import com.example.prepx.data.model.ItemType
import com.example.prepx.data.model.PlannerItem
import com.example.prepx.data.model.PlatformType
import com.example.prepx.data.model.Source
import com.example.prepx.data.repository.PlannerRepository
import com.example.prepx.databinding.FragmentContestsBinding
import com.example.prepx.ui.ContestsAdapter
import com.example.prepx.ui.PlannerViewModel
import com.example.prepx.util.DateTimeUtils
import kotlinx.coroutines.launch

/**
 * Fragment showcasing upcoming competitive programming contests from LeetCode, CodeChef, and Codeforces.
 */
class ContestsFragment : Fragment() {

    private var _binding: FragmentContestsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlannerViewModel by activityViewModels()
    private lateinit var repository: PlannerRepository
    private lateinit var contestsAdapter: ContestsAdapter
    
    private var activePlatformFilter: PlatformType = PlatformType.ALL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        repository = PlannerRepository(database.plannerDao())

        setupRecyclerView()
        setupPlatformFilters()
        setupSwipeRefresh()
        loadContests()
    }

    private fun setupRecyclerView() {
        contestsAdapter = ContestsAdapter(
            onAddClick = { contest -> addContestToPlanner(contest) },
            onViewUrlClick = { contest -> openContestUrl(contest.url) }
        )

        binding.recyclerViewContests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contestsAdapter
        }
    }

    private fun setupPlatformFilters() {
        binding.chipGroupPlatform.setOnCheckedStateChangeListener { _, checkedIds ->
            activePlatformFilter = if (checkedIds.isEmpty()) {
                PlatformType.ALL
            } else {
                when (checkedIds.first()) {
                    binding.chipPlatformLeetCode.id -> PlatformType.LEETCODE
                    binding.chipPlatformCodeChef.id -> PlatformType.CODECHEF
                    binding.chipPlatformCodeforces.id -> PlatformType.CODEFORCES
                    else -> PlatformType.ALL
                }
            }
            loadContests()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadContests()
        }
    }

    private fun loadContests() {
        binding.progressBar.visibility = View.VISIBLE
        binding.textErrorState.visibility = View.GONE
        binding.textEmptyState.visibility = View.GONE

        lifecycleScope.launch {
            val result = repository.fetchMultiPlatformContests(activePlatformFilter)
            binding.progressBar.visibility = View.GONE
            binding.swipeRefreshLayout.isRefreshing = false

            result.onSuccess { contests ->
                contestsAdapter.submitList(contests)
                binding.textEmptyState.visibility = if (contests.isEmpty()) View.VISIBLE else View.GONE
            }.onFailure { error ->
                showError("Offline Mode: Unable to connect to contest services.")
            }
        }
    }

    private fun addContestToPlanner(contest: KontestsContestDto) {
        val options = arrayOf("15 minutes before", "30 minutes before", "1 hour before (Default)", "2 hours before", "1 day before", "At Contest Start")
        val offsets = longArrayOf(15 * 60 * 1000L, 30 * 60 * 1000L, 3600 * 1000L, 2 * 3600 * 1000L, 24 * 3600 * 1000L, 0L)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Contest Reminder Time")
            .setSingleChoiceItems(options, 2) { dialog, which ->
                val selectedOffset = offsets[which]
                val startTimeMs = DateTimeUtils.parseIsoToEpochMillis(contest.startTime)
                val platformName = contest.site ?: "Contest"
                val eventTime = if (startTimeMs > 0) startTimeMs else System.currentTimeMillis() + 86400000L
                val reminderTimeMs = eventTime - selectedOffset

                val newItem = PlannerItem(
                    title = contest.name,
                    description = "Platform: $platformName | URL: ${contest.url}",
                    type = ItemType.CONTEST,
                    dateTime = eventTime,
                    isCompleted = false,
                    reminderEnabled = true,
                    reminderTime = reminderTimeMs,
                    source = if (platformName.lowercase().contains("codeforces")) Source.CODEFORCES else Source.MANUAL,
                    externalId = contest.name.hashCode().toString()
                )

                viewModel.addItem(newItem, requireContext())
                Toast.makeText(requireContext(), "Added '${contest.name}' with ${options[which]} alert!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openContestUrl(urlStr: String?) {
        if (urlStr.isNullOrBlank()) {
            Toast.makeText(requireContext(), "No URL available for this contest.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStr))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Could not open browser for URL: $urlStr", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(msg: String) {
        binding.textErrorState.text = msg
        binding.textErrorState.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
