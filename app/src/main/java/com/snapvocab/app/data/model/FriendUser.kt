package com.snapvocab.app.data.model

import com.google.gson.annotations.SerializedName

data class FriendUser(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("user_id")
    val userId: String? = null,

    @SerializedName("friend_user_id")
    val friendUserId: String? = null,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("avatar_url")
    val avatarUrl: String? = null
) {
    fun realId(): String {
        return friendUserId ?: userId ?: id ?: ""
    }

    fun displayName(): String {
        return username ?: email ?: "Unknown user"
    }
}