package com.snapvocab.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.snapvocab.app.R
import com.snapvocab.app.ui.auth.BeginActivity
import com.snapvocab.app.ui.friends.FriendActivity
import com.snapvocab.app.ui.home.MainActivity
import com.snapvocab.app.ui.vocabulary.VocabularyActivity
import com.snapvocab.app.utils.TokenManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvBio: TextView
    private lateinit var btnLogout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tokenManager = TokenManager(this)

        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvBio = findViewById(R.id.tvBio)
        btnLogout = findViewById(R.id.btnLogout)

        // Hiển thị thông tin user
        val user = tokenManager.getUser()
        if (user != null) {
            tvUserName.text = user.username
            tvUserEmail.text = user.email
            tvBio.text = user.bio ?: "No bio yet"
        } else {
            // Chưa đăng nhập? Chuyển về màn hình đăng nhập
            startActivity(Intent(this, BeginActivity::class.java))
            finish()
            return
        }

        setupBottomNav()
        setupMenuItems()
        setupLogout()
    }

    private fun setupBottomNav() {
        findViewById<View>(R.id.navProfile).apply {
            background = ContextCompat.getDrawable(this@ProfileActivity, R.drawable.bg_nav_item_selected)
            findViewById<ImageView>(R.id.ivProfile).setColorFilter(ContextCompat.getColor(this@ProfileActivity, R.color.white))
            findViewById<TextView>(R.id.tvProfile).setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.white))
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
        findViewById<View>(R.id.navVocab).setOnClickListener {
            startActivity(Intent(this, VocabularyActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun setupMenuItems() {
        // Edit Profile
        findViewById<View>(R.id.menuEditProfile).setOnClickListener {
            Toast.makeText(this, "Edit Profile - will be implemented", Toast.LENGTH_SHORT).show()
        }
        // Notifications
        findViewById<View>(R.id.menuNotifications).setOnClickListener {
            Toast.makeText(this, "Notifications - coming soon", Toast.LENGTH_SHORT).show()
        }
        // Privacy
        findViewById<View>(R.id.menuPrivacy).setOnClickListener {
            Toast.makeText(this, "Privacy - coming soon", Toast.LENGTH_SHORT).show()
        }
        // Help
        findViewById<View>(R.id.menuHelp).setOnClickListener {
            Toast.makeText(this, "Help & Support - coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLogout() {
        btnLogout.setOnClickListener {
            tokenManager.clear()
            startActivity(Intent(this, BeginActivity::class.java))
            finish()
        }
    }
}