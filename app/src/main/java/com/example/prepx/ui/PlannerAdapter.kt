package com.example.prepx.ui

import android.graphics.Color
import android.graphics.Paint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.prepx.R
import com.example.prepx.data.model.ItemType
import com.example.prepx.data.model.PlannerItem
import com.example.prepx.databinding.ItemPlannerCardBinding
import com.example.prepx.util.DateTimeUtils

/**
 * RecyclerView Adapter displaying PlannerItem cards with dynamic Material 3 chip colors,
 * completion strikethroughs, and gesture callbacks.
 */
class PlannerAdapter(
    private val onItemClick: (PlannerItem) -> Unit,
    private val onToggleCompletion: (PlannerItem) -> Unit,
    private val onItemLongClick: (PlannerItem) -> Unit
) : ListAdapter<PlannerItem, PlannerAdapter.PlannerViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlannerViewHolder {
        val binding = ItemPlannerCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlannerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlannerViewHolder(private val binding: ItemPlannerCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlannerItem) {
            val context = binding.root.context

            binding.textTitle.text = item.title
            binding.textDateTime.text = DateTimeUtils.formatDateTime(item.dateTime)

            if (!item.description.isNullOrEmpty()) {
                binding.textDescription.visibility = View.VISIBLE
                binding.textDescription.text = item.description
            } else {
                binding.textDescription.visibility = View.GONE
            }

            // Bind Type Chip
            binding.chipType.text = item.type.name
            styleChipByType(item.type)

            // Bind Reminder Badge
            if (item.reminderEnabled && item.reminderTime != null) {
                binding.chipReminder.visibility = View.VISIBLE
                binding.chipReminder.text = "⏰ " + DateTimeUtils.formatTime(item.reminderTime)
            } else {
                binding.chipReminder.visibility = View.GONE
            }

            // Bind Checkbox & Strikethrough
            binding.checkBoxCompleted.isChecked = item.isCompleted
            if (item.isCompleted) {
                binding.textTitle.paintFlags = binding.textTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.root.alpha = 0.6f
            } else {
                binding.textTitle.paintFlags = binding.textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.root.alpha = 1.0f
            }

            // Event Listeners
            binding.checkBoxCompleted.setOnClickListener {
                onToggleCompletion(item)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }

            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }

        private fun styleChipByType(type: ItemType) {
            val context = binding.root.context
            val (bgColor, textColor) = when (type) {
                ItemType.CONTEST -> Pair("#E0F2F1", "#00796B") // Teal/Cyan
                ItemType.CLASS -> Pair("#E3F2FD", "#1565C0")   // Blue
                ItemType.EXAM -> Pair("#F3E5F5", "#6A1B9A")    // Purple
                ItemType.GOAL -> Pair("#E8F5E9", "#2E7D32")    // Green
                ItemType.TASK -> Pair("#FFF3E0", "#E65100")    // Amber/Orange
            }

            binding.chipType.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(bgColor))
            binding.chipType.setTextColor(Color.parseColor(textColor))
            binding.chipType.chipStrokeColor = ColorStateList.valueOf(Color.parseColor(textColor))
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<PlannerItem>() {
        override fun areItemsTheSame(oldItem: PlannerItem, newItem: PlannerItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PlannerItem, newItem: PlannerItem): Boolean {
            return oldItem == newItem
        }
    }
}
