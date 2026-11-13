
package com.cscorner.task3

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {

    private lateinit var geoServer: MockWebServer
    private lateinit var forecastServer: MockWebServer

    @Before
    fun setUp() {
        geoServer = MockWebServer()
        forecastServer = MockWebServer()
        geoServer.start()
        forecastServer.start()
    }

    @After
    fun tearDown() {
        geoServer.shutdown()
        forecastServer.shutdown()
    }

    @Test
    fun fetchWeather_parsesGeocodingCurrentConditionsAndFiveDays() {
        runBlocking {
            geoServer.enqueue(
                MockResponse().setResponseCode(200).setBody(
                    """
                    {
                      "results": [
                        {
                          "name": "Kolkata",
                          "country": "India",
                          "latitude": 22.57,
                          "longitude": 88.36
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )

            forecastServer.enqueue(
                MockResponse().setResponseCode(200).setBody(
                    """
                    {
                      "current": {
                        "temperature_2m": 30.5,
                        "relative_humidity_2m": 70,
                        "apparent_temperature": 34.0,
                        "wind_speed_10m": 12.0,
                        "weather_code": 3
                      },
                      "daily": {
                        "time": ["2026-11-13", "2026-11-14", "2026-11-15", "2026-11-16", "2026-11-17"],
                        "weather_code": [3, 1, 2, 61, 0],
                        "temperature_2m_max": [31, 32, 30, 29, 31],
                        "temperature_2m_min": [24, 25, 23, 22, 24]
                      }
                    }
                    """.trimIndent()
                )
            )

            val repository = WeatherRepository(
                geocodingBaseUrl = geoServer.url("/v1/search").toString(),
                forecastBaseUrl = forecastServer.url("/v1/forecast").toString()
            )

            val result = repository.fetchWeather("Kolkata")

            assertEquals("Kolkata, India", result.city)
            assertEquals(30.5, result.temperature, 0.001)
            assertEquals(70, result.humidity)
            assertEquals(5, result.forecast.size)
            assertTrue(result.condition.contains("Overcast"))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun fetchWeather_rejectsBlankCity() {
        runBlocking {
            WeatherRepository().fetchWeather("   ")
        }
    }
}
