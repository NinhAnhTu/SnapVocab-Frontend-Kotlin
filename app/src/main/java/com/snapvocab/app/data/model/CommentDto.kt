package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class CommentDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("postcard_id")
    val postcardId: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("sender_username")
    val senderUsername: String,

    @SerializedName("sender_avatar_url")
    val senderAvatarUrl: String? = null,

    @SerializedName("content")
    val content: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)