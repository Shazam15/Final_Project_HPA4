package com.utp.finalproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.data.local.entity.TaskEntity
import com.utp.finalproject.data.repository.HomePetRepository
import com.utp.finalproject.databinding.ActivityMainBinding
import com.utp.finalproject.ui.adapters.TaskAdapter
import com.utp.finalproject.ui.PetArtwork
import com.utp.finalproject.viewmodel.HomeUiState
import com.utp.finalproject.viewmodel.HomeViewModel
import com.utp.finalproject.utils.MapIntentHelper
import com.utp.finalproject.viewmodel.RepositoryViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var taskAdapter: TaskAdapter
    private val taskFormLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK &&
            result.data?.getBooleanExtra(TaskFormActivity.EXTRA_TASK_CHANGED, false) == true
        ) {
            // Room emite la lista actualizada mediante Flow; el resultado confirma el cambio.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = HomePetRepository(applicationContext)
        if (!repository.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            RepositoryViewModelFactory(repository)
        )[HomeViewModel::class.java]

        setupTaskList()
        setupNavigation()

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                render(state)
            }
        }
    }

    private fun setupTaskList() {
        taskAdapter = TaskAdapter(
            onCompleteClick = { task -> viewModel.completeTask(task) },
            onEditClick = { task -> openTaskForm(task.id) },
            onDeleteClick = { },
            onLocationClick = { task -> MapIntentHelper.open(this, task) }
        )
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tasksRecyclerView.adapter = taskAdapter
    }

    private fun setupNavigation() {
        binding.addTaskButton.setOnClickListener { openTaskForm() }
        binding.tasksButton.setOnClickListener { startActivity(Intent(this, TaskListActivity::class.java)) }
        binding.rewardsButton.setOnClickListener { startActivity(Intent(this, RewardsActivity::class.java)) }
        binding.historyButton.setOnClickListener { startActivity(Intent(this, HistoryActivity::class.java)) }
        binding.settingsButton.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }

    private fun render(state: HomeUiState) {
        val pet = state.pet
        if (pet == null) {
            binding.greetingText.text = getString(R.string.homepet_title)
            return
        }

        binding.greetingText.text = getString(R.string.dashboard_greeting, pet.name)
        binding.petNameText.text = pet.name
        binding.petImage.setImageResource(PetArtwork.pet(pet.type, pet.mood))
        renderRewardLayer(
            binding.petBackgroundImage,
            pet.equippedBackground == "Fondo jardín",
            R.drawable.reward_garden_bg
        )
        renderRewardLayer(
            binding.petCapeImage,
            pet.equippedClothing == "Capa de héroe",
            R.drawable.reward_hero_cape
        )
        renderRewardLayer(
            binding.petGoldImage,
            pet.equippedColor == "Color dorado",
            R.drawable.reward_gold_color
        )
        renderRewardLayer(
            binding.petCollarImage,
            pet.equippedAccessory == "Collar azul",
            R.drawable.reward_collar_blue
        )
        renderRewardLayer(
            binding.petHatImage,
            pet.equippedHat == "Sombrero verde",
            R.drawable.reward_hat_green
        )
        binding.petStatusText.text = getString(R.string.pet_status_line, pet.level, pet.coins, pet.mood)
        binding.petStatsText.text = getString(
            R.string.pet_stats,
            pet.health,
            pet.hunger,
            pet.energy,
            pet.happiness
        )
        binding.petMessageText.text = petMessage(pet)
        binding.xpProgress.max = state.xpMax
        binding.xpProgress.progress = pet.experience
        binding.emptyText.visibility = if (state.urgentTasks.isEmpty()) View.VISIBLE else View.GONE
        taskAdapter.submitList(state.urgentTasks)
    }

    private fun petMessage(pet: PetEntity): String {
        return when (pet.mood) {
            PetEntity.MOOD_HAPPY -> getString(R.string.pet_message_happy, pet.name)
            PetEntity.MOOD_SAD -> getString(R.string.pet_message_sad, pet.name)
            PetEntity.MOOD_SICK -> getString(R.string.pet_message_sick, pet.name)
            PetEntity.MOOD_DANGER -> getString(R.string.pet_message_danger, pet.name)
            else -> getString(R.string.pet_message_neutral, pet.name)
        }
    }

    private fun renderRewardLayer(image: android.widget.ImageView, visible: Boolean, drawable: Int) {
        image.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) image.setImageResource(drawable)
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
