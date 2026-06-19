package com.snapvocab.app.data.api

import android.content.Context
import com.snapvocab.app.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val PREFS_NAME = "snapvocab_network_prefs"
    private const val KEY_BASE_URL = "base_url"

    // Emulator Android dùng 10.0.2.2.
    // Điện thoại thật phải đổi sang IP laptop, ví dụ: http://192.168.1.55:8000/
//    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
    private const val DEFAULT_BASE_URL = "http://172.20.10.7:8000/"

    private lateinit var appContext: Context
    private lateinit var tokenManager: TokenManager

    @Volatile
    private var retrofit: Retrofit? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        tokenManager = TokenManager(appContext)
    }

    fun getBaseUrl(context: Context? = null): String {
        val ctx = when {
            ::appContext.isInitialized -> appContext
            context != null -> context.applicationContext
            else -> return DEFAULT_BASE_URL
        }

        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BASE_URL, DEFAULT_BASE_URL)
            ?: DEFAULT_BASE_URL
    }

    fun setBaseUrl(context: Context, rawUrl: String) {
        val fixedUrl = normalizeBaseUrl(rawUrl)

        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BASE_URL, fixedUrl)
            .apply()

        // Reset Retrofit để request sau dùng URL mới
        retrofit = null
    }

    private fun normalizeBaseUrl(rawUrl: String): String {
        var url = rawUrl.trim()

        if (url.isBlank()) {
            url = DEFAULT_BASE_URL
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }

        if (!url.endsWith("/")) {
            url += "/"
        }

        return url
    }

    fun toAbsoluteUrl(path: String?): String {
        if (path.isNullOrBlank()) return ""

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }

        return getBaseUrl().trimEnd('/') + "/" + path.trimStart('/')
    }

    private fun createClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()

                if (::tokenManager.isInitialized) {
                    val token = tokenManager.getToken()

                    if (!token.isNullOrBlank()) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                }

                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    private fun getRetrofit(): Retrofit {
        val current = retrofit

        if (current != null) {
            return current
        }

        val created = Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(createClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit = created
        return created
    }

    val apiService: ApiService
        get() = getRetrofit().create(ApiService::class.java)
}