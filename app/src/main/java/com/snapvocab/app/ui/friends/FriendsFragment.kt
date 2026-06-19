package com.snapvocab.app.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.data.model.UserSearch
import com.snapvocab.app.databinding.FragmentFriendsBinding
import kotlinx.coroutines.launch

class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FriendAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentFriendsBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FriendAdapter { friend -> unfriend(friend) }
        binding.rvFriends.adapter = adapter
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())
        loadFriends()
    }

    private fun loadFriends() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getFriends()
                if (response.isSuccessful) {
                    val friends = response.body() ?: emptyList()
                    adapter.submitList(friends)

                    if (friends.isEmpty()) {
                        binding.llEmptyState.visibility = View.VISIBLE
                        binding.rvFriends.visibility = View.GONE
                    } else {
                        binding.llEmptyState.visibility = View.GONE
                        binding.rvFriends.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi tải danh sách bạn", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun unfriend(user: UserSearch) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.unfriend(user.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Đã hủy kết bạn với ${user.username}", Toast.LENGTH_SHORT).show()
                    loadFriends()
                } else {
                    Toast.makeText(requireContext(), "Hủy kết bạn thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}