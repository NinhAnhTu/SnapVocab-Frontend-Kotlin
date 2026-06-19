package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class AddVocabularyRequest(
    val word: String,

    val meaning: String? = null,

    val pronunciation: String? = null,

    @SerializedName("part_of_speech")
    val partOfSpeech: String? = null,

    @SerializedName("source_postcard_id")
    val sourcePostcardId: String? = null
)