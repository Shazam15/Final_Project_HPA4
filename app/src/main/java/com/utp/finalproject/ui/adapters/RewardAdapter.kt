package com.utp.finalproject.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utp.finalproject.R
import com.utp.finalproject.data.local.entity.RewardEntity
import com.utp.finalproject.databinding.ItemRewardBinding
import com.utp.finalproject.ui.PetArtwork

class RewardAdapter(
    private val onActionClick: (RewardEntity) -> Unit
) : ListAdapter<RewardEntity, RewardAdapter.RewardViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val binding = ItemRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RewardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RewardViewHolder(
        private val binding: ItemRewardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reward: RewardEntity) {
            // assetName se traduce a un VectorDrawable; el resto de campos se muestra como texto.
            val context = binding.root.context
            binding.rewardImage.setImageResource(PetArtwork.reward(reward.assetName))
            binding.rewardNameText.text = reward.name
            binding.rewardTypeText.text = context.getString(
                R.string.reward_requirement,
                reward.type,
                reward.requiredLevel,
                reward.cost
            )
            binding.rewardDescriptionText.text = reward.description
            binding.rewardActionButton.text = when {
                reward.isEquipped -> context.getString(R.string.equipped)
                reward.isUnlocked -> context.getString(R.string.equip)
                else -> context.getString(R.string.unlock)
            }
            binding.rewardActionButton.isEnabled = !reward.isEquipped
            // Devuelve la selección a RewardsActivity, que la envía al ViewModel y Repository.
            binding.rewardActionButton.setOnClickListener { onActionClick(reward) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<RewardEntity>() {
        override fun areItemsTheSame(oldItem: RewardEntity, newItem: RewardEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RewardEntity, newItem: RewardEntity): Boolean {
            return oldItem == newItem
        }
    }
}
