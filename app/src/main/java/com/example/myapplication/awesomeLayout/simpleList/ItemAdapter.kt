package com.example.myapplication.awesomeLayout.simpleList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class ItemAdapter(private val onItemClickListener: ItemListFragment.OnItemClickListener) :
    ListAdapter<Item, ItemAdapter.ItemViewHolder>(object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }

    }) {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClickListener.onItemClick(getItem(absoluteAdapterPosition))
            }
        }

        fun bind(item: Item) = with(itemView) {
            itemView.tag = item
            findViewById<TextView>(R.id.txtTitle).text = item.title
            findViewById<TextView>(R.id.txtContent).text = item.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}