package com.snapvocab.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.snapvocab.app.data.model.User

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? = prefs.getString("jwt_token", null)

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString("user", userJson).apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString("user", null) ?: return null
        return gson.fromJson(userJson, User::class.java)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null
}