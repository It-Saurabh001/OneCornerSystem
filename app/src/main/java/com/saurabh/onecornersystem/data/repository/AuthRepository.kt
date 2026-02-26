package com.saurabh.onecornersystem.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.saurabh.onecornersystem.data.model.User
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {



    // User Registration
    fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        role: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading)

        try {
            // 1. Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Registration failed")

            // 2. Create User object
            val user = User(
                userId = firebaseUser.uid,
                name = name,
                email = email,
                phone = phone,
                role = role,
                createdAt = com.google.firebase.Timestamp.now()
            )

            // 3. Save to Firestore
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            emit(Resource.Success(user))
            Log.d("AuthRepository", "User registered successfully: ${firebaseUser.uid}")

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Registration failed"))
            Log.e("AuthRepository", "Registration error", e)
        }
    }

    // User Login
    fun loginUser(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)

        try {
            // 1. Sign in with Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")

            // 2. Get user data from Firestore
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
            if (user != null) {
                emit(Resource.Success(user))
                Log.d("AuthRepository", "User logged in successfully: ${firebaseUser.uid}")
            } else {
                emit(Resource.Error("User data not found"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Login failed"))
            Log.e("AuthRepository", "Login error", e)
        }
    }

    // Get Current User
    fun getCurrentUser(): Flow<Resource<User>> = flow {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            try {
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val user = userDoc.toObject(User::class.java)
                if (user != null) {
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error("User not found"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Error fetching user"))
            }
        } else {
            emit(Resource.Error("No user logged in"))
        }
    }

    // Update Shop Owner Active Status
    fun updateShopOwnerActiveStatus(userId: String, isActive: Boolean): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)

        try {
            firestore.collection("users")
                .document(userId)
                .update("isActive", isActive)
                .await()

            emit(Resource.Success(true))
            Log.d("AuthRepository", "Shop owner active status updated: $isActive")

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update status"))
            Log.e("AuthRepository", "Update status error", e)
        }
    }

}