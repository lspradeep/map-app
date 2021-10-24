package com.android.w2sapp.di

import com.android.w2sapp.BuildConfig
import com.android.w2sapp.data.api.MapService
import com.android.w2sapp.data.api.PostService
import com.android.w2sapp.utils.Constants.BASE_URL_DIRECTIONS
import com.android.w2sapp.utils.Constants.BASE_URL_POSTS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    fun provideMapService(okHttpClient: OkHttpClient): MapService {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(BASE_URL_DIRECTIONS)
            .build()
            .create(MapService::class.java)
    }

    @Provides
    fun providePostService(okHttpClient: OkHttpClient): PostService {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(BASE_URL_POSTS)
            .build()
            .create(PostService::class.java)
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val okHttpClient = OkHttpClient().newBuilder()
        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            okHttpClient.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpClient.build()
    }
}