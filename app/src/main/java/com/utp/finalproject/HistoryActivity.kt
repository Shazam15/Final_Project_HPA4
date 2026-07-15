package com.utp.finalproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.finalproject.data.repository.HomePetRepository
import com.utp.finalproject.databinding.ActivityHistoryBinding
import com.utp.finalproject.ui.adapters.HistoryAdapter
import com.utp.finalproject.viewmodel.HistoryViewModel
import com.utp.finalproject.viewmodel.RepositoryViewModelFactory
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private val historyAdapter = HistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            RepositoryViewModelFactory(HomePetRepository(applicationContext))
        )[HistoryViewModel::class.java]

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        // Historial: Room -> HistoryDao Flow -> Repository -> ViewModel -> Adapter.
        lifecycleScope.launch {
            viewModel.history.collect { history ->
                historyAdapter.submitList(history)
                // Cada cambio de historial solicita un resumen calculado desde las tablas Room.
                viewModel.refreshStats()
            }
        }
        // Las estadísticas regresan como StateFlow y aquí se convierten en texto y progreso visual.
        lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                if (stats != null) {
                    binding.statsText.text = getString(
                        R.string.stats_line,
                        stats.completedTasks,
                        stats.pendingTasks,
                        stats.overdueTasks
                    ) + "\n" + getString(R.string.completion_rate, stats.completionRate) +
                        "\n" + getString(R.string.streak_line, stats.currentStreak, stats.totalXp)
                    binding.completionProgress.progress = stats.completionRate
                }
            }
        }
    }
}
