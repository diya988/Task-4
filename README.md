#  WeatherNow – Android Weather Application

WeatherNow is an Android weather application developed using **Kotlin and Jetpack Compose**. It allows users to search for cities, view current weather conditions, and check a five-day weather forecast.

This project was enhanced as part of **ApexPlanet Internship – Task 4: Advanced Features and Testing**.

##  Features

- Firebase Authentication: User registration and login using email and password.
- City Search: Search for weather information by city name.
- Current Weather: View temperature, feels-like temperature, humidity, wind speed, and weather conditions.
- Five-Day Forecast: Check upcoming weather conditions and daily minimum and maximum temperatures.
- Favorite Cities: Save and manage favorite cities.
- Temperature Conversion: Switch between Celsius and Fahrenheit.
- Light and Dark Theme: Choose between different app appearances.
- Push Notification Support: Firebase Cloud Messaging integration for receiving notifications.
- Error Handling: Handles invalid city names, empty responses, and network errors.

##  Technologies Used

| Technology               | Purpose                                     |
|--------------------------|---------------------------------------------|
| Kotlin                   | Main programming language                   |
| Jetpack Compose          | UI development                              |
| Firebase Authentication  | User login and registration                 |
| Firebase Cloud Messaging | Push notification support                   |
| Open-Meteo API           | Weather and geocoding data                  |
| SharedPreferences        | Local storage for preferences and favorites |
| Coroutines               | Asynchronous operations                     |
| JUnit                    | Unit testing                                |
| MockWebServer            | API repository testing                      |
| Compose UI Testing       | UI testing                                  |

##  Weather API

WeatherNow uses the free [Open-Meteo API](https://open-meteo.com/) for weather and geocoding information.

No weather API key is required.

##  Application Screens

The application includes authentication and weather dashboard screens, city search, forecast information, favorites, and theme settings.


##  Setup and Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/diya988/Task-4.git
   ```

2. Open the project in Android Studio.

3. Configure Firebase:
   - Create or select a Firebase project.
   - Register an Android app with the application's package name.
   - Download `google-services.json`.
   - Place the file inside the `app/` directory.
   - Enable Email/Password authentication in Firebase Authentication.

4. Sync the project with Gradle.

5. Run the application on an Android emulator or a compatible Android device.

##  Testing

The project includes different levels of testing to verify application functionality and reliability.

Unit Testing: Tests utility functions such as temperature conversion and weather condition descriptions.

Integration Testing: Uses MockWebServer to test weather repository responses and JSON parsing.

UI Testing: Uses Jetpack Compose UI Testing to execute instrumented tests on an Android emulator.

Manual Testing: Covers authentication, city search, weather information, forecasts, favourites, temperature conversion, themes, and notification permissions.

Run Local Unit Tests

./gradlew test

Run Android Instrumented UI Tests

./gradlew connectedAndroidTest

Windows users:

Run these commands separately in the project terminal:
.\gradlew.bat test
.\gradlew.bat connectedAndroidTest

##  Push Notifications

Firebase Cloud Messaging is integrated to support receiving push notifications.

A notification sender or backend service is needed to send automated notifications to users.

##  Internship Task

**Internship:** ApexPlanet Software Pvt. Ltd.

**Task:** Task 4 – Advanced Features and Testing

The project demonstrates the implementation of advanced Android application features, Firebase integration, API communication, and automated testing.

##  Developer

**Diya Bhattacharjee**

GitHub: [diya988](https://github.com/diya988)

---

⭐ If you find this project interesting, feel free to explore the repository!
