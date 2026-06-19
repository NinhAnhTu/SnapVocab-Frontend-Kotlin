package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class FriendRequest(
    val id: String,
    @SerializedName("from_user_id") val fromUserId: String,
    @SerializedName("from_username") val fromUsername: String?,
    @SerializedName("from_avatar_url") val fromAvatarUrl: String?,
    @SerializedName("to_user_id") val toUserId: String?,
    @SerializedName("to_username") val toUsername: String?,
    val status: String,
    @SerializedName("created_at") val createdAt: String
) {
    val senderName: String get() = fromUsername ?: "Unknown"
}