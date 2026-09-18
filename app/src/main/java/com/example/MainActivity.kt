package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.AppScreen
import com.example.ui.components.CyberBackgroundVideo
import com.example.ui.screens.DeviceScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.SafeZoneScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.KrishnaConfigTheme

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      KrishnaConfigTheme {
        val currentScreen by viewModel.currentScreen.collectAsState()
        val userSession by viewModel.userSession.collectAsState()
        val config by viewModel.config.collectAsState()
        val statusMessage by viewModel.statusMessage.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(statusMessage) {
          statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
          }
        }

        Scaffold(
          snackbarHost = { SnackbarHost(snackbarHostState) },
          contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            // Live background video looping
            CyberBackgroundVideo(
              videoUrls = config.videoUrls,
              modifier = Modifier.fillMaxSize()
            )

            // Dynamic screen hierarchy
            when (currentScreen) {
              AppScreen.SPLASH -> {
                SplashScreen(
                  onTimeout = {
                    if (userSession.userId.isNotBlank()) {
                      if (userSession.verificationStatus == com.example.data.VerificationStatus.APPROVED) {
                        viewModel.navigateTo(AppScreen.SAFE_ZONE)
                      } else {
                        viewModel.navigateTo(AppScreen.DEVICE)
                      }
                    } else {
                      viewModel.navigateTo(AppScreen.LOGIN)
                    }
                  }
                )
              }

              AppScreen.LOGIN -> {
                LoginScreen(
                  onLogin = { email, pass -> viewModel.login(email, pass) },
                  onRegister = { email, pass -> viewModel.register(email, pass) },
                  onForgotPassword = { email -> viewModel.resetPassword(email) },
                  isLoading = isLoading
                )
              }

              AppScreen.DEVICE -> {
                DeviceScreen(
                  userSession = userSession,
                  onProceed = { name -> viewModel.saveDeviceName(name) }
                )
              }

              AppScreen.PAYMENT -> {
                PaymentScreen(
                  userSession = userSession,
                  config = config,
                  onSubmitPayment = { uri, ref -> viewModel.submitPayment(uri, ref) },
                  isLoading = isLoading
                )
              }

              AppScreen.SAFE_ZONE -> {
                SafeZoneScreen(
                  userSession = userSession,
                  config = config,
                  onUpdateUID = { uid -> viewModel.updateSafeZoneUID(uid) },
                  onToggleProtection = { enable -> viewModel.toggleProtection(enable) },
                  onFixAntihack = { viewModel.fixAntihack() },
                  onRemoveBlacklist = { viewModel.removeBlacklist() },
                  onLogout = { viewModel.logout() }
                )
              }
            }
          }
        }
      }
    }
  }
}
