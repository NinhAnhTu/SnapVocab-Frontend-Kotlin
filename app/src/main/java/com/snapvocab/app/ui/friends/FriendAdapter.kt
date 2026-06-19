package com.snapvocab.app.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snapvocab.app.data.model.UserSearch
import com.snapvocab.app.databinding.ItemFriendBinding

class FriendAdapter(
    private val onUnfriend: (UserSearch) -> Unit
) : RecyclerView.Adapter<FriendAdapter.ViewHolder>() {

    private var items = listOf<UserSearch>()

    fun submitList(list: List<UserSearch>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = items[position]
        holder.bind(user)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserSearch) {
            binding.tvName.text = user.username
            // Ẩn tvEmail vì không có dữ liệu
            binding.tvEmail.visibility = android.view.View.GONE
            binding.tvAvatar.text = user.username.take(1).uppercase()
            binding.btnMore.setOnClickListener { onUnfriend(user) }
        }
    }
}