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
import com.snapvocab.app.data.model.FriendRequest
import com.snapvocab.app.databinding.FragmentRequestsBinding
import kotlinx.coroutines.launch

class RequestsFragment : Fragment() {
    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RequestAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RequestAdapter(
            onAccept = { request: FriendRequest -> acceptRequest(request) },
            onDecline = { request: FriendRequest -> rejectRequest(request) }
        )
        binding.rvRequests.adapter = adapter
        binding.rvRequests.layoutManager = LinearLayoutManager(requireContext())
        loadRequests()
    }

    private fun loadRequests() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPendingRequests()
                if (response.isSuccessful) {
                    val requests = response.body() ?: emptyList()
                    adapter.submitList(requests)

                    if (requests.isEmpty()) {
                        binding.llEmptyState.visibility = View.VISIBLE
                        binding.rvRequests.visibility = View.GONE
                    } else {
                        binding.llEmptyState.visibility = View.GONE
                        binding.rvRequests.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi tải lời mời", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun acceptRequest(request: FriendRequest) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.acceptFriendRequest(request.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Đã chấp nhận kết bạn", Toast.LENGTH_SHORT).show()
                    loadRequests()
                } else {
                    Toast.makeText(requireContext(), "Chấp nhận thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rejectRequest(request: FriendRequest) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.rejectFriendRequest(request.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Đã từ chối kết bạn", Toast.LENGTH_SHORT).show()
                    loadRequests()
                } else {
                    Toast.makeText(requireContext(), "Từ chối thất bại", Toast.LENGTH_SHORT).show()
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