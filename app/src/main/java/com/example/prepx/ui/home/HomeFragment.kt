package com.example.prepx.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prepx.data.model.ItemType
import com.example.prepx.data.model.PlannerItem
import com.example.prepx.databinding.FragmentHomeBinding
import com.example.prepx.ui.PlannerAdapter
import com.example.prepx.ui.PlannerViewModel
import com.example.prepx.ui.dialog.AddTaskBottomSheetDialog
import kotlinx.coroutines.launch

/**
 * Main dashboard fragment displaying active streak counter, category filters,
 * interactive task cards, and quick task creation FAB.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlannerViewModel by activityViewModels()
    private lateinit var adapter: PlannerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.refreshUserSession()
        setupRecyclerView()
        setupFilterChips()
        setupSwipeRefresh()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = PlannerAdapter(
            onItemClick = { item -> showEditDialog(item) },
            onToggleCompletion = { item -> viewModel.toggleCompletion(item) },
            onItemLongClick = { item -> showDeleteConfirmation(item) }
        )

        binding.recyclerViewPlanner.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                viewModel.setFilter(null)
                return@setOnCheckedStateChangeListener
            }
            val filter = when (checkedIds.first()) {
                binding.chipFilterContests.id -> ItemType.CONTEST
                binding.chipFilterClasses.id -> ItemType.CLASS
                binding.chipFilterExams.id -> ItemType.EXAM
                binding.chipFilterTasks.id -> ItemType.TASK
                binding.chipFilterGoals.id -> ItemType.GOAL
                else -> null
            }
            viewModel.setFilter(filter)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshContests()
        }
    }

    private fun setupListeners() {
        binding.fabAddTask.setOnClickListener {
            val dialog = AddTaskBottomSheetDialog.newInstance()
            dialog.show(childFragmentManager, AddTaskBottomSheetDialog.TAG)
        }

        binding.imageButtonGuide.setOnClickListener {
            val dialog = com.example.prepx.ui.dialog.AppGuideBottomSheetDialog.newInstance()
            dialog.show(childFragmentManager, com.example.prepx.ui.dialog.AppGuideBottomSheetDialog.TAG)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filteredItems.collect { items ->
                        adapter.submitList(items)
                        binding.layoutEmptyState.visibility =
                            if (items.isEmpty()) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.streakCount.collect { streak ->
                        binding.textStreakCount.text = "$streak"
                        binding.textStreakLabel.text =
                            if (streak == 1) "DAY STREAK 🔥" else "DAYS STREAK 🔥"
                    }
                }

                launch {
                    viewModel.isRefreshing.collect { isRefreshing ->
                        binding.swipeRefreshLayout.isRefreshing = isRefreshing
                    }
                }

                launch {
                    viewModel.syncMessage.collect { message ->
                        message?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                            viewModel.clearSyncMessage()
                        }
                    }
                }
            }
        }
    }

    private fun showEditDialog(item: PlannerItem) {
        val dialog = AddTaskBottomSheetDialog.newInstance(item.id)
        dialog.show(childFragmentManager, AddTaskBottomSheetDialog.TAG)
    }

    private fun showDeleteConfirmation(item: PlannerItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete '${item.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteItem(item, requireContext())
                Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
