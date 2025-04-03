package com.example.hellofigma.di

import android.content.Context
import androidx.datastore.preferences.createDataStore
import com.example.hellofigma.data.repository.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        val dataStore = context.createDataStore(name = "data_prefs")
        return DataStoreManager(dataStore)
    }
}