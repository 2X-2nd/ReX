package com.example.hellofigma.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


// 单例注解
@Singleton
class DataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_GOOGLE_ID = stringPreferencesKey("user_google_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    suspend fun saveLoginState(google_id: String, name: String, email: String) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_GOOGLE_ID] = google_id
            preferences[USER_NAME] = name
            preferences[USER_EMAIL] = email
        }
    }

    suspend fun clearLoginState() {
        dataStore.edit { preferences ->
            preferences.remove(IS_LOGGED_IN)
            preferences.remove(USER_GOOGLE_ID)
            preferences.remove(USER_NAME)
            preferences.remove(USER_EMAIL)
        }
    }

    val loginState: Flow<LoginState> = dataStore.data
        .map { preferences ->
            LoginState(
                isLoggedIn = preferences[IS_LOGGED_IN] ?: false,
                googleId = preferences[USER_GOOGLE_ID],
                userName = preferences[USER_NAME],
                userEmail = preferences[USER_EMAIL]
            )
        }
        .distinctUntilChanged() // 避免重复更新
}

data class LoginState(
    val isLoggedIn: Boolean,
    val googleId: String?,
    val userName: String?,
    val userEmail: String?
)
