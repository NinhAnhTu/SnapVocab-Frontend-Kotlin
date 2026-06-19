package com.snapvocab.app.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.snapvocab.app.R
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.data.model.Postcard
import com.snapvocab.app.ui.friends.FriendActivity
import com.snapvocab.app.ui.postcard.PostcardCommentsActivity
import com.snapvocab.app.ui.postcard.PostcardFeedActivity
import com.snapvocab.app.ui.profile.ProfileActivity
import com.snapvocab.app.ui.vocabulary.VocabularyActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

class ActivityHome : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector

    private lateinit var ivUserProfile: ImageView
    private lateinit var tvPosterName: TextView
    private lateinit var tvPostTime: TextView
    private lateinit var postImage: ImageView
    private lateinit var wordText: TextView
    private lateinit var ipaText: TextView
    private lateinit var meaningText: TextView
    private lateinit var tvPostNote: TextView

    private lateinit var btnEmoji: ImageButton
    private lateinit var btnComment: ImageButton
    private lateinit var btnSave: ImageButton
    private lateinit var emojiBar: LinearLayout

    private var postcardList: List<Postcard> = emptyList()
    private var currentPostIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ivUserProfile = findViewById(R.id.ivUserProfile)
        tvPosterName = findViewById(R.id.tvPosterName)
        tvPostTime = findViewById(R.id.tvPostTime)
        postImage = findViewById(R.id.postImage)
        wordText = findViewById(R.id.wordText)
        ipaText = findViewById(R.id.ipaText)
        meaningText = findViewById(R.id.meaningText)
        tvPostNote = findViewById(R.id.tvPostNote)

        btnEmoji = findViewById(R.id.btnEmoji)
        btnComment = findViewById(R.id.btnComment)
        btnSave = findViewById(R.id.btnSave)
        emojiBar = findViewById(R.id.emojiBar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSwipeGesture()
        setupPostcardActions()
        setupBottomNav()

        loadAllFeedPostcards()
    }

    override fun onResume() {
        super.onResume()
        if (::btnEmoji.isInitialized) {
            loadAllFeedPostcards()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (::gestureDetector.isInitialized) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun loadAllFeedPostcards() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllFeedPostcards()
                if (response.isSuccessful) {
                    postcardList = response.body().orEmpty()
                    if (postcardList.isEmpty()) {
                        showEmptyState()
                    } else {
                        if (currentPostIndex >= postcardList.size) currentPostIndex = 0
                        updatePostUI()
                    }
                } else {
                    showEmptyState()
                }
            } catch (e: Exception) {
                showEmptyState()
            }
        }
    }

    private fun showEmptyState() {
        tvPosterName.text = "SnapVocab"
        tvPostTime.text = "Chưa có postcard nào"
        ivUserProfile.setImageResource(R.drawable.ic_user)
        postImage.setImageResource(R.drawable.ic_camera_illustration1)
        wordText.text = "No postcard"
        ipaText.text = ""
        meaningText.text = "Bạn chưa có postcard nào."
        tvPostNote.visibility = View.GONE
        btnEmoji.setImageResource(R.drawable.ic_heart_outline)
        btnEmoji.clearColorFilter()
    }

    private fun updatePostUI() {
        if (postcardList.isEmpty()) {
            showEmptyState()
            return
        }

        val post = postcardList[currentPostIndex]
        val fadeIn = AlphaAnimation(0.3f, 1.0f).apply { duration = 400 }

        tvPosterName.text = post.senderUsername ?: "Unknown user"

        val relativeTime = getRelativeTimeSpan(post.createdAt)
        tvPostTime.text = buildString {
            append(relativeTime)
            append(" • ")
            append(post.likeCount)
            append(" thích")
            append(" • ")
            append(post.commentCount)
            append(" bình luận")
        }

        wordText.text = post.word
        ipaText.text = post.wordPronunciation ?: ""
        meaningText.text = post.wordMeaning ?: ""
        
        if (!post.note.isNullOrBlank()) {
            tvPostNote.text = post.note
            tvPostNote.visibility = View.VISIBLE
        } else {
            tvPostNote.visibility = View.GONE
        }

        Glide.with(this)
            .load(RetrofitClient.toAbsoluteUrl(post.senderAvatarUrl))
            .placeholder(R.drawable.ic_user)
            .error(R.drawable.ic_user)
            .into(ivUserProfile)

        Glide.with(this)
            .load(RetrofitClient.toAbsoluteUrl(post.imageUrl))
            .placeholder(R.drawable.ic_camera_illustration1)
            .error(R.drawable.ic_camera_illustration1)
            .into(postImage)

        updateLikeButton(post.likedByCurrentUser)
        findViewById<View>(R.id.locketCard).startAnimation(fadeIn)
        findViewById<View>(R.id.profileSection).startAnimation(fadeIn)
    }

    private fun getRelativeTimeSpan(dateString: String): String {
        if (dateString.isBlank()) return "Vừa xong"

        val formats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )
        
        var date: Date? = null
        val now = System.currentTimeMillis()
        
        // Thử parse theo UTC trước (phổ biến nhất cho API)
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val d = sdf.parse(dateString)
                if (d != null) {
                    // Kiểm tra xem thời gian có hợp lý không (không nằm ở tương lai quá xa)
                    if (now - d.time > -3600000) { // Chấp nhận lệch tối đa 1 tiếng do sai số hệ thống
                        date = d
                        break
                    }
                }
            } catch (e: Exception) {}
        }
        
        // Nếu parse UTC không hợp lý, thử lại với múi giờ hệ thống
        if (date == null) {
            for (pattern in formats) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.US)
                    sdf.timeZone = TimeZone.getDefault()
                    date = sdf.parse(dateString)
                    if (date != null) break
                } catch (e: Exception) {}
            }
        }
        
        if (date == null) return "Vừa xong"

        var diff = now - date.time

        // Xử lý Clock Skew: Nếu thời gian chụp ở tương lai gần (do lệch đồng hồ), coi như vừa xong
        if (diff < 0) {
            if (abs(diff) < 120000) { // Dưới 2 phút
                diff = 0
            } else {
                // Nếu ở tương lai xa (ví dụ bạn test bằng tay 2026), hiển thị theo hướng "nữa" hoặc cap lại
                return "Vừa xong" 
            }
        }

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 7 -> "$days ngày trước"
            weeks < 4 -> "$weeks tuần trước"
            months < 12 -> "$months tháng trước"
            else -> "$years năm trước"
        }
    }

    private fun updateLikeButton(isLiked: Boolean) {
        if (isLiked) {
            btnEmoji.setImageResource(R.drawable.ic_heart_filled)
            btnEmoji.setColorFilter(Color.RED)
        } else {
            btnEmoji.setImageResource(R.drawable.ic_heart_outline)
            btnEmoji.clearColorFilter()
        }
    }

    private fun setupPostcardActions() {
        btnEmoji.setOnClickListener { toggleLikeCurrentPost() }
        btnComment.setOnClickListener { openCurrentPostComments() }
        btnSave.setOnClickListener { addCurrentPostWordToVocabulary() }
        emojiBar.visibility = View.GONE
        findViewById<View>(R.id.locketCard).setOnClickListener { showCurrentPostActionDialog() }
    }

    private fun showCurrentPostActionDialog() {
        if (postcardList.isEmpty()) return
        val currentPost = postcardList[currentPostIndex]
        val actions = arrayOf("Bình luận", "Thêm vào từ vựng", "Xem danh sách từ", "Mở feed")
        AlertDialog.Builder(this)
            .setTitle(currentPost.word)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> openCurrentPostComments()
                    1 -> addCurrentPostWordToVocabulary()
                    2 -> startActivity(Intent(this, VocabularyActivity::class.java))
                    3 -> startActivity(Intent(this, PostcardFeedActivity::class.java))
                }
            }
            .show()
    }

    private fun addCurrentPostWordToVocabulary() {
        if (postcardList.isEmpty()) return
        val currentPost = postcardList[currentPostIndex]
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.addVocabularyFromPostcard(currentPost.id)
                if (response.isSuccessful) {
                    Toast.makeText(this@ActivityHome, "Đã lưu từ", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {}
        }
    }

    private fun toggleLikeCurrentPost() {
        if (postcardList.isEmpty()) return
        val currentPost = postcardList[currentPostIndex]
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.togglePostcardLike(currentPost.id)
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null) {
                        postcardList = postcardList.map { if (it.id == result.postcardId) it.copy(likedByCurrentUser = result.liked, likeCount = result.likeCount) else it }
                        updatePostUI()
                    }
                }
            } catch (e: Exception) {}
        }
    }

    private fun openCurrentPostComments() {
        if (postcardList.isEmpty()) return
        val currentPost = postcardList[currentPostIndex]
        val intent = Intent(this, PostcardCommentsActivity::class.java).apply {
            putExtra(PostcardCommentsActivity.EXTRA_POSTCARD_ID, currentPost.id)
            putExtra(PostcardCommentsActivity.EXTRA_POSTCARD_WORD, currentPost.word)
        }
        startActivity(intent)
    }

    private fun setupSwipeGesture() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                val e1S = e1 ?: return false
                val dY = e2.y - e1S.y
                val dX = e2.x - e1S.x
                if (abs(dY) > abs(dX)) {
                    if (dY < -100 && abs(vY) > 100) { currentPostIndex = (currentPostIndex + 1) % postcardList.size; updatePostUI(); return true }
                    if (dY > 100 && abs(vY) > 100) { if (currentPostIndex == 0) finish() else { currentPostIndex--; updatePostUI() }; return true }
                }
                return false
            }
        })
    }

    private fun setupBottomNav() {
        findViewById<View>(R.id.navFriends).setOnClickListener { startActivity(Intent(this, FriendActivity::class.java)); finish() }
        findViewById<View>(R.id.navHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<View>(R.id.navVocab).setOnClickListener { startActivity(Intent(this, VocabularyActivity::class.java)); finish() }
        findViewById<View>(R.id.navProfile).setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)); finish() }
        findViewById<View>(R.id.btnCamera).setOnClickListener { finish(); overridePendingTransition(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom) }
    }
}
