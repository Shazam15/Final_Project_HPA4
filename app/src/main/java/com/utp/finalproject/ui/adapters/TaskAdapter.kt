package com.utp.finalproject.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utp.finalproject.R
import com.utp.finalproject.data.local.entity.TaskEntity
import com.utp.finalproject.databinding.ItemTaskBinding
import com.utp.finalproject.utils.Formatters

class TaskAdapter(
    private val onCompleteClick: (TaskEntity) -> Unit,
    private val onEditClick: (TaskEntity) -> Unit,
    private val onDeleteClick: (TaskEntity) -> Unit,
    private val onLocationClick: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskEntity) {
            val context = binding.root.context
            binding.taskTitleText.text = task.title
            binding.taskMetaText.text = context.getString(
                R.string.task_meta,
                task.category,
                task.priority,
                Formatters.formatDateTime(task.dueAtMillis)
            )
            binding.taskDescriptionText.text = task.description.ifBlank { context.getString(R.string.no_description) }
            binding.taskStatusText.text = task.status
            val hasLocation = task.latitude != null && task.longitude != null
            binding.taskLocationText.visibility = if (hasLocation) android.view.View.VISIBLE else android.view.View.GONE
            binding.openLocationButton.visibility = if (hasLocation) android.view.View.VISIBLE else android.view.View.GONE
            binding.taskLocationText.text = task.locationName.orEmpty()
            binding.completeTaskButton.isEnabled = task.status == TaskEntity.STATUS_PENDING ||
                task.status == TaskEntity.STATUS_OVERDUE
            binding.completeTaskButton.text = if (binding.completeTaskButton.isEnabled) {
                context.getString(R.string.complete_task)
            } else {
                context.getString(R.string.completed_task)
            }

            binding.completeTaskButton.setOnClickListener { onCompleteClick(task) }
            binding.editTaskButton.setOnClickListener { onEditClick(task) }
            binding.deleteTaskButton.setOnClickListener { onDeleteClick(task) }
            binding.openLocationButton.setOnClickListener { onLocationClick(task) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem == newItem
        }
    }
}
