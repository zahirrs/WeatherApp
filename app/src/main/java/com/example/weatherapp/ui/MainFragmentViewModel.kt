package com.example.weatherapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.WeatherRepository
import com.example.weatherapp.data.WeatherRepositoryImpl
import com.example.weatherapp.utils.RequestState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainFragmentViewModel(
    private val weatherRepository: WeatherRepository = WeatherRepositoryImpl(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {

    private val _definition: MutableLiveData<RequestState> = MutableLiveData()
    val weatherDefinition: LiveData<RequestState> get() = _definition

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getCurrentWeatherByCity(cityName: String) {
        viewModelScope.launch(ioDispatcher) {
            weatherRepository.getCurrentWeatherByCity(cityName).collect {
                when (it) {
                    is RequestState.LOADING -> {
                        _definition.postValue(it)
                        _isLoading.postValue(it.loading)
                    }
                    is RequestState.SUCCESS<*> -> {
                        _isLoading.postValue(it.isLoading)
                        _definition.postValue(it)
                    }
                    is RequestState.ERROR -> {
                        _definition.postValue(it)
                        _isLoading.postValue(it.isLoading)
                    }
                }
            }
        }
    }

    fun getCurrentWeatherByLocation(lat: Double, lng: Double) {
        viewModelScope.launch(ioDispatcher) {
            weatherRepository.getCurrentWeatherByLocation(lat, lng).collect {
                when (it) {
                    is RequestState.LOADING -> {
                        _definition.postValue(it)
                        _isLoading.postValue(it.loading)
                    }
                    is RequestState.SUCCESS<*> -> {
                        _isLoading.postValue(it.isLoading)
                        _definition.postValue(it)
                    }
                    is RequestState.ERROR -> {
                        _definition.postValue(it)
                        _isLoading.postValue(it.isLoading)
                    }
                }
            }
        }
    }
}