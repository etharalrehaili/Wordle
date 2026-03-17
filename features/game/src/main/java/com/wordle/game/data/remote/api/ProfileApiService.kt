package com.wordle.game.data.remote.api

import com.wordle.game.data.remote.model.CreateProfileRequest
import com.wordle.game.data.remote.model.ProfileResponse
import com.wordle.game.data.remote.model.SingleProfileResponse
import com.wordle.game.data.remote.model.StrapiUploadResponse
import com.wordle.game.data.remote.model.UpdateProfileRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileApiService {
    @GET("profiles")
    suspend fun getProfile(
        @Query("filters[firebaseUid][\$eq]") firebaseUid: String
    ): ProfileResponse

    @POST("profiles")
    suspend fun createProfile(
        @Body body: CreateProfileRequest
    ): SingleProfileResponse

    @PUT("profiles/{documentId}")
    suspend fun updateProfile(
        @Path("documentId") documentId: String,
        @Body body: UpdateProfileRequest
    ): SingleProfileResponse

    @Multipart
    @POST("upload")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): List<StrapiUploadResponse>

    @GET("profiles")
    suspend fun getLeaderboard(
        @Query("sort[0]") sort: String = "currentPoints:desc",
        @Query("sort[1]") sort2: String = "lastPlayedAt:asc",
        @Query("pagination[limit]") limit: Int = 10,
        @Query("fields[0]") f0: String = "name",
        @Query("fields[1]") f1: String = "avatarUrl",
        @Query("fields[2]") f2: String = "currentPoints",
        @Query("fields[3]") f3: String = "firebaseUid",
        @Query("fields[4]") f4: String = "wordsSolved",
        @Query("fields[5]") f5: String = "lastPlayedAt",
    ): ProfileResponse
}
