package com.example.weatherapp.data

import com.example.weatherapp.utils.FailureResponse
import com.example.weatherapp.utils.NullResponseException
import com.example.weatherapp.utils.RequestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface WeatherRepository {
    fun getCurrentWeatherByCity(acronym: String): Flow<RequestState>
    fun getCurrentWeatherByLocation(lat: Double, lng: Double): Flow<RequestState>
}

class WeatherRepositoryImpl(
    private val acronymService: WeatherService = WeatherService.weatherService
) : WeatherRepository {

    companion object {
        private const val OPEN_WEATHER_MAP_APP_ID = "6592d24a33ae13c2ac1401db99732c61"
    }

    override fun getCurrentWeatherByCity(cityName: String): Flow<RequestState> = flow {
        emit(RequestState.LOADING())

        try {
            val response = acronymService.getCurrentWeatherByCity(cityName, OPEN_WEATHER_MAP_APP_ID)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(RequestState.SUCCESS(it))
                } ?: throw NullResponseException("Weather response is null")
            } else {
                throw FailureResponse("Weather definition response is a failure")
            }
        } catch (e: Exception) {
            emit(RequestState.ERROR(e))
        }
    }

    override fun getCurrentWeatherByLocation(lat: Double, lng: Double): Flow<RequestState> = flow {
        emit(RequestState.LOADING())

        try {
            val response = acronymService.getCurrentWeatherByLatLng(lat, lng, OPEN_WEATHER_MAP_APP_ID)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(RequestState.SUCCESS(it))
                } ?: throw NullResponseException("Weather response is null")
            } else {
                throw FailureResponse("Weather definition response is a failure")
            }
        } catch (e: Exception) {
            emit(RequestState.ERROR(e))
        }
    }
}