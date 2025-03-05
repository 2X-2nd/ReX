package com.example.hellofigma.message.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    // 替换成你的实际 API 地址
    private const val BASE_URL = "https://zv6aqpazhh.execute-api.us-east-2.amazonaws.com/"

    val instance: Retrofit by lazy {
        // 添加日志拦截器（调试用）
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 配置 OkHttp
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // 构建 Retrofit 实例
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
