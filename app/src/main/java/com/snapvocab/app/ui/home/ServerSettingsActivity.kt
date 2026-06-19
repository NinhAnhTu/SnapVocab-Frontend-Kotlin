package com.snapvocab.app.ui.home

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.snapvocab.app.R
import com.snapvocab.app.data.api.RetrofitClient

class ServerSettingsActivity : AppCompatActivity() {

    private lateinit var edtBaseUrl: EditText
    private lateinit var tvCurrentBaseUrl: TextView
    private lateinit var btnSave: Button
    private lateinit var btnUseEmulator: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_settings)

        edtBaseUrl = findViewById(R.id.edtBaseUrl)
        tvCurrentBaseUrl = findViewById(R.id.tvCurrentBaseUrl)
        btnSave = findViewById(R.id.btnSaveServer)
        btnUseEmulator = findViewById(R.id.btnUseEmulator)
        btnBack = findViewById(R.id.btnBack)

        val currentUrl = RetrofitClient.getBaseUrl(this)
        edtBaseUrl.setText(currentUrl)
        tvCurrentBaseUrl.text = "Server hiện tại: $currentUrl"

        btnSave.setOnClickListener {
            val url = edtBaseUrl.text.toString().trim()

            if (url.isBlank()) {
                Toast.makeText(this, "Bạn chưa nhập server URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.setBaseUrl(this, url)

            val newUrl = RetrofitClient.getBaseUrl(this)
            tvCurrentBaseUrl.text = "Server hiện tại: $newUrl"

            Toast.makeText(this, "Đã lưu server URL", Toast.LENGTH_SHORT).show()
        }

        btnUseEmulator.setOnClickListener {
            RetrofitClient.setBaseUrl(this, "http://10.0.2.2:8000/")

            val newUrl = RetrofitClient.getBaseUrl(this)
            edtBaseUrl.setText(newUrl)
            tvCurrentBaseUrl.text = "Server hiện tại: $newUrl"

            Toast.makeText(this, "Đã chọn URL emulator", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}