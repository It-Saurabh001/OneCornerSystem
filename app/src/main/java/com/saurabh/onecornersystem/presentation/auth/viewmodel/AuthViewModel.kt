package com.saurabh.onecornersystem.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val authRepository: AuthRepository
) : ViewModel(){

    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Idle)
    val loginState: StateFlow<Resource<User>> = _loginState

    private val _registerState = MutableStateFlow<Resource<User>>(Resource.Idle)
    val registerState: StateFlow<Resource<User>> = _registerState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser


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
        role: String
    ) {
        viewModelScope.launch {
            authRepository.registerUser(email, password, name, phone, role)
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
        _currentUser.value = null
        _loginState.value = Resource.Idle
        _registerState.value = Resource.Idle
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

}