package com.acitelight.aether.service

import com.acitelight.aether.model.ChallengeResponse
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.VideoResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiInterface {
    @GET("api/video")
    suspend fun getVideoClasses(
        @Query("token") token: String
    ): List<String>
    @GET("api/video/{klass}")
    suspend fun queryVideoClasses(
        @Path("klass") klass: String,
        @Query("token") token: String
    ): List<String>
    @GET("api/video/{klass}/{id}")
    suspend fun queryVideo(
        @Path("klass") klass: String,
        @Path("id") id: String,
        @Query("token") token: String
    ): VideoResponse

    @GET("api/video/{klass}/{id}/nv")
    @Streaming
    suspend fun getNailVideo(
        @Path("klass") klass: String,
        @Path("id") id: String,
        @Query("token") token: String
    ): ResponseBody

    @GET("api/image/collections")
    suspend fun getComicCollections(): List<String>
    @GET("api/image/meta")
    suspend fun queryComicInfo(@Query("collection") collection: String): Comic


    @GET("api/user/{user}")
    suspend fun getChallenge(
        @Path("user") user: String
    ): ResponseBody

    @POST("api/user/{user}")
    suspend fun verifyChallenge(
        @Path("user") user: String,
        @Body challengeResponse: ChallengeResponse
    ): ResponseBody
}
