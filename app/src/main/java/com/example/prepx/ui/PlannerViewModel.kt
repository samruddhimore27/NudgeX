package com.example.prepx.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prepx.data.auth.AuthRepository
import com.example.prepx.data.db.AppDatabase
import com.example.prepx.data.model.ItemType
import com.example.prepx.data.model.PlannerItem
import com.example.prepx.data.repository.PlannerRepository
import com.example.prepx.reminder.AlarmScheduler
import com.example.prepx.util.StreakCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel managing state and user actions for PrepX UI screens.
 * Exposes reactive StateFlow streams for items, active category filters, streaks, and sync status.
 */
class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlannerRepository
    val authRepository: AuthRepository = AuthRepository()

    private val _currentFilter = MutableStateFlow<ItemType?>(null)
    val currentFilter: StateFlow<ItemType?> = _currentFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PlannerRepository(database.plannerDao())
    }

    /**
     * Flow of raw item list from Room database.
     */
    val allItems: StateFlow<List<PlannerItem>> = repository.allItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Filtered list of planner items based on active category chip selection.
     */
    val filteredItems: StateFlow<List<PlannerItem>> = combine(allItems, _currentFilter) { items, filter ->
        if (filter == null) {
            items
        } else {
            items.filter { it.type == filter }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Reactive count of consecutive daily goal completion streak.
     */
    val streakCount: StateFlow<Int> = allItems.map { items ->
        StreakCalculator.calculateStreak(items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    /**
     * Updates active category filter.
     */
    fun setFilter(filter: ItemType?) {
        _currentFilter.value = filter
    }

    /**
     * Creates and schedules a new planner item.
     */
    fun addItem(item: PlannerItem, context: Context) {
        viewModelScope.launch {
            val insertedId = repository.insertItem(item)
            if (item.reminderEnabled) {
                val scheduledItem = item.copy(id = insertedId)
                AlarmScheduler.scheduleExactReminder(context, scheduledItem)
            }
        }
    }

    /**
     * Toggles item completion status.
     */
    fun toggleCompletion(item: PlannerItem) {
        viewModelScope.launch {
            repository.toggleCompletion(item.id, !item.isCompleted)
        }
    }

    /**
     * Deletes a planner item and cancels any associated exact alarm.
     */
    fun deleteItem(item: PlannerItem, context: Context) {
        viewModelScope.launch {
            AlarmScheduler.cancelReminder(context, item.id)
            repository.deleteItem(item)
        }
    }

    /**
     * Synchronizes Codeforces contests from public API.
     */
    fun refreshContests() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = repository.refreshContests()
            _isRefreshing.value = false

            result.onSuccess { count ->
                _syncMessage.value = if (count > 0) "Synced $count new contests!" else "All contests up to date."
            }.onFailure { error ->
                _syncMessage.value = "Offline mode: Unable to reach contest API."
            }
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }
}
