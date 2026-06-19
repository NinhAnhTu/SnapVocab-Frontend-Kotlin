package com.snapvocab.app.data.model


data class User(
    val id: String,
    val email: String,
    val username: String,
    val avatar_url: String?,
    val bio: String?,
    val created_at: String,
    val updated_at: String
)