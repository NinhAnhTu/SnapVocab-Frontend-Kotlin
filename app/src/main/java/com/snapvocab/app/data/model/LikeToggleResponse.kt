package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class LikeToggleResponse(
    @SerializedName("postcard_id")
    val postcardId: String,

    @SerializedName("liked")
    val liked: Boolean,

    @SerializedName("like_count")
    val likeCount: Int
)