package com.khammin.game.data.remote.api

import com.khammin.game.data.remote.model.CreateProfileRequest
import com.khammin.game.data.remote.model.ProfileResponse
import com.khammin.game.data.remote.model.SingleProfileResponse
import com.khammin.game.data.remote.model.StrapiUploadResponse
import com.khammin.game.data.remote.model.UpdateProfileRequest
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
        @Query("filters[firebaseUid][\$eq]") firebaseUid: String,
        @Query("sort[0]") sort: String = "createdAt:asc",
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
        @Query("sort[0]") sort: String = "arCurrentPoints:desc",
        @Query("pagination[limit]") limit: Int = 10,
        @Query("fields[0]") f0: String = "name",
        @Query("fields[1]") f1: String = "avatarUrl",
        @Query("fields[2]") f2: String = "firebaseUid",
        @Query("fields[6]") f6: String = "arCurrentPoints",
        @Query("fields[7]") f7: String = "arWordsSolved",
        @Query("fields[8]") f8: String = "arLastPlayedAt",
    ): ProfileResponse
}
