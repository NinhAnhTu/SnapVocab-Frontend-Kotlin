package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class UserSearch(
    @SerializedName("user_id") val id: String,
    val username: String,
    @SerializedName("avatar_url") val avatar_url: String? = null
)