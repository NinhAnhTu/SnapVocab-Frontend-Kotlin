package com.snapvocab.app.ui.postcard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.data.model.Postcard
import com.snapvocab.app.databinding.ItemPostcardBinding

class PostcardFeedAdapter(
    private val onLikeClick: (Postcard) -> Unit,
    private val onCommentClick: (Postcard) -> Unit,
    private val onAddVocabularyClick: (Postcard) -> Unit
) : RecyclerView.Adapter<PostcardFeedAdapter.PostcardViewHolder>() {

    private val items = mutableListOf<Postcard>()

    fun submitList(newItems: List<Postcard>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateLikeState(postcardId: String, liked: Boolean, likeCount: Int) {
        val index = items.indexOfFirst { it.id == postcardId }

        if (index == -1) return

        val oldItem = items[index]

        items[index] = oldItem.copy(
            likedByCurrentUser = liked,
            likeCount = likeCount
        )

        notifyItemChanged(index)
    }

    fun increaseCommentCount(postcardId: String) {
        val index = items.indexOfFirst { it.id == postcardId }

        if (index == -1) return

        val oldItem = items[index]

        items[index] = oldItem.copy(
            commentCount = oldItem.commentCount + 1
        )

        notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostcardViewHolder {
        val binding = ItemPostcardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PostcardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostcardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class PostcardViewHolder(
        private val binding: ItemPostcardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Postcard) {
            binding.tvSender.text = item.senderUsername ?: "Unknown user"
            binding.tvTime.text = item.createdAt

            binding.tvWord.text = item.word
            binding.tvPronunciation.text = item.wordPronunciation ?: ""
            binding.tvMeaning.text = item.wordMeaning ?: ""
            binding.tvNote.text = item.note ?: ""

            binding.tvLikeCount.text = "${item.likeCount} likes"
            binding.tvCommentCount.text = "${item.commentCount} comments"

            binding.btnLike.text = if (item.likedByCurrentUser) {
                "♥ Liked"
            } else {
                "♡ Like"
            }

            binding.btnAddVocabulary.text = "Thêm '${item.word}' vào từ vựng"

            Glide.with(binding.root.context)
                .load(RetrofitClient.toAbsoluteUrl(item.imageUrl))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivPostcard)

            if (!item.senderAvatarUrl.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load(RetrofitClient.toAbsoluteUrl(item.senderAvatarUrl))
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            binding.btnLike.setOnClickListener {
                onLikeClick(item)
            }

            binding.btnComment.setOnClickListener {
                onCommentClick(item)
            }

            binding.btnAddVocabulary.setOnClickListener {
                onAddVocabularyClick(item)
            }
        }
    }
}