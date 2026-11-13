package com.cscorner.task3






import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class WeatherNowUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun authenticationScreen_displaysBrandAndEmailField() {
        composeRule.setContent {
            MaterialTheme {
                AuthScreen(
                    isRegister = false,
                    onSwitch = {},
                    onSuccess = {}
                )
            }
        }

        composeRule.onNodeWithText("WeatherNow").assertExists()
        composeRule.onNodeWithText("Email").assertExists()
        composeRule.onNodeWithText("Login").assertExists()
    }
}
