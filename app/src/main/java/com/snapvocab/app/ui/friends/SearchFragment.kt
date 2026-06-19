package com.snapvocab.app.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.snapvocab.app.R
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.data.model.SendFriendRequest
import com.snapvocab.app.data.model.UserSearch
import com.snapvocab.app.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: UserSearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = UserSearchAdapter { user -> sendFriendRequest(user) }
        binding.rvSearchResults.adapter = adapter
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchUsers(query)
            }
        }
    }

    private fun searchUsers(query: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.searchUsers(query)
                if (response.isSuccessful) {
                    val results = response.body() ?: emptyList()
                    adapter.submitList(results)

                    if (results.isEmpty()) {
                        binding.llEmptyState.visibility = View.VISIBLE
                        binding.ivEmptyIcon.setImageResource(R.drawable.ic_search)
                        binding.tvEmptyMessage.text = "Không tìm thấy người dùng nào phù hợp"
                        binding.rvSearchResults.visibility = View.GONE
                    } else {
                        binding.llEmptyState.visibility = View.GONE
                        binding.rvSearchResults.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendFriendRequest(user: UserSearch) {
        lifecycleScope.launch {
            try {
                val request = SendFriendRequest(user.id)
                val response = RetrofitClient.apiService.sendFriendRequest(request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Đã gửi lời mời đến ${user.username}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gửi lời mời thất bại", Toast.LENGTH_SHORT).show()
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