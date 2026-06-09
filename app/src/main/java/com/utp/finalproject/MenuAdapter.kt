package com.utp.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MenuAdapter(
    private val items: MutableList<String>
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val textView: TextView = view.findViewById(R.id.itemText)
        val checkBox: CheckBox = view.findViewById(R.id.itemCheck)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)

        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {

        holder.textView.text = items[position]

        holder.deleteButton.setOnClickListener {

            val currentPosition = holder.adapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {

                items.removeAt(currentPosition)

                notifyItemRemoved(currentPosition)
            }
        }
    }

    override fun getItemCount(): Int {

        return items.size
    }

    fun addItem(item: String) {

        items.add(item)

        notifyItemInserted(items.size - 1)
    }
}