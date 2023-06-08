package com.example.weatherapp.data

import com.example.weatherapp.data.model.CurrentWeatherResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") cityName: String,
        @Query("APPID") appId: String
    ): Response<CurrentWeatherResponse>

    @GET("weather")
    suspend fun getCurrentWeatherByLatLng(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("APPID") appId: String
    ): Response<CurrentWeatherResponse>

    companion object {
        private const val OPEN_WEATHER_MAP_BASE_URL = "https://api.openweathermap.org/data/2.5/"

        val weatherService =
            Retrofit.Builder()
                .baseUrl(OPEN_WEATHER_MAP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(
                            HttpLoggingInterceptor().apply {
                                level = HttpLoggingInterceptor.Level.BODY
                            }
                        )
                        .build()
                )
                .build()
                .create(WeatherService::class.java)
    }
}