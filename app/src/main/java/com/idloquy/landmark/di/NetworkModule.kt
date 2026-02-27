package com.idloquy.landmark.di

import com.idloquy.landmark.BuildConfig
import com.idloquy.landmark.data.network.LandmarkApiService
import com.idloquy.landmark.data.network.interceptors.BearerTokenInterceptor
import com.idloquy.landmark.data.network.model.ApiResponseAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideLandmarkApiService(client: OkHttpClient, moshi: Moshi): LandmarkApiService {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LandmarkApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOkhttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(BearerTokenInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(ApiResponseAdapterFactory())
            .add(KotlinJsonAdapterFactory())
            .build()
    }
}