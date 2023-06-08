package com.example.weatherapp.utils

sealed class RequestState {
    data class LOADING(val loading: Boolean = true) : RequestState()
    class SUCCESS<T>(val data: T, val isLoading: Boolean = false) : RequestState()
    class ERROR(val exception: Throwable, val isLoading: Boolean = false) : RequestState()
}
