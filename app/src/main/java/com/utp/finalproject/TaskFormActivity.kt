package com.utp.finalproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.utp.finalproject.data.local.entity.TaskEntity
import com.utp.finalproject.data.repository.HomePetRepository
import com.utp.finalproject.databinding.ActivityTaskFormBinding
import com.utp.finalproject.utils.Formatters
import com.utp.finalproject.viewmodel.RepositoryViewModelFactory
import com.utp.finalproject.viewmodel.TaskFormViewModel
import kotlinx.coroutines.launch

class TaskFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskFormBinding
    private lateinit var viewModel: TaskFormViewModel
    private var taskId: Long = 0L
    private var currentTask: TaskEntity? = null
    private var locationName: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var placeId: String? = null

    // Flujo de ida y vuelta: TaskForm envía ubicación inicial a LocationPicker y
    // recibe nombre/coordenadas como resultado para incorporarlos a TaskEntity.
    private val locationPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult
        locationName = data.getStringExtra(LocationPickerActivity.EXTRA_RESULT_NAME)
        latitude = data.getDoubleExtra(LocationPickerActivity.EXTRA_RESULT_LATITUDE, 0.0)
        longitude = data.getDoubleExtra(LocationPickerActivity.EXTRA_RESULT_LONGITUDE, 0.0)
        placeId = data.getStringExtra(LocationPickerActivity.EXTRA_RESULT_PLACE_ID)
        renderLocation()
    }

    private val categories = listOf("Limpieza", "Compras", "Cocina", "Plantas", "Organizacion", "Mascota", "Otro")
    private val priorities = listOf(TaskEntity.PRIORITY_LOW, TaskEntity.PRIORITY_MEDIUM, TaskEntity.PRIORITY_HIGH)
    private val frequencies = listOf(
        TaskEntity.FREQUENCY_ONCE,
        TaskEntity.FREQUENCY_DAILY,
        TaskEntity.FREQUENCY_WEEKLY,
        TaskEntity.FREQUENCY_MONTHLY
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            RepositoryViewModelFactory(HomePetRepository(applicationContext))
        )[TaskFormViewModel::class.java]

        // El id proviene de MainActivity o TaskListActivity; 0L representa una tarea nueva.
        taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        setupSpinners()
        binding.deleteTaskButton.visibility = if (taskId == 0L) View.GONE else View.VISIBLE
        viewModel.loadTask(taskId)

        binding.saveTaskButton.setOnClickListener { saveTask() }
        binding.deleteTaskButton.setOnClickListener { confirmDelete() }
        binding.selectLocationButton.setOnClickListener { openLocationPicker() }
        binding.clearLocationButton.setOnClickListener { clearLocation() }

        // Repository publica la tarea cargada o el fin del CRUD mediante el StateFlow del ViewModel.
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.task?.let { fillTask(it) }
                if (state.saved || state.deleted) {
                    // Devuelve la confirmación al launcher de la Activity que abrió el formulario.
                    setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_TASK_CHANGED, true))
                    finish()
                }
            }
        }
    }

    private fun setupSpinners() {
        bindSpinner(binding.categorySpinner, categories)
        bindSpinner(binding.prioritySpinner, priorities)
        bindSpinner(binding.frequencySpinner, frequencies)
    }

    private fun bindSpinner(spinner: android.widget.Spinner, values: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun fillTask(task: TaskEntity) {
        if (currentTask != null) return
        currentTask = task
        binding.titleInput.setText(task.title)
        binding.descriptionInput.setText(task.description)
        binding.dueDateInput.setText(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(task.dueAtMillis)))
        binding.categorySpinner.setSelection(categories.indexOf(task.category).coerceAtLeast(0))
        binding.prioritySpinner.setSelection(priorities.indexOf(task.priority).coerceAtLeast(0))
        binding.frequencySpinner.setSelection(frequencies.indexOf(task.frequency).coerceAtLeast(0))
        locationName = task.locationName
        latitude = task.latitude
        longitude = task.longitude
        placeId = task.placeId
        renderLocation()
    }

    private fun saveTask() {
        val title = binding.titleInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()
        val dueAt = Formatters.parseInputDateTime(binding.dueDateInput.text.toString().trim())

        if (title.isBlank() || dueAt == null) {
            Toast.makeText(this, if (title.isBlank()) R.string.task_required_fields else R.string.invalid_date, Toast.LENGTH_SHORT).show()
            return
        }

        val original = currentTask
        // Convierte los valores visuales en una entidad y la envía al ViewModel -> Repository -> DAO.
        viewModel.saveTask(
            TaskEntity(
                id = taskId,
                title = title,
                description = description,
                category = binding.categorySpinner.selectedItem.toString(),
                priority = binding.prioritySpinner.selectedItem.toString(),
                frequency = binding.frequencySpinner.selectedItem.toString(),
                status = original?.status ?: TaskEntity.STATUS_PENDING,
                createdAtMillis = original?.createdAtMillis ?: System.currentTimeMillis(),
                dueAtMillis = dueAt,
                completedAtMillis = original?.completedAtMillis,
                xpReward = original?.xpReward ?: 0,
                completedOnTime = original?.completedOnTime ?: false,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                placeId = placeId
            )
        )
    }

    private fun openLocationPicker() {
        // Los extras permiten que el mapa comience en la ubicación que ya tenía la tarea.
        val intent = Intent(this, LocationPickerActivity::class.java).apply {
            locationName?.let { putExtra(LocationPickerActivity.EXTRA_INITIAL_NAME, it) }
            latitude?.let { putExtra(LocationPickerActivity.EXTRA_INITIAL_LATITUDE, it) }
            longitude?.let { putExtra(LocationPickerActivity.EXTRA_INITIAL_LONGITUDE, it) }
        }
        locationPickerLauncher.launch(intent)
    }

    private fun clearLocation() {
        locationName = null
        latitude = null
        longitude = null
        placeId = null
        renderLocation()
    }

    private fun renderLocation() {
        val hasLocation = latitude != null && longitude != null
        binding.locationPreviewText.text = if (hasLocation) {
            locationName ?: getString(R.string.location_selected)
        } else {
            getString(R.string.no_location_selected)
        }
        binding.clearLocationButton.visibility = if (hasLocation) View.VISIBLE else View.GONE
    }

    private fun confirmDelete() {
        val task = currentTask ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete)
            .setPositiveButton(R.string.yes_delete) { _, _ -> viewModel.deleteTask(task) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    companion object {
        const val EXTRA_TASK_ID = "com.utp.finalproject.extra.TASK_ID"
        const val EXTRA_TASK_CHANGED = "com.utp.finalproject.extra.TASK_CHANGED"
    }
}
