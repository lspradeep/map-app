package com.android.w2sapp.data.api

import com.android.w2sapp.data.models.post.Post
import retrofit2.http.GET
import retrofit2.http.Query

interface PostService {
    @GET("posts")
    suspend fun getPosts(): List<Post>?
}