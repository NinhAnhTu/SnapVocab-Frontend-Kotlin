package com.snapvocab.app.data.api

import com.snapvocab.app.data.model.AddVocabularyRequest
import com.snapvocab.app.data.model.AnalyzeResponse
import com.snapvocab.app.data.model.AuthResponse
import com.snapvocab.app.data.model.FriendRequest
import com.snapvocab.app.data.model.LoginRequest
import com.snapvocab.app.data.model.RegisterRequest
import com.snapvocab.app.data.model.SendFriendRequest
import com.snapvocab.app.data.model.User
import com.snapvocab.app.data.model.UserSearch
import com.snapvocab.app.data.model.VocabularyItem
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import com.snapvocab.app.data.model.CreatePostcardData
import com.snapvocab.app.data.model.FriendUser
import com.snapvocab.app.data.model.Postcard
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import com.snapvocab.app.data.model.CommentCreate
import com.snapvocab.app.data.model.CommentDto
import com.snapvocab.app.data.model.LikeToggleResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query
import com.snapvocab.app.data.model.VocabularyFromPostcardResponse
interface ApiService {

    // =========================
    // Authentication
    // =========================
    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    // =========================
    // User
    // =========================
    @GET("api/users/me")
    fun getMe(@Header("Authorization") token: String): Call<User>

    // =========================
    // Friends
    // =========================
    @GET("api/friends/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<UserSearch>>

    @GET("api/friends")
    suspend fun getFriends(): Response<List<UserSearch>>

    @GET("api/friends/requests/pending")
    suspend fun getPendingRequests(): Response<List<FriendRequest>>

    @POST("api/friends/requests")
    suspend fun sendFriendRequest(@Body request: SendFriendRequest): Response<Void>

    @PUT("api/friends/requests/{id}/accept")
    suspend fun acceptFriendRequest(@Path("id") requestId: String): Response<Void>

    @PUT("api/friends/requests/{id}/reject")
    suspend fun rejectFriendRequest(@Path("id") requestId: String): Response<Void>

    @DELETE("api/friends/{friendUserId}")
    suspend fun unfriend(@Path("friendUserId") friendUserId: String): Response<Void>

    // =========================
    // Analyze
    // Backend nhận multipart field tên "file"
    // max_objects = 3 để nhận được nhiều vật thể: ruler + pencil + pen
    // =========================
    @Multipart
    @POST("api/analyze")
    suspend fun analyzeImage(
        @Part file: MultipartBody.Part,
        @Query("max_objects") maxObjects: Int = 5,
        @Query("debug_low_confidence") debugLowConfidence: Boolean = true,
        @Query("use_gemini_fallback") useGeminiFallback: Boolean = true,

        // ROI dùng sau này nếu app cho người dùng khoanh vùng
        @Query("roi_x1") roiX1: Float? = null,
        @Query("roi_y1") roiY1: Float? = null,
        @Query("roi_x2") roiX2: Float? = null,
        @Query("roi_y2") roiY2: Float? = null
    ): Response<AnalyzeResponse>

    // =========================
    // Vocabulary
    // =========================
    @GET("api/vocabulary")
    suspend fun getMyVocabulary(): Response<List<VocabularyItem>>

    @POST("api/vocabulary")
    suspend fun addVocabulary(
        @Body request: AddVocabularyRequest
    ): Response<VocabularyItem>

    @GET("api/vocabulary/{id}")
    suspend fun getVocabularyDetail(
        @Path("id") vocabId: String
    ): Response<VocabularyItem>

    @DELETE("api/vocabulary/{id}")
    suspend fun deleteVocabulary(
        @Path("id") vocabId: String
    ): Response<Void>

    @GET("api/friends/")
    suspend fun getFriendsForPostcard(): Response<List<FriendUser>>

    @Multipart
    @POST("api/postcards")
    suspend fun createPostcard(
        @Part("data") data: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("objects") objects: RequestBody? = null  // <-- thêm dòng này
    ): Response<Postcard>

    @GET("api/postcards/feed")
    suspend fun getPostcardFeed(): Response<List<Postcard>>

    @GET("api/postcards/feed")
    suspend fun getReceivedPostcards(): Response<List<Postcard>>

    @GET("api/postcards/sent")
    suspend fun getSentPostcards(): Response<List<Postcard>>

    @POST("api/postcards/{postcard_id}/like")
    suspend fun togglePostcardLike(
        @Path("postcard_id") postcardId: String
    ): Response<LikeToggleResponse>

    @GET("api/postcards/{postcard_id}/comments")
    suspend fun getPostcardComments(
        @Path("postcard_id") postcardId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<CommentDto>>

    @POST("api/postcards/{postcard_id}/comments")
    suspend fun addPostcardComment(
        @Path("postcard_id") postcardId: String,
        @Body body: CommentCreate
    ): Response<CommentDto>

    @DELETE("api/comments/{comment_id}")
    suspend fun deleteComment(
        @Path("comment_id") commentId: String
    ): Response<Map<String, String>>

    @GET("api/postcards/feed/all")
    suspend fun getAllFeedPostcards(): Response<List<Postcard>>

    @POST("api/vocabulary/from-postcard/{postcard_id}")
    suspend fun addVocabularyFromPostcard(
        @Path("postcard_id") postcardId: String
    ): Response<VocabularyFromPostcardResponse>
}