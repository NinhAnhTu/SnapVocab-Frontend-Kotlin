package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class AnalyzeResponse(
    val objects: List<DetectedObject> = emptyList()
)

data class DetectedObject(
    val label: String = "",

    val confidence: Float = 0f,

    @SerializedName("is_low_confidence")
    val isLowConfidence: Boolean = false,

    @SerializedName("detection_source")
    val detectionSource: String? = null,

    @SerializedName("preprocessing_source")
    val preprocessingSource: String? = null,

    @SerializedName("from_user_roi")
    val fromUserRoi: Boolean = false,

    @SerializedName("boundingBox")
    val boundingBox: BoundingBox? = null,

    val words: List<WordItem> = emptyList()
)

data class BoundingBox(
    val x1: Float = 0f,
    val y1: Float = 0f,
    val x2: Float = 0f,
    val y2: Float = 0f
)

data class WordItem(
    val word: String = "",

    val meaning: String? = null,

    val pronunciation: String? = null,

    @SerializedName("part_of_speech")
    val partOfSpeech: String? = null,

    val variants: List<String> = emptyList(),

    val related: List<String> = emptyList(),

    @SerializedName("example_sentence")
    val exampleSentence: String? = null,

    @SerializedName("example_meaning")
    val exampleMeaning: String? = null,

    val difficulty: String? = null,

    val topic: String? = null,

    @SerializedName("exercise_question")
    val exerciseQuestion: String? = null,

    @SerializedName("exercise_answer")
    val exerciseAnswer: String? = null
)