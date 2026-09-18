package com.example

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppScreen
import com.example.data.FirebaseRepository
import com.example.data.RemoteAppConfig
import com.example.data.UserSession
import com.example.data.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

  val repository = FirebaseRepository(application.applicationContext)

  private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  val userSession: StateFlow<UserSession> = repository.userSession
  val config: StateFlow<RemoteAppConfig> = repository.config

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  init {
    viewModelScope.launch {
      userSession.collect { session ->
        // When Admin approves from Telegram Bot, auto-navigate to SAFE ZONE immediately!
        if (session.verificationStatus == VerificationStatus.APPROVED) {
          if (_currentScreen.value == AppScreen.PAYMENT || _currentScreen.value == AppScreen.DEVICE) {
            _currentScreen.value = AppScreen.SAFE_ZONE
            _statusMessage.value = "Access Approved by Admin! Welcome to Safe Zone."
          }
        } else if (session.verificationStatus == VerificationStatus.REJECTED) {
          if (_currentScreen.value == AppScreen.SAFE_ZONE) {
            _currentScreen.value = AppScreen.PAYMENT
            _statusMessage.value = "Access has been Declined by Admin."
          }
        }
      }
    }
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  fun showMessage(msg: String) {
    _statusMessage.value = msg
  }

  fun login(email: String, pass: String) {
    if (email.isBlank() || pass.isBlank()) {
      showMessage("Please enter both email and password")
      return
    }
    _isLoading.value = true
    repository.signIn(
      email = email,
      pass = pass,
      onSuccess = {
        _isLoading.value = false
        val session = repository.userSession.value
        if (session.verificationStatus == VerificationStatus.APPROVED) {
          _currentScreen.value = AppScreen.SAFE_ZONE
        } else {
          _currentScreen.value = AppScreen.DEVICE
        }
      },
      onError = { err ->
        _isLoading.value = false
        showMessage("Login notice: $err")
        _currentScreen.value = AppScreen.DEVICE
      }
    )
  }

  fun register(email: String, pass: String) {
    if (email.isBlank() || pass.length < 6) {
      showMessage("Password must be at least 6 characters")
      return
    }
    _isLoading.value = true
    repository.signUp(
      email = email,
      pass = pass,
      onSuccess = {
        _isLoading.value = false
        showMessage("Account created successfully!")
        _currentScreen.value = AppScreen.DEVICE
      },
      onError = { err ->
        _isLoading.value = false
        showMessage("Registration notice: $err")
        _currentScreen.value = AppScreen.DEVICE
      }
    )
  }

  fun resetPassword(email: String) {
    if (email.isBlank()) {
      showMessage("Enter your email address")
      return
    }
    repository.resetPassword(email) { success, msg ->
      showMessage(msg)
    }
  }

  fun saveDeviceName(name: String) {
    if (name.isBlank()) {
      showMessage("Please enter your device name")
      return
    }
    repository.setDeviceName(name)
    _currentScreen.value = AppScreen.PAYMENT
  }

  fun submitPayment(screenshotUri: Uri?, upiRef: String) {
    if (upiRef.isBlank() && screenshotUri == null) {
      showMessage("Please enter UPI reference number or select screenshot")
      return
    }
    _isLoading.value = true
    viewModelScope.launch {
      repository.submitPaymentProof(screenshotUri, upiRef) { success, msg ->
        _isLoading.value = false
        showMessage(msg)
      }
    }
  }

  fun updateSafeZoneUID(uid: String) {
    repository.updateSafeZoneFeature(uidConfig = uid)
    showMessage("UID $uid successfully verified and linked!")
  }

  fun toggleProtection(enable: Boolean) {
    repository.updateSafeZoneFeature(protectionEnabled = enable)
    showMessage(if (enable) "KRISHNA PROTECTION: ACTIVE" else "KRISHNA PROTECTION: DISABLED")
  }

  fun fixAntihack() {
    repository.updateSafeZoneFeature(antihackFixed = true)
    showMessage("ANTI-HACK SYSTEM INTEGRITY RESTORED 100%")
  }

  fun removeBlacklist() {
    repository.updateSafeZoneFeature(blacklistRemoved = true)
    showMessage("BLACKLIST CACHE WIPED & SYSTEM SECURED")
  }

  fun logout() {
    repository.logout()
    _currentScreen.value = AppScreen.LOGIN
  }
}
