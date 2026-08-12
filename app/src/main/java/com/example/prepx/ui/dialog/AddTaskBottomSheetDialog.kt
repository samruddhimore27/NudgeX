package com.example.prepx.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.prepx.data.model.ItemType
import com.example.prepx.data.model.PlannerItem
import com.example.prepx.data.model.RepeatType
import com.example.prepx.data.model.Source
import com.example.prepx.databinding.DialogAddTaskBinding
import com.example.prepx.ui.PlannerViewModel
import com.example.prepx.util.DateTimeUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

/**
 * BottomSheet Dialog for creating or editing scheduled PrepX tasks, contests, classes, exams, or goals.
 */
class AddTaskBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "AddTaskBottomSheetDialog"
        private const val ARG_ITEM_ID = "arg_item_id"

        fun newInstance(itemId: Long = -1L): AddTaskBottomSheetDialog {
            val fragment = AddTaskBottomSheetDialog()
            val args = Bundle().apply {
                putLong(ARG_ITEM_ID, itemId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: DialogAddTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlannerViewModel by activityViewModels()
    private var selectedCalendar: Calendar = Calendar.getInstance()
    private var editingItemId: Long = -1L
    private var existingItem: PlannerItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editingItemId = arguments?.getLong(ARG_ITEM_ID, -1L) ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateDateTimeDisplay()
        setupListeners()

        if (editingItemId != -1L) {
            loadExistingItem()
        }
    }

    private fun updateDateTimeDisplay() {
        binding.textSelectedDateTime.text = DateTimeUtils.formatDateTime(selectedCalendar.timeInMillis)
    }

    private fun setupListeners() {
        binding.buttonPickDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonPickTime.setOnClickListener {
            showTimePicker()
        }

        binding.chipGroupRepeat.setOnCheckedStateChangeListener { _, checkedIds ->
            binding.layoutWeeklyDaysContainer.visibility =
                if (checkedIds.contains(binding.chipRepeatWeekly.id)) View.VISIBLE else View.GONE
        }

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutReminderContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.buttonSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Event Date")
            .setSelection(selectedCalendar.timeInMillis)
            .build()

        datePicker.addOnPositiveButtonClickListener { selectionMillis ->
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = selectionMillis
            }
            selectedCalendar.set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
            selectedCalendar.set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
            selectedCalendar.set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
            updateDateTimeDisplay()
        }

        datePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(selectedCalendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(selectedCalendar.get(Calendar.MINUTE))
            .setTitleText("Select Event Time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            selectedCalendar.set(Calendar.MINUTE, timePicker.minute)
            selectedCalendar.set(Calendar.SECOND, 0)
            updateDateTimeDisplay()
        }

        timePicker.show(parentFragmentManager, "MATERIAL_TIME_PICKER")
    }

    private fun loadExistingItem() {
        lifecycleScope.launch {
            val item = viewModel.allItems.value.find { it.id == editingItemId }
            if (item != null) {
                existingItem = item
                binding.textDialogTitle.text = "Edit Item"
                binding.editTaskTitle.setText(item.title)
                binding.editTaskDescription.setText(item.description ?: "")
                binding.editTaskUrl.setText(item.url ?: "")
                selectedCalendar.timeInMillis = item.dateTime
                updateDateTimeDisplay()

                when (item.type) {
                    ItemType.CONTEST -> binding.chipTypeContest.isChecked = true
                    ItemType.CLASS -> binding.chipTypeClass.isChecked = true
                    ItemType.EXAM -> binding.chipTypeExam.isChecked = true
                    ItemType.TASK -> binding.chipTypeTask.isChecked = true
                    ItemType.GOAL -> binding.chipTypeGoal.isChecked = true
                }

                when (item.repeatType) {
                    RepeatType.DAILY -> binding.chipRepeatDaily.isChecked = true
                    RepeatType.WEEKLY -> {
                        binding.chipRepeatWeekly.isChecked = true
                        binding.layoutWeeklyDaysContainer.visibility = View.VISIBLE
                        val days = item.repeatDays?.split(",")?.map { it.trim().uppercase() } ?: emptyList()
                        binding.chipDaySun.isChecked = days.contains("SUN")
                        binding.chipDayMon.isChecked = days.contains("MON")
                        binding.chipDayTue.isChecked = days.contains("TUE")
                        binding.chipDayWed.isChecked = days.contains("WED")
                        binding.chipDayThu.isChecked = days.contains("THU")
                        binding.chipDayFri.isChecked = days.contains("FRI")
                        binding.chipDaySat.isChecked = days.contains("SAT")
                    }
                    RepeatType.NONE -> binding.chipRepeatNone.isChecked = true
                }

                binding.switchReminder.isChecked = item.reminderEnabled
                if (item.reminderEnabled && item.reminderTime != null) {
                    val diffMs = item.dateTime - item.reminderTime
                    when {
                        diffMs >= 82800000L -> binding.chipReminder1d.isChecked = true
                        diffMs >= 7100000L -> binding.chipReminder2h.isChecked = true
                        diffMs >= 3500000L -> binding.chipReminder1h.isChecked = true
                        diffMs >= 1700000L -> binding.chipReminder30m.isChecked = true
                        diffMs >= 800000L -> binding.chipReminder15m.isChecked = true
                        else -> binding.chipReminder0m.isChecked = true
                    }
                }
            }
        }
    }

    private fun saveTask() {
        val title = binding.editTaskTitle.text.toString().trim()
        val description = binding.editTaskDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.editTaskTitle.error = "Title is required"
            return
        }

        val type = when (binding.chipGroupType.checkedChipId) {
            binding.chipTypeContest.id -> ItemType.CONTEST
            binding.chipTypeClass.id -> ItemType.CLASS
            binding.chipTypeExam.id -> ItemType.EXAM
            binding.chipTypeGoal.id -> ItemType.GOAL
            else -> ItemType.TASK
        }

        val repeatType = when (binding.chipGroupRepeat.checkedChipId) {
            binding.chipRepeatDaily.id -> RepeatType.DAILY
            binding.chipRepeatWeekly.id -> RepeatType.WEEKLY
            else -> RepeatType.NONE
        }

        val repeatDays = if (repeatType == RepeatType.WEEKLY) {
            val selectedDays = mutableListOf<String>()
            if (binding.chipDaySun.isChecked) selectedDays.add("SUN")
            if (binding.chipDayMon.isChecked) selectedDays.add("MON")
            if (binding.chipDayTue.isChecked) selectedDays.add("TUE")
            if (binding.chipDayWed.isChecked) selectedDays.add("WED")
            if (binding.chipDayThu.isChecked) selectedDays.add("THU")
            if (binding.chipDayFri.isChecked) selectedDays.add("FRI")
            if (binding.chipDaySat.isChecked) selectedDays.add("SAT")

            if (selectedDays.isEmpty()) {
                // Default to selected event day if no days checked
                val dayStr = when (selectedCalendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SUNDAY -> "SUN"
                    Calendar.MONDAY -> "MON"
                    Calendar.TUESDAY -> "TUE"
                    Calendar.WEDNESDAY -> "WED"
                    Calendar.THURSDAY -> "THU"
                    Calendar.FRIDAY -> "FRI"
                    Calendar.SATURDAY -> "SAT"
                    else -> "MON"
                }
                dayStr
            } else {
                selectedDays.joinToString(",")
            }
        } else null

        val reminderEnabled = binding.switchReminder.isChecked
        val eventTimeMs = selectedCalendar.timeInMillis

        val offsetMs = when (binding.chipGroupReminderOffset.checkedChipId) {
            binding.chipReminder15m.id -> 15 * 60 * 1000L
            binding.chipReminder30m.id -> 30 * 60 * 1000L
            binding.chipReminder2h.id -> 2 * 3600 * 1000L
            binding.chipReminder1d.id -> 24 * 3600 * 1000L
            binding.chipReminder0m.id -> 0L
            else -> 3600 * 1000L // 1 hour default
        }

        val reminderTimeMs = if (reminderEnabled) eventTimeMs - offsetMs else null

        val rawUrl = binding.editTaskUrl.text.toString().trim()
        val formattedUrl = if (rawUrl.isNotEmpty()) {
            if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) "https://$rawUrl" else rawUrl
        } else null

        val itemToSave = PlannerItem(
            id = if (editingItemId != -1L) editingItemId else 0L,
            title = title,
            description = if (description.isNotEmpty()) description else null,
            type = type,
            dateTime = eventTimeMs,
            isCompleted = existingItem?.isCompleted ?: false,
            reminderEnabled = reminderEnabled,
            reminderTime = reminderTimeMs,
            source = existingItem?.source ?: Source.MANUAL,
            externalId = existingItem?.externalId,
            repeatType = repeatType,
            repeatDays = repeatDays,
            url = formattedUrl
        )

        if (editingItemId != -1L) {
            viewModel.addItem(itemToSave, requireContext())
            Toast.makeText(requireContext(), "Item updated!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addItem(itemToSave, requireContext())
            Toast.makeText(requireContext(), "Task added!", Toast.LENGTH_SHORT).show()
        }

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
