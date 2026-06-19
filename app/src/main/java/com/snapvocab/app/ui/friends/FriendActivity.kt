package com.snapvocab.app.ui.friends

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.snapvocab.app.R
import com.snapvocab.app.ui.home.MainActivity
import com.snapvocab.app.ui.profile.ProfileActivity
import com.snapvocab.app.ui.vocabulary.VocabularyActivity

class FriendActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)

        setupTabLayout()
        setupBottomNav()
    }

    private fun setupTabLayout() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = FriendsPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Bạn bè"
                1 -> "Lời mời"
                else -> "Tìm kiếm"
            }
        }.attach()
    }

    private fun setupBottomNav() {
        findViewById<View>(R.id.navFriends).apply {
            background = ContextCompat.getDrawable(this@FriendActivity, R.drawable.bg_nav_item_selected)
            findViewById<ImageView>(R.id.ivFriends).setColorFilter(ContextCompat.getColor(this@FriendActivity, R.color.white))
            findViewById<TextView>(R.id.tvFriends).setTextColor(ContextCompat.getColor(this@FriendActivity, R.color.white))
        }

        findViewById<View>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        findViewById<View>(R.id.navVocab).setOnClickListener {
            startActivity(Intent(this, VocabularyActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        findViewById<View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }
}