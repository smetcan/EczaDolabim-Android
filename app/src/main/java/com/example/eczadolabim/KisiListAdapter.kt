package com.example.eczadolabim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class KisiListAdapter(private val onKisiSil: (Kisi) -> Unit) :
    ListAdapter<Kisi, KisiListAdapter.KisiViewHolder>(KisiComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KisiViewHolder {
        return KisiViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: KisiViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, onKisiSil)
    }

    class KisiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val kisiIsim: TextView = itemView.findViewById(R.id.textView_kisi_isim)
        private val silButton: ImageButton = itemView.findViewById(R.id.button_kisi_sil)

        fun bind(kisi: Kisi, onKisiSil: (Kisi) -> Unit) {
            kisiIsim.text = kisi.isim
            silButton.setOnClickListener {
                onKisiSil(kisi)
            }
        }

        companion object {
            fun create(parent: ViewGroup): KisiViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_kisi, parent, false)
                return KisiViewHolder(view)
            }
        }
    }

    class KisiComparator : DiffUtil.ItemCallback<Kisi>() {
        override fun areItemsTheSame(oldItem: Kisi, newItem: Kisi): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Kisi, newItem: Kisi): Boolean {
            return oldItem == newItem
        }
    }
}