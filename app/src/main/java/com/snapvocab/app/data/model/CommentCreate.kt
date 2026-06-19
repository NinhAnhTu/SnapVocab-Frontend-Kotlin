package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class CommentCreate(
    @SerializedName("content")
    val content: String
)