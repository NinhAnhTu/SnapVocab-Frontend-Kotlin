package com.snapvocab.app.ui.postcard

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.data.model.CreatePostcardData
import com.snapvocab.app.data.model.DetectedObject  // <-- import thêm
import com.snapvocab.app.databinding.ActivityCreatePostcardBinding
import com.snapvocab.app.utils.FileUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class CreatePostcardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostcardBinding
    private lateinit var receiverAdapter: ReceiverSelectAdapter

    private var imageUriOrPath: String = ""
    private var word: String = ""
    private var wordMeaning: String? = null
    private var wordPronunciation: String? = null
    private var objectsJson: String? = null  // <-- thêm biến

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreatePostcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        readIntentData()
        setupViews()
        loadFriends()

        binding.btnSendPostcard.setOnClickListener {
            createPostcard()
        }
    }

    private fun readIntentData() {
        imageUriOrPath = intent.getStringExtra(EXTRA_IMAGE_URI) ?: ""
        word = intent.getStringExtra(EXTRA_WORD) ?: ""
        wordMeaning = intent.getStringExtra(EXTRA_WORD_MEANING)
        wordPronunciation = intent.getStringExtra(EXTRA_WORD_PRONUNCIATION)
        objectsJson = intent.getStringExtra(EXTRA_OBJECTS)  // <-- thêm
    }

    private fun setupViews() {
        if (imageUriOrPath.isNotBlank()) {
            binding.ivPreview.setImageURI(Uri.parse(imageUriOrPath))
        }

        binding.tvWord.text = word.ifBlank { "No word selected" }
        binding.tvMeaning.text = wordMeaning ?: ""
        binding.tvPronunciation.text = wordPronunciation ?: ""

        receiverAdapter = ReceiverSelectAdapter()

        binding.rvReceivers.apply {
            layoutManager = LinearLayoutManager(this@CreatePostcardActivity)
            adapter = receiverAdapter
        }
    }

    private fun loadFriends() {
        lifecycleScope.launch {
            try {
                setLoading(true)

                val response = RetrofitClient.apiService.getFriendsForPostcard()

                if (response.isSuccessful) {
                    val friends = response.body().orEmpty()
                    receiverAdapter.submitList(friends)

                    if (friends.isEmpty()) {
                        Toast.makeText(
                            this@CreatePostcardActivity,
                            "Bạn chưa có bạn bè để gửi postcard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@CreatePostcardActivity,
                        "Không tải được danh sách bạn bè: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CreatePostcardActivity,
                    "Lỗi tải bạn bè: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun createPostcard() {
        if (imageUriOrPath.isBlank()) {
            Toast.makeText(this, "Thiếu ảnh postcard", Toast.LENGTH_SHORT).show()
            return
        }

        if (word.isBlank()) {
            Toast.makeText(this, "Thiếu từ vựng", Toast.LENGTH_SHORT).show()
            return
        }

        val receiverIds = receiverAdapter.getSelectedIds()

        if (receiverIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 người nhận", Toast.LENGTH_SHORT).show()
            return
        }

        val visibility = if (binding.rbPublic.isChecked) {
            "public"
        } else {
            "friends"
        }

        val note = binding.etNote.text.toString().trim().ifBlank {
            null
        }

        // Parse objects từ JSON nếu có
        val objects = objectsJson?.let {
            try {
                Gson().fromJson(it, Array<DetectedObject>::class.java).toList()
            } catch (e: Exception) {
                null
            }
        }

        val postcardData = CreatePostcardData(
            word = word,
            wordMeaning = wordMeaning,
            wordPronunciation = wordPronunciation,
            note = note,
            filterMetadata = null,
            visibility = visibility,
            receiverIds = receiverIds,
            objects = objects  // <-- thêm trường này
        )

        lifecycleScope.launch {
            try {
                setLoading(true)

                val imageFile = FileUtils.fileFromUriOrPath(
                    context = this@CreatePostcardActivity,
                    uriOrPath = imageUriOrPath
                )

                val dataJson = Gson().toJson(postcardData)

                val dataBody = dataJson.toRequestBody(
                    "text/plain; charset=utf-8".toMediaType()
                )

                val imageBody = imageFile.asRequestBody(
                    "image/*".toMediaType()
                )

                val imagePart = MultipartBody.Part.createFormData(
                    name = "image",
                    filename = imageFile.name,
                    body = imageBody
                )

                // Tạo RequestBody cho objects nếu có
                val objectsBody = objectsJson?.toRequestBody(
                    "application/json; charset=utf-8".toMediaType()
                )

                val response = RetrofitClient.apiService.createPostcard(
                    data = dataBody,
                    image = imagePart,
                    objects = objectsBody  // <-- thêm tham số này
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@CreatePostcardActivity,
                        "Gửi postcard thành công",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = android.content.Intent(
                        this@CreatePostcardActivity,
                        PostcardFeedActivity::class.java
                    ).apply {
                        putExtra(
                            PostcardFeedActivity.EXTRA_INITIAL_MODE,
                            PostcardFeedActivity.MODE_SENT
                        )
                    }

                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()

                    Toast.makeText(
                        this@CreatePostcardActivity,
                        "Gửi thất bại: ${response.code()} ${errorBody ?: ""}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CreatePostcardActivity,
                    "Lỗi gửi postcard: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSendPostcard.isEnabled = !isLoading
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_WORD = "extra_word"
        const val EXTRA_WORD_MEANING = "extra_word_meaning"
        const val EXTRA_WORD_PRONUNCIATION = "extra_word_pronunciation"
        const val EXTRA_OBJECTS = "extra_objects"  // <-- thêm hằng số
    }
}