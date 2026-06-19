package com.snapvocab.app.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.snapvocab.app.R
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.data.model.AddVocabularyRequest
import com.snapvocab.app.data.model.DetectedObject
import com.snapvocab.app.data.model.WordItem
import com.snapvocab.app.ui.postcard.CreatePostcardActivity
import com.snapvocab.app.ui.vocabulary.VocabularyActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import com.google.gson.Gson

class ActivityAnalyze : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private lateinit var btnAnalyze: Button
    private lateinit var analyzingLayout: LinearLayout
    private lateinit var previewLayout: LinearLayout
    private lateinit var resultsLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var txtProgress: TextView
    private lateinit var objectListContainer: LinearLayout
    private lateinit var btnViewVocabulary: Button
    private lateinit var darkOverlay: View

    private var imageUri: Uri? = null
    private var detectedObjects: List<DetectedObject> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_analyze)

        imagePreview = findViewById(R.id.image_preview)
        val btnBack = findViewById<View>(R.id.btn_back)

        btnAnalyze = findViewById(R.id.btn_analyze)
        analyzingLayout = findViewById(R.id.analyzing_layout)
        previewLayout = findViewById(R.id.preview_layout)
        resultsLayout = findViewById(R.id.results_layout)
        progressBar = findViewById(R.id.progress_bar)
        txtProgress = findViewById(R.id.txt_progress)
        objectListContainer = findViewById(R.id.object_list_container)
        btnViewVocabulary = findViewById(R.id.btn_view_vocabulary)
        darkOverlay = findViewById(R.id.dark_overlay)

        val imageUriString = intent.getStringExtra("image_uri")

        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString)
            imagePreview.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "Không nhận được ảnh", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnAnalyze.setOnClickListener {
            val uri = imageUri
            if (uri == null) {
                Toast.makeText(this, "Không có ảnh để phân tích", Toast.LENGTH_SHORT).show()
            } else {
                sendImageForAnalysis(uri)
            }
        }

        btnViewVocabulary.setOnClickListener {
            startActivity(Intent(this, VocabularyActivity::class.java))
        }

        // Fix: Cộng dồn hệ thống insets với padding có sẵn từ XML
        val topBar = findViewById<View>(R.id.topBar)
        val initialPaddingTop = topBar.paddingTop
        val initialPaddingStart = topBar.paddingStart
        val initialPaddingEnd = topBar.paddingEnd
        val initialPaddingBottom = topBar.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                initialPaddingStart,
                initialPaddingTop + systemBars.top,
                initialPaddingEnd,
                initialPaddingBottom
            )
            insets
        }
    }

    private fun sendImageForAnalysis(uri: Uri) {
        lifecycleScope.launch {
            try {
                showAnalyzingState()

                val file = uriToCacheFile(uri)
                val mimeType = getMimeType(uri)
                val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())

                val part = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = file.name,
                    body = requestBody
                )

                val response = RetrofitClient.apiService.analyzeImage(
                    file = part,
                    maxObjects = 5,
                    debugLowConfidence = true,
                    useGeminiFallback = true,
                    roiX1 = null,
                    roiY1 = null,
                    roiX2 = null,
                    roiY2 = null
                )

                if (response.isSuccessful) {
                    val result = response.body()

                    if (result != null && result.objects.isNotEmpty()) {
                        detectedObjects = result.objects
                        displayResults(detectedObjects)
                    } else {
                        Toast.makeText(
                            this@ActivityAnalyze,
                            "Không tìm thấy vật thể trong ảnh",
                            Toast.LENGTH_SHORT
                        ).show()
                        resetToPreview()
                    }
                } else {
                    val errorMessage = getErrorMessage(
                        response.code(),
                        response.errorBody()?.string()
                    )
                    Toast.makeText(
                        this@ActivityAnalyze,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    resetToPreview()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ActivityAnalyze,
                    "Lỗi phân tích ảnh: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                resetToPreview()
            }
        }
    }

    private fun uriToCacheFile(uri: Uri): File {
        val extension = getFileExtension(uri)
        val file = File(cacheDir, "temp_analyze_${System.currentTimeMillis()}.$extension")

        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Không thể mở ảnh đã chọn")

        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    private fun getMimeType(uri: Uri): String {
        return contentResolver.getType(uri) ?: "image/jpeg"
    }

    private fun getFileExtension(uri: Uri): String {
        val mimeType = getMimeType(uri)
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?: "jpg"
    }

    private fun showAnalyzingState() {
        previewLayout.visibility = View.GONE
        analyzingLayout.visibility = View.VISIBLE
        resultsLayout.visibility = View.GONE
        darkOverlay.visibility = View.VISIBLE

        btnAnalyze.isEnabled = false
        progressBar.progress = 30
        txtProgress.text = "Đang nhận diện vật thể..."
    }

    private fun displayResults(objects: List<DetectedObject>) {
        objectListContainer.removeAllViews()

        for (obj in objects) {
            val objectView = layoutInflater.inflate(
                R.layout.item_object,
                objectListContainer,
                false
            )

            val tvLabel = objectView.findViewById<TextView>(R.id.tvLabel)
            val rvWords = objectView.findViewById<RecyclerView>(R.id.rvWords)

            tvLabel.text = buildObjectTitle(obj)

            val wordAdapter = WordAdapter { word ->
                showWordActionDialog(word)
            }

            rvWords.adapter = wordAdapter
            rvWords.layoutManager = LinearLayoutManager(this)
            wordAdapter.submitList(obj.words)

            objectListContainer.addView(objectView)
        }

        previewLayout.visibility = View.GONE
        analyzingLayout.visibility = View.GONE
        resultsLayout.visibility = View.VISIBLE
        darkOverlay.visibility = View.VISIBLE
        btnAnalyze.isEnabled = true
    }

    private fun showWordActionDialog(wordItem: WordItem) {
        if (wordItem.word.isBlank()) {
            Toast.makeText(this, "Từ không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_word_actions, null)

        view.findViewById<TextView>(R.id.tvSheetWord).text = wordItem.word
        view.findViewById<TextView>(R.id.tvSheetMeaning).text = wordItem.meaning

        view.findViewById<View>(R.id.btnSaveVocab).setOnClickListener {
            saveToVocabulary(wordItem)
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btnCreatePostcard).setOnClickListener {
            openCreatePostcard(wordItem)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun openCreatePostcard(wordItem: WordItem) {
        val uri = imageUri
        if (uri == null) {
            Toast.makeText(this, "Không tìm thấy ảnh để tạo postcard", Toast.LENGTH_SHORT).show()
            return
        }

        // Chuyển danh sách objects thành JSON
        val objectsJson = if (detectedObjects.isNotEmpty()) {
            Gson().toJson(detectedObjects)
        } else null

        val intent = Intent(this, CreatePostcardActivity::class.java).apply {
            putExtra(CreatePostcardActivity.EXTRA_IMAGE_URI, uri.toString())
            putExtra(CreatePostcardActivity.EXTRA_WORD, wordItem.word)
            putExtra(CreatePostcardActivity.EXTRA_WORD_MEANING, wordItem.meaning)
            putExtra(CreatePostcardActivity.EXTRA_WORD_PRONUNCIATION, wordItem.pronunciation)
            putExtra(CreatePostcardActivity.EXTRA_OBJECTS, objectsJson)  // <-- thêm dòng này
        }
        startActivity(intent)
    }

    private fun buildObjectTitle(obj: DetectedObject): String {
        val percent = (obj.confidence * 100).toInt()
        val source = obj.detectionSource ?: "unknown"
        val lowText = if (obj.isLowConfidence) " • cần kiểm tra" else ""
        return "${obj.label} • $percent% • $source$lowText"
    }

    private fun saveToVocabulary(wordItem: WordItem) {
        if (wordItem.word.isBlank()) {
            Toast.makeText(this, "Từ không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = AddVocabularyRequest(
                    word = wordItem.word,
                    meaning = wordItem.meaning,
                    pronunciation = wordItem.pronunciation,
                    partOfSpeech = wordItem.partOfSpeech
                )

                val response = RetrofitClient.apiService.addVocabulary(request)

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ActivityAnalyze,
                        "Đã lưu từ '${wordItem.word}'",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (response.code() == 400 && errorBody?.contains("already", ignoreCase = true) == true) {
                        Toast.makeText(
                            this@ActivityAnalyze,
                            "Từ '${wordItem.word}' đã có trong danh sách",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ActivityAnalyze,
                            "Lưu từ thất bại: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ActivityAnalyze,
                    "Lỗi lưu từ: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun resetToPreview() {
        previewLayout.visibility = View.VISIBLE
        analyzingLayout.visibility = View.GONE
        resultsLayout.visibility = View.GONE
        darkOverlay.visibility = View.GONE
        btnAnalyze.isEnabled = true
    }

    private fun getErrorMessage(code: Int, errorBody: String?): String {
        return when (code) {
            401 -> "Bạn cần đăng nhập lại"
            413 -> "Ảnh quá lớn, vui lòng chọn ảnh nhỏ hơn"
            422 -> "Dữ liệu gửi lên không đúng định dạng multipart field 'file'"
            500 -> "Server lỗi khi phân tích ảnh"
            else -> "Lỗi phân tích ảnh: $code ${errorBody ?: ""}"
        }
    }
}