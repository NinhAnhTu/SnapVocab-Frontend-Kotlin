package com.snapvocab.app.ui.postcard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.data.model.FriendUser
import com.snapvocab.app.databinding.ItemReceiverSelectBinding

class ReceiverSelectAdapter : RecyclerView.Adapter<ReceiverSelectAdapter.ReceiverViewHolder>() {

    private val items = mutableListOf<FriendUser>()
    private val selectedIds = mutableSetOf<String>()

    fun submitList(newItems: List<FriendUser>) {
        items.clear()
        items.addAll(newItems)
        selectedIds.clear()
        notifyDataSetChanged()
    }

    fun getSelectedIds(): List<String> {
        return selectedIds.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiverViewHolder {
        val binding = ItemReceiverSelectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ReceiverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiverViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ReceiverViewHolder(
        private val binding: ItemReceiverSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FriendUser) {
            val userId = item.realId()

            binding.tvUsername.text = item.displayName()
            binding.tvEmail.text = item.email ?: ""

            binding.checkBoxReceiver.setOnCheckedChangeListener(null)
            binding.checkBoxReceiver.isChecked = selectedIds.contains(userId)

            if (!item.avatarUrl.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load(RetrofitClient.toAbsoluteUrl(item.avatarUrl))
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            binding.rootReceiver.setOnClickListener {
                toggleSelection(userId)
                binding.checkBoxReceiver.isChecked = selectedIds.contains(userId)
            }

            binding.checkBoxReceiver.setOnCheckedChangeListener { _, isChecked ->
                if (userId.isBlank()) return@setOnCheckedChangeListener

                if (isChecked) {
                    selectedIds.add(userId)
                } else {
                    selectedIds.remove(userId)
                }
            }
        }

        private fun toggleSelection(userId: String) {
            if (userId.isBlank()) return

            if (selectedIds.contains(userId)) {
                selectedIds.remove(userId)
            } else {
                selectedIds.add(userId)
            }
        }
    }
}