package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppScreen
import com.example.ui.components.CyberBackgroundVideo
import com.example.ui.screens.DeviceScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.SafeZoneScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
        val userSession by viewModel.userSession.collectAsStateWithLifecycle()
        val config by viewModel.config.collectAsStateWithLifecycle()
        val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(statusMessage) {
          statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
          }
        }

        Box(modifier = Modifier.fillMaxSize()) {
          // Looping background video behind ALL screens
          CyberBackgroundVideo(
            videoUrls = config.videoUrls,
            modifier = Modifier.fillMaxSize()
          )

          // Transparent overlay scaffold
          Scaffold(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ) {
              AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                  (fadeIn() + slideInHorizontally { it / 4 }) togetherWith
                      (fadeOut() + slideOutHorizontally { -it / 4 })
                },
                label = "screen_transition"
              ) { screen ->
                when (screen) {
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
                      initialDeviceName = userSession.deviceName,
                      onProceed = { name -> viewModel.saveDeviceName(name) }
                    )
                  }

                  AppScreen.PAYMENT -> {
                    PaymentScreen(
                      userSession = userSession,
                      config = config,
                      onSubmitPayment = { uri, ref -> viewModel.submitPayment(uri, ref) },
                      onSimulateAdminDecision = { approved -> viewModel.simulateAdminDecision(approved) },
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
  }
}
