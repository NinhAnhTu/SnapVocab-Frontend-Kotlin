package com.snapvocab.app.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snapvocab.app.data.model.FriendRequest
import com.snapvocab.app.databinding.ItemRequestBinding

class RequestAdapter(
    private val onAccept: (FriendRequest) -> Unit,
    private val onDecline: (FriendRequest) -> Unit
) : RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

    private var items = listOf<FriendRequest>()

    fun submitList(list: List<FriendRequest>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(request: FriendRequest) {
            binding.tvName.text = request.senderName
            // Ẩn email nếu không có
            binding.tvEmail.visibility = android.view.View.GONE
            binding.tvAvatar.text = request.senderName.take(1).uppercase()
            binding.btnAccept.setOnClickListener { onAccept(request) }
            binding.btnDecline.setOnClickListener { onDecline(request) }
        }
    }
}