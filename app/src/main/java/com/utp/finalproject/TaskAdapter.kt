package com.utp.finalproject

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utp.finalproject.data.PetTask

class TaskAdapter(
    private val onCompletionChanged: (PetTask, Boolean) -> Unit,
    private val onEditClicked: (PetTask) -> Unit,
    private val onDeleteClicked: (PetTask) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val tasks = mutableListOf<PetTask>()

    fun submitList(newTasks: List<PetTask>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)

        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val completedCheck: CheckBox = view.findViewById(R.id.completedCheck)
        private val titleText: TextView = view.findViewById(R.id.taskTitleText)
        private val detailText: TextView = view.findViewById(R.id.taskDetailText)
        private val notesText: TextView = view.findViewById(R.id.taskNotesText)
        private val editButton: Button = view.findViewById(R.id.editTaskButton)
        private val deleteButton: Button = view.findViewById(R.id.deleteTaskButton)

        fun bind(task: PetTask) {
            completedCheck.setOnCheckedChangeListener(null)
            completedCheck.isChecked = task.isCompleted

            titleText.text = "${task.taskType} - ${task.petName}"
            detailText.text = "${task.dueDate} | ${task.priority}"
            notesText.text = task.notes

            titleText.paintFlags = if (task.isCompleted) {
                titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                titleText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            completedCheck.setOnCheckedChangeListener { _, isChecked ->
                onCompletionChanged(task, isChecked)
            }
            editButton.setOnClickListener { onEditClicked(task) }
            deleteButton.setOnClickListener { onDeleteClicked(task) }
        }
    }
}
