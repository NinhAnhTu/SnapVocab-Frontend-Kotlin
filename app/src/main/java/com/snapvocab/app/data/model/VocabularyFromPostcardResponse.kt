package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class VocabularyFromPostcardResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("word")
    val word: String,

    @SerializedName("meaning")
    val meaning: String? = null,

    @SerializedName("pronunciation")
    val pronunciation: String? = null,

    @SerializedName("part_of_speech")
    val partOfSpeech: String? = null,

    @SerializedName("source_postcard_id")
    val sourcePostcardId: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("already_exists")
    val alreadyExists: Boolean = false
)