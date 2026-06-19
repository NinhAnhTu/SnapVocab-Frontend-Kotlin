package com.snapvocab.app.ui.postcard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.data.model.CommentCreate
import com.snapvocab.app.databinding.ActivityPostcardCommentsBinding
import kotlinx.coroutines.launch

class PostcardCommentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostcardCommentsBinding
    private lateinit var adapter: CommentAdapter

    private var postcardId: String = ""
    private var postcardWord: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostcardCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postcardId = intent.getStringExtra(EXTRA_POSTCARD_ID) ?: ""
        postcardWord = intent.getStringExtra(EXTRA_POSTCARD_WORD) ?: ""

        if (postcardId.isBlank()) {
            Toast.makeText(this, "Thiếu postcard id", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        loadComments()

        binding.btnSendComment.setOnClickListener {
            sendComment()
        }
    }

    private fun setupViews() {
        binding.tvCommentTitle.text = "Comments: $postcardWord"

        adapter = CommentAdapter()

        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = adapter
    }

    private fun loadComments() {
        lifecycleScope.launch {
            try {
                setLoading(true)

                val response = RetrofitClient.apiService.getPostcardComments(
                    postcardId = postcardId,
                    page = 1,
                    limit = 50
                )

                if (response.isSuccessful) {
                    adapter.submitList(response.body().orEmpty())
                } else {
                    Toast.makeText(
                        this@PostcardCommentsActivity,
                        "Không tải được comment: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PostcardCommentsActivity,
                    "Lỗi comment: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun sendComment() {
        val content = binding.etComment.text.toString().trim()

        if (content.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                binding.btnSendComment.isEnabled = false

                val response = RetrofitClient.apiService.addPostcardComment(
                    postcardId = postcardId,
                    body = CommentCreate(content)
                )

                if (response.isSuccessful) {
                    val comment = response.body()

                    if (comment != null) {
                        adapter.addComment(comment)
                        binding.etComment.setText("")
                        binding.rvComments.scrollToPosition(adapter.itemCount - 1)
                    }
                } else {
                    Toast.makeText(
                        this@PostcardCommentsActivity,
                        "Gửi comment thất bại: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PostcardCommentsActivity,
                    "Lỗi gửi comment: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnSendComment.isEnabled = true
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarComments.visibility = if (isLoading) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    companion object {
        const val EXTRA_POSTCARD_ID = "extra_postcard_id"
        const val EXTRA_POSTCARD_WORD = "extra_postcard_word"
    }
}