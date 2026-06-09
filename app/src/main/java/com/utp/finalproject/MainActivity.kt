package com.utp.finalproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.utp.finalproject.data.TaskDatabaseHelper
import com.utp.finalproject.data.TaskRepository
import com.utp.finalproject.data.UserPreferencesRepository
import com.utp.finalproject.ui.MainViewModel
import com.utp.finalproject.ui.MainViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var greetingText: TextView
    private lateinit var emptyText: TextView
    private lateinit var orderSpinner: Spinner

    private val taskFormLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK &&
            result.data?.getBooleanExtra(TaskFormActivity.EXTRA_TASK_CHANGED, false) == true
        ) {
            viewModel.refreshTasks()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val taskRepository = TaskRepository(TaskDatabaseHelper(applicationContext))
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(taskRepository, preferencesRepository)
        )[MainViewModel::class.java]

        greetingText = findViewById(R.id.greetingText)
        emptyText = findViewById(R.id.emptyText)
        orderSpinner = findViewById(R.id.orderSpinner)

        setupTaskList()
        setupOrderSpinner()

        findViewById<Button>(R.id.addTaskButton).setOnClickListener {
            openTaskForm()
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                val userName = intent.getStringExtra(EXTRA_USER_NAME)
                    ?.takeIf { it.isNotBlank() }
                    ?: state.userName

                greetingText.text = getString(R.string.dashboard_greeting, userName)
                taskAdapter.submitList(state.tasks)
                emptyText.visibility = if (state.tasks.isEmpty()) View.VISIBLE else View.GONE
                setSelectedOrder(state.orderBy)
            }
        }
    }

    private fun setupTaskList() {
        taskAdapter = TaskAdapter(
            onCompletionChanged = { task, isCompleted ->
                viewModel.updateCompletion(task, isCompleted)
            },
            onEditClicked = { task ->
                openTaskForm(task.id)
            },
            onDeleteClicked = { task ->
                viewModel.deleteTask(task)
            }
        )

        findViewById<RecyclerView>(R.id.tasksRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }

    private fun setupOrderSpinner() {
        val options = listOf(
            TaskRepository.ORDER_BY_DATE,
            TaskRepository.ORDER_BY_PRIORITY,
            TaskRepository.ORDER_BY_PET
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        orderSpinner.adapter = adapter

        orderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedOrder = options[position]
                if (selectedOrder != viewModel.uiState.value.orderBy) {
                    viewModel.updateOrder(selectedOrder)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setSelectedOrder(orderBy: String) {
        val selectedIndex = when (orderBy) {
            TaskRepository.ORDER_BY_PRIORITY -> 1
            TaskRepository.ORDER_BY_PET -> 2
            else -> 0
        }

        if (orderSpinner.selectedItemPosition != selectedIndex) {
            orderSpinner.setSelection(selectedIndex)
        }
    }

    private fun openTaskForm(taskId: Long = 0L) {
        val intent = Intent(this, TaskFormActivity::class.java).apply {
            putExtra(TaskFormActivity.EXTRA_TASK_ID, taskId)
        }
        taskFormLauncher.launch(intent)
    }

    companion object {
        const val EXTRA_USER_NAME = "com.utp.finalproject.extra.USER_NAME"
        const val EXTRA_USER_EMAIL = "com.utp.finalproject.extra.USER_EMAIL"
    }
}
