package com.utp.finalproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.utp.finalproject.data.PetTask
import com.utp.finalproject.data.TaskDatabaseHelper
import com.utp.finalproject.data.TaskRepository
import com.utp.finalproject.ui.TaskFormViewModel
import com.utp.finalproject.ui.TaskFormViewModelFactory
import kotlinx.coroutines.launch

class TaskFormActivity : AppCompatActivity() {

    private lateinit var viewModel: TaskFormViewModel
    private lateinit var petNameInput: EditText
    private lateinit var taskTypeInput: EditText
    private lateinit var dueDateInput: EditText
    private lateinit var priorityInput: EditText
    private lateinit var notesInput: EditText
    private lateinit var completedCheck: CheckBox
    private lateinit var deleteButton: Button

    private var taskId: Long = 0L
    private var resultDelivered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)

        val taskRepository = TaskRepository(TaskDatabaseHelper(applicationContext))
        viewModel = ViewModelProvider(
            this,
            TaskFormViewModelFactory(taskRepository)
        )[TaskFormViewModel::class.java]

        taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        bindViews()
        deleteButton.visibility = if (taskId == 0L) View.GONE else View.VISIBLE

        viewModel.loadTask(taskId)

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.task?.let { fillTask(it) }

                if (!resultDelivered && (state.isSaved || state.isDeleted)) {
                    resultDelivered = true
                    finishWithResult()
                }
            }
        }

        findViewById<Button>(R.id.saveTaskButton).setOnClickListener {
            saveTask()
        }

        deleteButton.setOnClickListener {
            viewModel.deleteTask(taskId)
        }
    }

    private fun bindViews() {
        petNameInput = findViewById(R.id.petNameInput)
        taskTypeInput = findViewById(R.id.taskTypeInput)
        dueDateInput = findViewById(R.id.dueDateInput)
        priorityInput = findViewById(R.id.priorityInput)
        notesInput = findViewById(R.id.notesInput)
        completedCheck = findViewById(R.id.completedCheck)
        deleteButton = findViewById(R.id.deleteTaskButton)
    }

    private fun fillTask(task: PetTask) {
        if (petNameInput.text.isNotBlank()) {
            return
        }

        petNameInput.setText(task.petName)
        taskTypeInput.setText(task.taskType)
        dueDateInput.setText(task.dueDate)
        priorityInput.setText(task.priority)
        notesInput.setText(task.notes)
        completedCheck.isChecked = task.isCompleted
    }

    private fun saveTask() {
        val petName = petNameInput.text.toString().trim()
        val taskType = taskTypeInput.text.toString().trim()
        val dueDate = dueDateInput.text.toString().trim()
        val priority = priorityInput.text.toString().trim()
        val notes = notesInput.text.toString().trim()

        if (petName.isBlank() || taskType.isBlank() || dueDate.isBlank() || priority.isBlank()) {
            Toast.makeText(this, R.string.task_required_fields, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveTask(
            PetTask(
                id = taskId,
                petName = petName,
                taskType = taskType,
                dueDate = dueDate,
                priority = priority,
                notes = notes,
                isCompleted = completedCheck.isChecked
            )
        )
    }

    private fun finishWithResult() {
        val result = Intent().apply {
            putExtra(EXTRA_TASK_CHANGED, true)
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    companion object {
        const val EXTRA_TASK_ID = "com.utp.finalproject.extra.TASK_ID"
        const val EXTRA_TASK_CHANGED = "com.utp.finalproject.extra.TASK_CHANGED"
    }
}
