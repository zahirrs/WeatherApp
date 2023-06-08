package com.example.weatherapp.utils

class NullResponseException(message: String = "Error happened here") : Exception(message)
class FailureResponse(message: String) : Exception(message)