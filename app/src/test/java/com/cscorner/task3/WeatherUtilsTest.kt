package com.cscorner.task3

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherUtilsTest {

    @Test
    fun celsiusToFahrenheit_convertsFreezingPoint() {
        assertEquals(32.0, celsiusToFahrenheit(0.0), 0.001)
    }

    @Test
    fun celsiusToFahrenheit_convertsBoilingPoint() {
        assertEquals(212.0, celsiusToFahrenheit(100.0), 0.001)
    }

    @Test
    fun weatherDescription_recognizesClearSky() {
        assertTrue(weatherDescription(0).contains("Clear sky"))
    }

    @Test
    fun weatherDescription_recognizesThunderstorm() {
        assertTrue(weatherDescription(95).contains("Thunderstorm"))
    }

    @Test
    fun formatTemperature_usesFahrenheitWhenSelected() {
        assertEquals("32.0°F", formatTemperature(0.0, true))
    }
}
