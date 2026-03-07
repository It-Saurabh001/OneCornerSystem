package com.saurabh.onecornersystem.utils


sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    object Idle : Resource<Nothing>()

    // Helper functions
    fun isLoading(): Boolean = this is Loading
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isIdle(): Boolean = this is Idle

    fun getSuccessData(): T? = (this as? Success)?.data
    fun getErrorMessage(): String? = (this as? Error)?.message
}