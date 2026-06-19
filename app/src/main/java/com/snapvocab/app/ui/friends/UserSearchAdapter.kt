package com.snapvocab.app.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snapvocab.app.R
import com.snapvocab.app.data.model.UserSearch
import com.snapvocab.app.databinding.ItemUserSearchBinding

class UserSearchAdapter(
    private val onSendRequest: (UserSearch) -> Unit
) : RecyclerView.Adapter<UserSearchAdapter.ViewHolder>() {

    private var items = listOf<UserSearch>()

    fun submitList(list: List<UserSearch>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = items[position]
        holder.bind(user)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemUserSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserSearch) {
            binding.tvUsername.text = user.username
            binding.btnAddFriend.setOnClickListener { onSendRequest(user) }
        }
    }
}