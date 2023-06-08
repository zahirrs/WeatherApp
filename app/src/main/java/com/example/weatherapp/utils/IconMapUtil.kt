package com.example.weatherapp.utils

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.weatherapp.R

@DrawableRes
fun Context.getIconDrawableFromCurrentWeather(
    weatherIcon: String?
): Int {
    if (weatherIcon == "01d" || weatherIcon == "01n") {
        return R.drawable.weather_clear_sky
    }
    if (weatherIcon == "50d" || weatherIcon == "50n") {
        return R.drawable.weather_foggy
    }
    if (weatherIcon == "50d") {
        return R.drawable.weather_dust
    }
    if (weatherIcon == "03d") {
        return R.drawable.weather_haze
    }
    if (weatherIcon == "11d") {
        return R.drawable.weather_tornado
    }
    return R.drawable.weather_icon_null
}