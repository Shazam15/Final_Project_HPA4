package com.utp.finalproject.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utp.finalproject.data.local.entity.ActivityHistoryEntity
import com.utp.finalproject.databinding.ItemHistoryBinding
import com.utp.finalproject.utils.Formatters

class HistoryAdapter : ListAdapter<ActivityHistoryEntity, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ActivityHistoryEntity) {
            binding.historyTitleText.text = item.title
            binding.historyDetailText.text = item.detail
            binding.historyDateText.text = Formatters.formatDateTime(item.createdAtMillis)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ActivityHistoryEntity>() {
        override fun areItemsTheSame(oldItem: ActivityHistoryEntity, newItem: ActivityHistoryEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivityHistoryEntity, newItem: ActivityHistoryEntity): Boolean {
            return oldItem == newItem
        }
    }
}
