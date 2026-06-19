package com.snapvocab.app.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.snapvocab.app.R
import com.snapvocab.app.data.api.RetrofitClient
import com.snapvocab.app.ui.friends.FriendActivity
import com.snapvocab.app.ui.profile.ProfileActivity
import com.snapvocab.app.ui.vocabulary.VocabularyActivity
import com.snapvocab.app.utils.TokenManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var gestureDetector: GestureDetector
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
    private lateinit var tokenManager: TokenManager

    // Launcher chọn ảnh từ gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val intent = Intent(this, ActivityAnalyze::class.java).apply {
                putExtra("image_uri", it.toString())
            }
            startActivity(intent)
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Log.e(TAG, "Quyền Camera bị từ chối")
                Toast.makeText(baseContext, "Vui lòng cấp quyền Camera để sử dụng ứng dụng", Toast.LENGTH_LONG).show()
            } else {
                Log.d(TAG, "Quyền Camera đã được cấp, đang khởi động Camera...")
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tokenManager = TokenManager(this)
        viewFinder = findViewById(R.id.viewFinder)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Kiểm tra và yêu cầu quyền
        if (allPermissionsGranted()) {
            Log.d(TAG, "Quyền đã có sẵn, khởi động Camera")
            startCamera()
        } else {
            Log.d(TAG, "Chưa có quyền, đang yêu cầu...")
            requestPermissions()
        }

        findViewById<View>(R.id.btnCamera).setOnClickListener {
            takePhoto()
        }

        // Nút chọn ảnh từ thư viện
        findViewById<View>(R.id.btnGallery).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        findViewById<View>(R.id.btnGallery).setOnLongClickListener {
            startActivity(Intent(this, ServerSettingsActivity::class.java))
            true
        }

        setupTopBar()
        setupBottomNav()
        setupSwipeGesture()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupTopBar() {
        val ivProfileTop = findViewById<ImageView>(R.id.ivProfileTop)
        val btnProfileTop = findViewById<View>(R.id.btnProfileTop)
        
        val user = tokenManager.getUser()
        if (user != null) {
            Glide.with(this)
                .load(RetrofitClient.toAbsoluteUrl(user.avatar_url))
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(ivProfileTop)
        }
        
        btnProfileTop.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Chụp ảnh thất bại: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d(TAG, "Chụp ảnh thành công: $savedUri")

                    val intent = Intent(this@MainActivity, ActivityAnalyze::class.java).apply {
                        putExtra("image_uri", savedUri.toString())
                    }
                    startActivity(intent)
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = viewFinder.surfaceProvider
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                Log.d(TAG, "Camera đã được gắn vào Lifecycle thành công")
            } catch (exc: Exception) {
                Log.e(TAG, "Lỗi khi gắn Camera vào Lifecycle", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val outputDirectory: File by lazy {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun setupSwipeGesture() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                val diffY = e2.y - (e1?.y ?: 0f)
                val diffX = e2.x - (e1?.x ?: 0f)
                if (abs(diffY) > abs(diffX) && diffY < -100 && abs(vy) > 100) {
                    val intent = Intent(this@MainActivity, ActivityHome::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top)
                    return true
                }
                return false
            }
        })

        findViewById<View>(R.id.mainScrollView).setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun setupBottomNav() {
        findViewById<View>(R.id.navHome).apply {
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_nav_item_selected)
            findViewById<ImageView>(R.id.ivHome).setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.white))
            findViewById<TextView>(R.id.tvHome).setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
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
        findViewById<View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    companion object {
        private const val TAG = "SnapVocabCamera"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}