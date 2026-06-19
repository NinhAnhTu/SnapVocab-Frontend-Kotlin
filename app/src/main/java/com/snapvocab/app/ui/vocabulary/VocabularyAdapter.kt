package com.snapvocab.app.ui.vocabulary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snapvocab.app.data.model.VocabularyItem
import com.snapvocab.app.databinding.ItemVocabularyBinding

class VocabularyAdapter(
    private val onDetail: (VocabularyItem) -> Unit,
    private val onDelete: (VocabularyItem) -> Unit
) : RecyclerView.Adapter<VocabularyAdapter.ViewHolder>() {

    private var items = listOf<VocabularyItem>()

    fun submitList(list: List<VocabularyItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVocabularyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemVocabularyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vocab: VocabularyItem) {
            binding.tvWordTitle.text = vocab.word
            binding.tvWordMeaning.text = vocab.meaning ?: "No meaning yet"
            binding.tvWordIPA.text = vocab.pronunciation ?: "/pronunciation/"

            binding.root.setOnClickListener {
                onDetail(vocab)
            }

            // Tạm thời: nhấn giữ item để xóa
            binding.root.setOnLongClickListener {
                onDelete(vocab)
                true
            }
        }
    }
}
