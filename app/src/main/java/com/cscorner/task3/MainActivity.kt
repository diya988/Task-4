package com.cscorner.task3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WeatherNowApp() }
    }
}

@Composable
fun WeatherNowApp() {
    val auth = remember { FirebaseAuth.getInstance() }
    var loggedIn by remember { mutableStateOf(auth.currentUser != null) }
    var register by remember { mutableStateOf(false) }
    var darkTheme by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (darkTheme) androidx.compose.material3.darkColorScheme()
        else androidx.compose.material3.lightColorScheme()
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (loggedIn) {
                WeatherScreen(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = it },
                    onLogout = {
                        auth.signOut()
                        loggedIn = false
                    }
                )
            } else {
                AuthScreen(
                    isRegister = register,
                    onSwitch = { register = !register },
                    onSuccess = { loggedIn = true }
                )
            }
        }
    }
}

@Composable
fun AuthScreen(
    isRegister: Boolean,
    onSwitch: () -> Unit,
    onSuccess: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌦️", fontSize = 52.sp)
        Text("WeatherNow", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(if (isRegister) "Create your account" else "Welcome back!")
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (minimum 6 characters)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                    message = "Please enter a valid email address."
                } else if (password.length < 6) {
                    message = "Password must contain at least 6 characters."
                } else {
                    loading = true
                    message = ""
                    val task = if (isRegister) {
                        auth.createUserWithEmailAndPassword(email.trim(), password)
                    } else {
                        auth.signInWithEmailAndPassword(email.trim(), password)
                    }
                    task.addOnCompleteListener { result ->
                        loading = false
                        if (result.isSuccessful) {
                            onSuccess()
                        } else {
                            message = result.exception?.localizedMessage
                                ?: "Authentication failed. Please try again."
                        }
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text(if (isRegister) "Create Account" else "Login")
            }
        }

        TextButton(onClick = onSwitch) {
            Text(
                if (isRegister) "Already have an account? Login"
                else "New user? Create an account"
            )
        }

        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun WeatherScreen(
    darkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { WeatherRepository() }
    val preferences = remember {
        context.getSharedPreferences(
            "weathernow_preferences",
            android.content.Context.MODE_PRIVATE
        )
    }
    val favoriteCities = remember {
        mutableStateListOf<String>().apply {
            addAll(preferences.getStringSet("favorite_cities", emptySet()) ?: emptySet())
        }
    }

    var city by remember { mutableStateOf("Kolkata") }
    var weather by remember { mutableStateOf<WeatherInfo?>(null) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var useFahrenheit by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener {
                    Log.d("WeatherNowFCM", "FCM token: $it")
                    Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Could not register for notifications",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            Toast.makeText(
                context,
                "Notification permission was not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener {
                    Log.d("WeatherNowFCM", "FCM token: $it")
                    Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Could not register for notifications",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    fun saveFavorites() {
        preferences.edit().putStringSet("favorite_cities", favoriteCities.toSet()).apply()
    }

    fun loadWeather(searchCity: String = city) {
        if (searchCity.isBlank()) {
            message = "Please enter a city name."
            return
        }

        scope.launch {
            loading = true
            message = ""
            try {
                weather = repository.fetchWeather(searchCity.trim())
                city = searchCity.trim()
            } catch (error: Exception) {
                message = error.message ?: "Unable to fetch weather. Check your connection."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadWeather()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("WeatherNow 🌦️", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Your weather companion", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onLogout) { Text("Logout") }
        }

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Search city") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { loadWeather(city) },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Searching..." else "Search Weather")
        }

        if (favoriteCities.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Favourite cities", fontWeight = FontWeight.SemiBold)
            favoriteCities.forEach { favorite ->
                TextButton(
                    onClick = {
                        city = favorite
                        loadWeather(favorite)
                    }
                ) {
                    Text("⭐ $favorite")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Show Fahrenheit")
            Switch(checked = useFahrenheit, onCheckedChange = { useFahrenheit = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Dark theme")
            Switch(checked = darkTheme, onCheckedChange = onToggleTheme)
        }

        Button(
            onClick = { enableNotifications() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable notifications")
        }

        if (loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
            Text("Fetching weather...")
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { loadWeather(city) }) { Text("Try Again") }
        }

        weather?.let { info ->
            Spacer(Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(info.city, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(info.condition, fontSize = 18.sp)
                    Text(
                        formatTemperature(info.temperature, useFahrenheit),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Feels like ${formatTemperature(info.feelsLike, useFahrenheit)}")
                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💧 Humidity")
                            Text("${info.humidity}%", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💨 Wind")
                            Text("${info.wind} km/h", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            val selected = city.trim()
                            if (selected.isNotBlank() && !favoriteCities.contains(selected)) {
                                favoriteCities.add(selected)
                                saveFavorites()
                            }
                        }
                    ) {
                        Text(
                            if (favoriteCities.contains(city.trim())) {
                                "★ Saved to favourites"
                            } else {
                                "☆ Save city"
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("5-Day Forecast", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            info.forecast.forEach { day ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(day.date, fontWeight = FontWeight.SemiBold)
                            Text(day.condition, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            "${formatTemperature(day.minTemperature, useFahrenheit)} / " +
                                    formatTemperature(day.maxTemperature, useFahrenheit),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Weather data provided by Open-Meteo",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
