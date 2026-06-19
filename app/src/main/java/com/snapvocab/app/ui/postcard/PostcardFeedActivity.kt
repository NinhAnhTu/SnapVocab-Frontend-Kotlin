package com.snapvocab.app.ui.postcard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.data.model.Postcard
import com.snapvocab.app.databinding.ActivityPostcardFeedBinding
import kotlinx.coroutines.launch

class PostcardFeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostcardFeedBinding
    private lateinit var adapter: PostcardFeedAdapter

    // Mặc định dùng MODE_ALL để xem cả postcard bản thân + postcard nhận
    private var currentMode: String = MODE_ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostcardFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentMode = intent.getStringExtra(EXTRA_INITIAL_MODE) ?: MODE_ALL

        setupRecyclerView()
        setupEvents()
        loadCurrentMode()
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            loadCurrentMode()
        }
    }

    private fun setupRecyclerView() {
        adapter = PostcardFeedAdapter(
            onLikeClick = { postcard ->
                toggleLike(postcard)
            },
            onCommentClick = { postcard ->
                openComments(postcard)
            },
            onAddVocabularyClick = { postcard ->
                addVocabularyFromPostcard(postcard)
            }
        )

        binding.rvPostcardFeed.layoutManager = LinearLayoutManager(this)
        binding.rvPostcardFeed.adapter = adapter
    }

    private fun setupEvents() {
        // Tận dụng nút btnReceived để hiện "Tất cả"
        binding.btnReceived.text = "Tất cả"
        binding.btnSent.text = "Đã gửi"

        binding.btnReceived.setOnClickListener {
            currentMode = MODE_ALL
            loadCurrentMode()
        }

        binding.btnSent.setOnClickListener {
            currentMode = MODE_SENT
            loadCurrentMode()
        }

        // Bấm giữ nút "Tất cả" để xem riêng postcard đã nhận
        binding.btnReceived.setOnLongClickListener {
            currentMode = MODE_RECEIVED
            loadCurrentMode()
            true
        }
    }

    private fun loadCurrentMode() {
        when (currentMode) {
            MODE_SENT -> loadSentPostcards()
            MODE_RECEIVED -> loadReceivedPostcards()
            else -> loadAllPostcards()
        }
    }

    private fun loadAllPostcards() {
        lifecycleScope.launch {
            try {
                setLoading(true)
                binding.tvFeedTitle.text = "Tất cả Postcard"

                val response = RetrofitClient.apiService.getAllFeedPostcards()

                if (response.isSuccessful) {
                    val list = response.body().orEmpty()
                    showPostcards(list)
                } else {
                    Toast.makeText(
                        this@PostcardFeedActivity,
                        "Không tải được tất cả postcard: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PostcardFeedActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun loadReceivedPostcards() {
        lifecycleScope.launch {
            try {
                setLoading(true)
                binding.tvFeedTitle.text = "Postcard đã nhận"

                val response = RetrofitClient.apiService.getReceivedPostcards()

                if (response.isSuccessful) {
                    val list = response.body().orEmpty()
                    showPostcards(list)
                } else {
                    Toast.makeText(
                        this@PostcardFeedActivity,
                        "Không tải được postcard đã nhận: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PostcardFeedActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun loadSentPostcards() {
        lifecycleScope.launch {
            try {
                setLoading(true)
                binding.tvFeedTitle.text = "Postcard đã gửi"

                val response = RetrofitClient.apiService.getSentPostcards()

                if (response.isSuccessful) {
                    val list = response.body().orEmpty()
                    showPostcards(list)
                } else {
                    Toast.makeText(
                        this@PostcardFeedActivity,
                        "Không tải được postcard đã gửi: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PostcardFeedActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun showPostcards(list: List<Postcard>) {
        adapter.submitList(list)

        binding.tvEmpty.visibility = if (list.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun toggleLike(postcard: Postcard) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.togglePostcardLike(postcard.id)

                if (response.isSuccessful) {
                    val result = response.body()

                    if (result != null) {
                        adapter.updateLikeState(
                            postcardId = result.postcardId,
                            liked = result.liked,
                            likeCount = result.likeCount
                        )
                    }
                } else {
                    Toast.makeText(
                        this@PostcardFeedActivity,
                        "Like thất bại: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PostcardFeedActivity,
                    "Lỗi like: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun addVocabularyFromPostcard(postcard: Postcard) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.addVocabularyFromPostcard(postcard.id)

                if (response.isSuccessful) {
                    val result = response.body()

                    if (result?.alreadyExists == true) {
                        Toast.makeText(
                            this@PostcardFeedActivity,
                            "Từ '${postcard.word}' đã có trong danh sách",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@PostcardFeedActivity,
                            "Đã thêm '${postcard.word}' vào từ vựng",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()

                    Toast.makeText(
                        this@PostcardFeedActivity,
                        "Thêm từ thất bại: ${response.code()} ${errorBody ?: ""}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PostcardFeedActivity,
                    "Lỗi thêm từ: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openComments(postcard: Postcard) {
        val intent = Intent(this, PostcardCommentsActivity::class.java).apply {
            putExtra(PostcardCommentsActivity.EXTRA_POSTCARD_ID, postcard.id)
            putExtra(PostcardCommentsActivity.EXTRA_POSTCARD_WORD, postcard.word)
        }

        startActivity(intent)
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarFeed.visibility = if (isLoading) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    companion object {
        const val EXTRA_INITIAL_MODE = "extra_initial_mode"

        const val MODE_ALL = "all"
        const val MODE_RECEIVED = "received"
        const val MODE_SENT = "sent"
    }
}