package com.snapvocab.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snapvocab.app.R
import com.snapvocab.app.data.model.DetectedObject
import com.snapvocab.app.data.model.WordItem

class ObjectAdapter(private val onWordSelected: (WordItem) -> Unit) : RecyclerView.Adapter<ObjectAdapter.ViewHolder>() {

    private var items = listOf<DetectedObject>()

    fun submitList(list: List<DetectedObject>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_object, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel = itemView.findViewById<android.widget.TextView>(R.id.tvLabel)
        private val rvWords = itemView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvWords)

        fun bind(obj: DetectedObject) {
            tvLabel.text = obj.label
            val wordAdapter = WordAdapter { word -> onWordSelected(word) }
            rvWords.adapter = wordAdapter
            rvWords.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(itemView.context)
            wordAdapter.submitList(obj.words)
        }
    }
}