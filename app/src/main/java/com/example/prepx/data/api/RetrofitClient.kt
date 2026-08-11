package com.example.prepx.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object supplying Retrofit service instances for Codeforces, CodeChef, and Kontests public APIs.
 */
object RetrofitClient {

    private const val CODEFORCES_BASE_URL = "https://codeforces.com/api/"
    private const val CODECHEF_BASE_URL = "https://www.codechef.com/"
    private const val KONTESTS_BASE_URL = "https://kontests.net/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: CodeforcesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(CODEFORCES_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CodeforcesApiService::class.java)
    }

    val codeChefApiService: CodeChefApiService by lazy {
        Retrofit.Builder()
            .baseUrl(CODECHEF_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CodeChefApiService::class.java)
    }

    val kontestsApiService: KontestsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(KONTESTS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KontestsApiService::class.java)
    }
}
