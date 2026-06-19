package com.snapvocab.app.data.model


data class AuthResponse(
    val token: String,
    val user: User
)