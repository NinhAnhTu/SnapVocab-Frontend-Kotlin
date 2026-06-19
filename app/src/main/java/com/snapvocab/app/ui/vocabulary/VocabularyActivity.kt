package com.snapvocab.app.ui.vocabulary

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.snapvocab.app.R
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.data.model.VocabularyItem
import com.snapvocab.app.ui.friends.FriendActivity
import com.snapvocab.app.ui.home.MainActivity
import com.snapvocab.app.ui.postcard.CreatePostcardActivity
import com.snapvocab.app.ui.profile.ProfileActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class VocabularyActivity : AppCompatActivity() {

    private lateinit var rvVocabulary: RecyclerView
    private lateinit var adapter: VocabularyAdapter
    private lateinit var tvWordCount: TextView
    private lateinit var tvTotalValue: TextView
    private lateinit var tvThisWeekValue: TextView
    private lateinit var tvTodayValue: TextView
    private lateinit var emptyState: LinearLayout
    private lateinit var etSearch: EditText
    private lateinit var ivClearSearch: ImageView

    private var allWords = listOf<VocabularyItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vocabulary)

        rvVocabulary = findViewById(R.id.rvVocabulary)
        tvWordCount = findViewById(R.id.tvWordCount)
        tvTotalValue = findViewById(R.id.tvTotalValue)
        tvThisWeekValue = findViewById(R.id.tvThisWeekValue)
        tvTodayValue = findViewById(R.id.tvStreakValue)
        emptyState = findViewById(R.id.emptyState)
        etSearch = findViewById(R.id.etSearch)
        ivClearSearch = findViewById(R.id.ivClearSearch)

        adapter = VocabularyAdapter(
            onDetail = { vocab ->
                showVocabularyDetail(vocab)
            },
            onDelete = { vocab ->
                confirmDeleteVocabulary(vocab)
            }
        )

        rvVocabulary.adapter = adapter
        rvVocabulary.layoutManager = LinearLayoutManager(this)

        setupBottomNav()
        setupSearch()
        loadVocabulary()
    }

    override fun onResume() {
        super.onResume()
        loadVocabulary()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterVocabulary(s.toString())
            }
        })

        ivClearSearch.setOnClickListener {
            etSearch.text.clear()
        }
    }

    private fun filterVocabulary(keyword: String) {
        val query = keyword.trim()

        val filteredWords = if (query.isEmpty()) {
            allWords
        } else {
            allWords.filter { vocab ->
                vocab.word.contains(query, ignoreCase = true) ||
                        (vocab.meaning?.contains(query, ignoreCase = true) == true) ||
                        (vocab.partOfSpeech?.contains(query, ignoreCase = true) == true)
            }
        }

        adapter.submitList(filteredWords)
        ivClearSearch.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
        updateEmptyState()
    }

    private fun loadVocabulary() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMyVocabulary()

                if (response.isSuccessful) {
                    allWords = response.body() ?: emptyList()
                    adapter.submitList(allWords)
                    updateStats()
                    updateEmptyState()
                } else {
                    val message = when (response.code()) {
                        401 -> "Bạn cần đăng nhập lại"
                        else -> "Lỗi tải danh sách từ: ${response.code()}"
                    }
                    Toast.makeText(this@VocabularyActivity, message, Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@VocabularyActivity,
                    "Lỗi kết nối: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateStats() {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var todayCount = 0
        var weekCount = 0

        val formats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )

        allWords.forEach { item ->
            item.createdAt?.let { dateStr ->
                var date: Date? = null
                for (pattern in formats) {
                    try {
                        val sdf = SimpleDateFormat(pattern, Locale.US)
                        if (pattern.endsWith("'Z'")) sdf.timeZone = TimeZone.getTimeZone("UTC")
                        date = sdf.parse(dateStr)
                        if (date != null) break
                    } catch (e: Exception) {}
                }

                date?.let {
                    val cal = Calendar.getInstance().apply { time = it }
                    if (cal.after(todayStart)) todayCount++
                    if (cal.after(weekStart)) weekCount++
                }
            }
        }

        tvTotalValue.text = allWords.size.toString()
        tvThisWeekValue.text = weekCount.toString()
        tvTodayValue.text = todayCount.toString()
        tvWordCount.text = "${allWords.size} words collected"
    }

    private fun showVocabularyDetail(vocab: VocabularyItem) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_vocabulary_detail, null)

        view.findViewById<TextView>(R.id.tvDetailWord).text = vocab.word
        view.findViewById<TextView>(R.id.tvDetailMeaning).text = vocab.meaning ?: "Chưa có nghĩa"
        view.findViewById<TextView>(R.id.tvDetailPronunciation).text = vocab.pronunciation ?: ""
        
        val tvPOS = view.findViewById<TextView>(R.id.tvDetailPartOfSpeech)
        if (vocab.partOfSpeech.isNullOrBlank()) {
            tvPOS.visibility = View.GONE
        } else {
            tvPOS.text = vocab.partOfSpeech
            tvPOS.visibility = View.VISIBLE
        }

        view.findViewById<TextView>(R.id.tvDetailDate).text = "Ngày lưu: ${vocab.createdAt ?: "Không rõ"}"

        view.findViewById<View>(R.id.btnDetailCreatePostcard).setOnClickListener {
            val intent = Intent(this, CreatePostcardActivity::class.java).apply {
                putExtra(CreatePostcardActivity.EXTRA_WORD, vocab.word)
                putExtra(CreatePostcardActivity.EXTRA_WORD_MEANING, vocab.meaning)
                putExtra(CreatePostcardActivity.EXTRA_WORD_PRONUNCIATION, vocab.pronunciation)
            }
            startActivity(intent)
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btnDetailDelete).setOnClickListener {
            dialog.dismiss()
            confirmDeleteVocabulary(vocab)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun confirmDeleteVocabulary(vocab: VocabularyItem) {
        if (vocab.id.isBlank()) {
            Toast.makeText(this, "Không tìm thấy ID của từ", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Xóa từ vựng")
            .setMessage("Bạn có chắc muốn xóa từ '${vocab.word}' khỏi danh sách không?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteVocabulary(vocab)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteVocabulary(vocab: VocabularyItem) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteVocabulary(vocab.id)

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@VocabularyActivity,
                        "Đã xóa '${vocab.word}'",
                        Toast.LENGTH_SHORT
                    ).show()

                    allWords = allWords.filter { it.id != vocab.id }
                    filterVocabulary(etSearch.text.toString())
                    updateStats()
                } else {
                    val message = when (response.code()) {
                        401 -> "Bạn cần đăng nhập lại"
                        404 -> "Từ này không còn tồn tại"
                        else -> "Xóa thất bại: ${response.code()}"
                    }
                    Toast.makeText(this@VocabularyActivity, message, Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@VocabularyActivity,
                    "Lỗi xóa từ: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateEmptyState() {
        val isEmpty = adapter.itemCount == 0
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvVocabulary.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun setupBottomNav() {
        findViewById<View>(R.id.navVocab).apply {
            background = ContextCompat.getDrawable(this@VocabularyActivity, R.drawable.bg_nav_item_selected)
            findViewById<ImageView>(R.id.ivVocab).setColorFilter(ContextCompat.getColor(this@VocabularyActivity, R.color.white))
            findViewById<TextView>(R.id.tvVocab).setTextColor(ContextCompat.getColor(this@VocabularyActivity, R.color.white))
        }

        findViewById<View>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        findViewById<View>(R.id.navFriends).setOnClickListener {
            startActivity(Intent(this, FriendActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        findViewById<View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        findViewById<View>(R.id.btnSnapPhoto).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}