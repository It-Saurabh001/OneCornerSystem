package com.saurabh.onecornersystem.presentation.auth.viewmodel

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
        viewModelScope.launch {
            authRepository.loginUser(email, password)
                .collect { result ->
                    _loginState.value = result
                    if (result is Resource.Success) {
                        _currentUser.value = result.data
                    }
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
        viewModelScope.launch {
            authRepository.registerUser(email, password, name, phone, role, shopType)
                .collect { result ->
                    _registerState.value = result
                    if (result is Resource.Success) {
                        _currentUser.value = result.data
                    }
                }
        }
    }

    fun resetStates() {
        _loginState.value = Resource.Idle
        _registerState.value = Resource.Idle
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _loginState.value = Resource.Idle
            _registerState.value = Resource.Idle
        }
    }

    fun updateShopOwnerActiveStatus(isActive: Boolean) {
        val userId = _currentUser.value?.userId ?: return

        viewModelScope.launch {
            authRepository.updateShopOwnerActiveStatus(userId, isActive)
                .collect { result ->
                    if (result is Resource.Success) {
                        // Update the local state
                        val updatedUser = _currentUser.value?.copy(isActive = isActive)
                        _currentUser.value = updatedUser
                    }
                }
        }
    }

    // Load current user data from Firestore
    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _currentUser.value = result.data
                            Log.d("AuthViewModel", "Current user loaded: ${result.data?.email}")
                        }
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