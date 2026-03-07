package com.saurabh.onecornersystem.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

class SessionManager @Inject constructor(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        private const val TAG = "SessionManager"
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val dataStoreLoggedIn = preferences[IS_LOGGED_IN] ?: false
        val firebaseUser = firebaseAuth.currentUser
        val firebaseUserExists = firebaseUser != null
        val result = dataStoreLoggedIn && firebaseUserExists

        Log.d(TAG, "isLoggedIn check - DataStore: $dataStoreLoggedIn, FirebaseUser: ${firebaseUser?.uid}, Result: $result")
        result
    }

    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        val firebaseUser = firebaseAuth.currentUser
        val role = if (firebaseUser != null) {
            preferences[USER_ROLE]
        } else {
            null
        }
        Log.d(TAG, "userRole check - FirebaseUser: ${firebaseUser?.uid}, Role: $role")
        role
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        val id = preferences[USER_ID]
        Log.d(TAG, "userId check - UserId: $id")
        id
    }

    suspend fun saveLoginSession(userId: String, userEmail: String, userRole: String) {
        Log.d(TAG, "saveLoginSession - userId: $userId, email: $userEmail, role: $userRole")
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = userId
            preferences[USER_EMAIL] = userEmail
            preferences[USER_ROLE] = userRole
        }
    }

    suspend fun clearSession() {
        Log.d(TAG, "clearSession called")
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences.remove(USER_ID)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_ROLE)
        }
    }
}
