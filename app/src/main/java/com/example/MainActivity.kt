package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainHostScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TalabatOrange
import com.example.ui.viewmodel.CodViewModel

enum class AppScreenState {
    Splash,
    Onboarding,
    Login,
    MainHost
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: CodViewModel = viewModel()
                val currentUserState by viewModel.currentUser.collectAsState()
                
                var currentScreen by remember { mutableStateOf(AppScreenState.Splash) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreenState.Splash -> {
                                SplashScreen(
                                    onSplashComplete = {
                                        currentScreen = AppScreenState.Onboarding
                                    }
                                )
                            }
                            AppScreenState.Onboarding -> {
                                OnboardingScreen(
                                    onOnboardingComplete = {
                                        currentScreen = AppScreenState.Login
                                    }
                                )
                            }
                            AppScreenState.Login -> {
                                LoginScreen(
                                    onLoginSuccess = { name, email, riderType ->
                                        // Save to database, then transition
                                        viewModel.updateRiderProfile(name, email, riderType)
                                        currentScreen = AppScreenState.MainHost
                                    }
                                )
                            }
                            AppScreenState.MainHost -> {
                                val user = currentUserState
                                if (user != null) {
                                    MainHostScreen(
                                        viewModel = viewModel,
                                        user = user
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = TalabatOrange)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
