package com.snapvocab.app.ui.postcard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snapvocab.app.data.model.CommentDto
import com.snapvocab.app.databinding.ItemCommentBinding

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val items = mutableListOf<CommentDto>()

    fun submitList(newItems: List<CommentDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addComment(comment: CommentDto) {
        items.add(comment)
        notifyItemInserted(items.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class CommentViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CommentDto) {
            binding.tvCommentUser.text = item.senderUsername
            binding.tvCommentContent.text = item.content
            binding.tvCommentTime.text = item.createdAt
        }
    }
}