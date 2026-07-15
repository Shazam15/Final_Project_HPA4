package com.utp.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.finalproject.data.local.entity.TaskEntity
import com.utp.finalproject.data.repository.HomePetRepository
import com.utp.finalproject.databinding.ActivityTasksBinding
import com.utp.finalproject.ui.adapters.TaskAdapter
import com.utp.finalproject.viewmodel.RepositoryViewModelFactory
import com.utp.finalproject.viewmodel.TasksViewModel
import com.utp.finalproject.utils.MapIntentHelper
import kotlinx.coroutines.launch

class TaskListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private lateinit var viewModel: TasksViewModel
    private lateinit var taskAdapter: TaskAdapter
    // Al volver del formulario no se recarga manualmente: Room notifica el cambio por tasksFlow.
    private val taskFormLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            RepositoryViewModelFactory(HomePetRepository(applicationContext))
        )[TasksViewModel::class.java]

        setupSpinners()
        setupList()

        // Room -> TaskDao.observeTasks -> Repository -> TasksViewModel -> RecyclerView.
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                taskAdapter.submitList(state.tasks)
            }
        }
    }

    private fun setupSpinners() {
        val filters = listOf("Todas", TaskEntity.STATUS_PENDING, TaskEntity.STATUS_COMPLETED, TaskEntity.STATUS_OVERDUE)
        val orders = listOf("Fecha", "Prioridad", "Categoria")
        bindSpinner(binding.filterSpinner, filters) { viewModel.setFilter(it) }
        bindSpinner(binding.orderSpinner, orders) { viewModel.setOrder(it) }
    }

    private fun bindSpinner(spinner: android.widget.Spinner, values: List<String>, onSelected: (String) -> Unit) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                // La selección modifica un StateFlow del ViewModel y vuelve como una lista filtrada.
                onSelected(values[position])
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
    }

    private fun setupList() {
        taskAdapter = TaskAdapter(
            onCompleteClick = { viewModel.completeTask(it) },
            onEditClick = { task ->
                // Envía únicamente el id; TaskFormActivity consultará el registro completo en Room.
                taskFormLauncher.launch(
                    Intent(this, TaskFormActivity::class.java)
                        .putExtra(TaskFormActivity.EXTRA_TASK_ID, task.id)
                )
            },
            onDeleteClick = { task -> confirmDelete(task) },
            onLocationClick = { task -> MapIntentHelper.open(this, task) }
        )
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tasksRecyclerView.adapter = taskAdapter
    }

    private fun confirmDelete(task: TaskEntity) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete)
            .setPositiveButton(R.string.yes_delete) { _, _ -> viewModel.deleteTask(task) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
