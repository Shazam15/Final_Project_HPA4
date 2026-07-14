package com.utp.finalproject

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.finalproject.data.repository.HomePetRepository
import com.utp.finalproject.databinding.ActivityRewardsBinding
import com.utp.finalproject.ui.adapters.RewardAdapter
import com.utp.finalproject.viewmodel.RepositoryViewModelFactory
import com.utp.finalproject.viewmodel.RewardsViewModel
import kotlinx.coroutines.launch

class RewardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardsBinding
    private lateinit var viewModel: RewardsViewModel
    private lateinit var rewardAdapter: RewardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            RepositoryViewModelFactory(HomePetRepository(applicationContext))
        )[RewardsViewModel::class.java]

        rewardAdapter = RewardAdapter { reward -> viewModel.buyOrEquip(reward) }
        binding.rewardsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.rewardsRecyclerView.adapter = rewardAdapter

        lifecycleScope.launch {
            viewModel.rewards.collect { rewards ->
                rewardAdapter.submitList(rewards)
            }
        }
        lifecycleScope.launch {
            viewModel.message.collect { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(this@RewardsActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
