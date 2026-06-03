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
import com.example.ui.screens.RiderTypeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TalabatOrange
import com.example.ui.viewmodel.CodViewModel
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

enum class AppScreenState {
    Splash,
    Onboarding,
    Login,
    RiderType,
    MainHost
}

class MainActivity : ComponentActivity() {
    private var _viewModel: CodViewModel? = null
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            _viewModel?.startLocationTracking()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: CodViewModel = viewModel()
                _viewModel = viewModel
                val currentUserState by viewModel.currentUser.collectAsState()
                
                LaunchedEffect(Unit) {
                    val fine = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarse = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    
                    if (fine || coarse) {
                        viewModel.startLocationTracking()
                    } else {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                var currentScreen by remember { mutableStateOf(AppScreenState.Splash) }
                var tempPhone by remember { mutableStateOf("+974 55 123 456") }

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
                                    onLoginSuccess = { phone, _ ->
                                        tempPhone = phone
                                        currentScreen = AppScreenState.RiderType
                                    }
                                )
                            }
                            AppScreenState.RiderType -> {
                                RiderTypeScreen(
                                    onTypeSelected = { selectedType ->
                                        viewModel.updateRiderProfile("John Rider", tempPhone, selectedType)
                                        currentScreen = AppScreenState.MainHost
                                    },
                                    onBack = {
                                        currentScreen = AppScreenState.Login
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
