package com.acitelight.aether.service

import com.acitelight.aether.model.BookMark
import com.acitelight.aether.model.ChallengeResponse
import com.acitelight.aether.model.ComicResponse
import com.acitelight.aether.model.VideoResponse

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {
    @GET("api/video")
    suspend fun getVideoClasses(
    ): List<String>
    @GET("api/video/{klass}")
    suspend fun queryVideoClasses(
        @Path("klass") klass: String
    ): List<String>
    @GET("api/video/{klass}/{id}")
    suspend fun queryVideo(
        @Path("klass") klass: String,
        @Path("id") id: String
    ): VideoResponse

    @POST("api/video/{klass}/bulkquery")
    suspend fun queryVideoBulk(
        @Path("klass") klass: String,
        @Body() id: List<String>
    ): List<VideoResponse>

    @GET("api/image")
    suspend fun getComics(): List<String>
    @GET("api/image/{id}")
    suspend fun queryComicInfo(@Path("id") id: String): ComicResponse

    @POST("api/image/bulkquery")
    suspend fun queryComicInfoBulk(@Body() id: List<String>): List<ComicResponse>

    @POST("api/image/{id}/bookmark")
    suspend fun postBookmark(@Path("id") id: String, @Body bookmark: BookMark)

    @GET("api/user/{user}")
    suspend fun getChallenge(
        @Path("user") user: String
    ): ResponseBody

    @POST("api/user/{user}")
    suspend fun verifyChallenge(
        @Path("user") user: String,
        @Body challengeResponse: ChallengeResponse
    ): ResponseBody

    @GET("api/abyss")
    suspend fun hello(): ResponseBody
}
