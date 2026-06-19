package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class Postcard(
    @SerializedName("id")
    val id: String,

    @SerializedName("sender_id")
    val senderId: String,

    @SerializedName("sender_username")
    val senderUsername: String? = null,

    @SerializedName("sender_avatar_url")
    val senderAvatarUrl: String? = null,

    @SerializedName("image_url")
    val imageUrl: String,

    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @SerializedName("word")
    val word: String,

    @SerializedName("word_meaning")
    val wordMeaning: String? = null,

    @SerializedName("word_pronunciation")
    val wordPronunciation: String? = null,

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("filter_metadata")
    val filterMetadata: String? = null,

    @SerializedName("visibility")
    val visibility: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("like_count")
    val likeCount: Int = 0,

    @SerializedName("comment_count")
    val commentCount: Int = 0,

    @SerializedName("liked_by_current_user")
    val likedByCurrentUser: Boolean = false,

    @SerializedName("receiver_ids")
    val receiverIds: List<String> = emptyList()
)