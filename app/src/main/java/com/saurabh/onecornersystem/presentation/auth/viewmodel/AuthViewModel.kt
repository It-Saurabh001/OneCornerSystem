package com.saurabh.onecornersystem.presentation.auth.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.local.SessionManager
import com.saurabh.onecornersystem.data.model.User
import com.saurabh.onecornersystem.data.repository.AuthRepository
import com.saurabh.onecornersystem.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel(){

    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Idle)
    val loginState: StateFlow<Resource<User>> = _loginState

    private val _registerState = MutableStateFlow<Resource<User>>(Resource.Idle)
    val registerState: StateFlow<Resource<User>> = _registerState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // Expose session manager flows directly
    val isLoggedIn = sessionManager.isLoggedIn
    val userRole = sessionManager.userRole

    init {
        // Load current user data on ViewModel initialization
        loadCurrentUser()
    }


    fun login(email: String, password: String) {
        Log.d(TAG, "🔐 Login attempt - email: $email, password: [HIDDEN]")
        viewModelScope.launch {
            authRepository.loginUser(email, password)
                .collect { result ->
                    Log.d(TAG, "📥 Login result received: ${result::class.simpleName}")
                    when (result) {
                        is Resource.Loading -> {
                            Log.d(TAG, "⏳ Login in progress...")
                        }
                        is Resource.Success -> {
                            Log.d(TAG, "✅ Login successful!")
                            Log.d(TAG, "   User ID: ${result.data?.userId}")
                            Log.d(TAG, "   User Email: ${result.data?.email}")
                            Log.d(TAG, "   User Role: ${result.data?.role}")
                            Log.d(TAG, "   User Active: ${result.data?.active}")

                            _currentUser.value = result.data

                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Login failed: ${result.message}")
                            Log.e(TAG, "   Error details: ${result.message}")
                        }
                        else -> {}
                    }

                    _loginState.value = result
                }
        }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        role: String,
        shopType: com.saurabh.onecornersystem.data.model.ShopType? = null
    ) {
        Log.d(TAG, "📝 Register attempt - email: $email, name: $name, phone: $phone, role: $role, shopType: $shopType")
        viewModelScope.launch {
            authRepository.registerUser(email, password, name, phone, role, shopType)
                .collect { result ->
                    Log.d(TAG, "📥 Register result received: ${result::class.simpleName}")
                    when (result) {
                        is Resource.Loading -> {
                            Log.d(TAG, "⏳ Registration in progress...")
                        }
                        is Resource.Success -> {
                            Log.d(TAG, "✅ Registration successful!")
                            Log.d(TAG, "   User ID: ${result.data?.userId}")
                            Log.d(TAG, "   User Email: ${result.data?.email}")
                            Log.d(TAG, "   User Role: ${result.data?.role}")
                            Log.d(TAG, "   User Active: ${result.data?.active}")

                            _currentUser.value = result.data

                        }
                        is Resource.Error -> {
                            Log.e(TAG, "❌ Registration failed: ${result.message}")
                            Log.e(TAG, "   Error details: ${result.message}")
                        }
                        else -> {}
                    }

                    _registerState.value = result
                }
        }
    }

    fun resetStates() {
        _loginState.value = Resource.Idle
        _registerState.value = Resource.Idle
    }

    fun logout() {
        // Reset states IMMEDIATELY before any async operations
        // This prevents stale state from triggering navigation in LoginScreen
        _currentUser.value = null
        _loginState.value = Resource.Idle
        _registerState.value = Resource.Idle

        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun updateShopOwnerActiveStatus(isActive: Boolean) {
        val userId = _currentUser.value?.userId ?: return

        viewModelScope.launch {
            authRepository.updateShopOwnerActiveStatus(userId, isActive)
                .collect { result ->
                    if (result is Resource.Success) {
                        // Update the local state
                        val updatedUser = _currentUser.value?.copy(active = isActive)
                        _currentUser.value = updatedUser
                    }
                }
        }
    }

    // Load current user data from Firestore
    private fun loadCurrentUser() {
        Log.d("TAG", " Loading current user from repository")
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .collect { result ->
                    Log.d("TAG", "📥 Load current user result: ${result::class.simpleName}")
                    when (result) {
                        is Resource.Success -> {
                            _currentUser.value = result.data
                            if (result.data != null) {
                                Log.d("TAG", "✅ Current user loaded successfully:")
                                Log.d("TAG", "   User ID: ${result.data.userId}")
                                Log.d("TAG", "   Email: ${result.data.email}")
                                Log.d(TAG, "   Role: ${result.data.role}")
                                Log.d(TAG, "   Active: ${result.data.active}")
                                Log.d(TAG, "   Name: ${result.data.name}")
                            } else {
                                Log.d(TAG, "ℹ️ No user is currently logged in")
                            }                        }
                        is Resource.Error -> {
                            Log.d("AuthViewModel", "Error loading current user: ${result.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("AuthViewModel", "Loading current user...")
                        }
                        else -> {}
                    }
                }
        }
    }

}