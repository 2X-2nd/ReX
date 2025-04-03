package com.example.hellofigma.di

import com.example.hellofigma.data.repository.PriceSuggestionsApi
import com.example.hellofigma.data.repository.ProductApi
import com.example.hellofigma.data.repository.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named


/*
 Define a Dagger module to provide network related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val USER_BASE_URL = "http://3.138.121.192:8080/"
    private const val PRODUCT_BASE_URL = "https://nsefhqsvqf.execute-api.us-east-2.amazonaws.com/"
    private const val PRICE_SUGGESTIONS_BASE_URL = "http://3.138.121.192:5001/"

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    /*
     用户服务的 Retrofit
     */
    @Provides
    @Named("UserRetrofit")
    fun provideUserRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(USER_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /*
     商品服务的 Retrofit
     */
    @Provides
    @Named("ProductRetrofit")
    fun provideProductRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(PRODUCT_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /*
     商品服务的 Retrofit
     */
    @Provides
    @Named("PriceSuggestionsRetrofit")
    fun providePriceSuggestionsRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(PRICE_SUGGESTIONS_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /*
     用户相关接口（绑定到 UserRetrofit）
     */
    @Provides
    fun provideUserApi(@Named("UserRetrofit") retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    /*
     商品相关接口（绑定到 ProductRetrofit）
     */
    @Provides
    fun provideProductApi(@Named("ProductRetrofit") retrofit: Retrofit): ProductApi {
        return retrofit.create(ProductApi::class.java)
    }

    @Provides
    fun providePriceSuggestionsApi(@Named("PriceSuggestionsRetrofit") retrofit: Retrofit): PriceSuggestionsApi {
        return retrofit.create(PriceSuggestionsApi::class.java)
    }
}
