package com.snapvocab.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snapvocab.app.R
import com.snapvocab.app.data.model.WordItem

class WordAdapter(private val onWordClick: (WordItem) -> Unit) : RecyclerView.Adapter<WordAdapter.ViewHolder>() {
    private var items = listOf<WordItem>()

    fun submitList(list: List<WordItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = items[position]
        holder.bind(word)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvWord = itemView.findViewById<android.widget.TextView>(R.id.tvWord)
        private val tvMeaning = itemView.findViewById<android.widget.TextView>(R.id.tvMeaning)
        fun bind(word: WordItem) {
            tvWord.text = word.word
            tvMeaning.text = word.meaning ?: ""
            itemView.setOnClickListener { onWordClick(word) }
        }
    }
}