package com.snapvocab.app

import android.app.Application
import com.snapvocab.app.data.api.RetrofitClient

class SnapVocabApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}