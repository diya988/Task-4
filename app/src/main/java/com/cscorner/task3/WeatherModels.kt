package com.cscorner.task3

data class DailyForecast(
    val date: String,
    val minTemperature: Double,
    val maxTemperature: Double,
    val condition: String
)

data class WeatherInfo(
    val city: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val wind: Double,
    val condition: String,
    val forecast: List<DailyForecast>
)

fun weatherDescription(code: Int): String = when (code) {
    0 -> "Clear sky ☀️"
    1 -> "Mainly clear 🌤️"
    2 -> "Partly cloudy ⛅"
    3 -> "Overcast ☁️"
    45, 48 -> "Fog 🌫️"
    51, 53, 55 -> "Drizzle 🌦️"
    56, 57 -> "Freezing drizzle 🌧️"
    61, 63, 65 -> "Rain 🌧️"
    66, 67 -> "Freezing rain 🌧️"
    71, 73, 75 -> "Snow ❄️"
    77 -> "Snow grains ❄️"
    80, 81, 82 -> "Rain showers 🌧️"
    85, 86 -> "Snow showers 🌨️"
    95 -> "Thunderstorm ⛈️"
    96, 99 -> "Thunderstorm with hail ⛈️"
    else -> "Mixed weather"
}

fun celsiusToFahrenheit(celsius: Double): Double = (celsius * 9.0 / 5.0) + 32.0
