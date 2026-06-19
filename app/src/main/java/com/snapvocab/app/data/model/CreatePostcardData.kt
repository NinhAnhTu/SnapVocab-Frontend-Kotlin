package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class CreatePostcardData(
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
    val visibility: String = "friends",

    @SerializedName("receiver_ids")
    val receiverIds: List<String>,

    @SerializedName("objects")
    val objects: List<DetectedObject>? = null
)