package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class VocabularyItem(
    val id: String = "",

    val word: String = "",

    val meaning: String? = null,

    val pronunciation: String? = null,

    @SerializedName("part_of_speech")
    val partOfSpeech: String? = null,

    @SerializedName("source_postcard_id")
    val sourcePostcardId: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    val variants: List<String> = emptyList(),

    val related: List<String> = emptyList()
)