package com.cscorner.task3

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class WeatherRepository(
    private val geocodingBaseUrl: String = "https://geocoding-api.open-meteo.com/v1/search",
    private val forecastBaseUrl: String = "https://api.open-meteo.com/v1/forecast"
) {
    suspend fun fetchWeather(city: String): WeatherInfo = withContext(Dispatchers.IO) {
        require(city.isNotBlank()) { "Please enter a city name." }

        val encodedCity = URLEncoder.encode(city.trim(), "UTF-8")
        val geoUrl = URL("$geocodingBaseUrl?name=$encodedCity&count=1&language=en&format=json")
        val geo = getJson(geoUrl)
        val locations = geo.optJSONArray("results")
            ?: throw IllegalArgumentException("City not found. Check the spelling.")

        if (locations.length() == 0) {
            throw IllegalArgumentException("City not found. Try another city.")
        }

        val location = locations.getJSONObject(0)
        val latitude = location.getDouble("latitude")
        val longitude = location.getDouble("longitude")
        val cityName = location.optString("name", city)
        val country = location.optString("country", "")

        val forecastUrl = URL(
            "$forecastBaseUrl?latitude=$latitude&longitude=$longitude" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m,weather_code" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                    "&forecast_days=5&timezone=auto"
        )

        val response = getJson(forecastUrl)
        val current = response.getJSONObject("current")
        val daily = response.getJSONObject("daily")
        val dates = daily.getJSONArray("time")
        val codes = daily.getJSONArray("weather_code")
        val highs = daily.getJSONArray("temperature_2m_max")
        val lows = daily.getJSONArray("temperature_2m_min")

        val dailyItems = (0 until dates.length()).map { index ->
            DailyForecast(
                date = dates.getString(index),
                minTemperature = lows.getDouble(index),
                maxTemperature = highs.getDouble(index),
                condition = weatherDescription(codes.getInt(index))
            )
        }

        WeatherInfo(
            city = if (country.isNotBlank()) "$cityName, $country" else cityName,
            temperature = current.getDouble("temperature_2m"),
            feelsLike = current.getDouble("apparent_temperature"),
            humidity = current.getInt("relative_humidity_2m"),
            wind = current.getDouble("wind_speed_10m"),
            condition = weatherDescription(current.getInt("weather_code")),
            forecast = dailyItems
        )
    }

    private fun getJson(url: URL): JSONObject {
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
        }

        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (status !in 200..299) {
                throw IllegalStateException("Weather service error ($status). Please try again.")
            }
            if (body.isBlank()) {
                throw IllegalStateException("The weather service returned an empty response.")
            }
            return JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }
}

fun formatTemperature(celsius: Double, useFahrenheit: Boolean): String {
    val value = if (useFahrenheit) celsiusToFahrenheit(celsius) else celsius
    return String.format(Locale.US, "%.1f°%s", value, if (useFahrenheit) "F" else "C")
}
