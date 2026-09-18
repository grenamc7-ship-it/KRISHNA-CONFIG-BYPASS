package com.example.security

import android.content.Context
import android.os.Build
import java.io.File

object SecurityIntegrityHelper {

  fun isDeviceRooted(): Boolean {
    return checkBuildTags() || checkSuBinary() || checkSuperuserApks()
  }

  private fun checkBuildTags(): Boolean {
    val buildTags = Build.TAGS
    return buildTags != null && buildTags.contains("test-keys")
  }

  private fun checkSuBinary(): Boolean {
    val paths = arrayOf(
      "/system/app/Superuser.apk",
      "/sbin/su",
      "/system/bin/su",
      "/system/xbin/su",
      "/data/local/xbin/su",
      "/data/local/bin/su",
      "/system/sd/xbin/su",
      "/system/bin/failsafe/su",
      "/data/local/su"
    )
    for (path in paths) {
      if (File(path).exists()) return true
    }
    return false
  }

  private fun checkSuperuserApks(): Boolean {
    val suPackages = listOf(
      "com.noshufou.android.su",
      "com.thirdparty.superuser",
      "eu.chainfire.supersu",
      "com.koushikdutta.superuser",
      "com.topjohnwu.magisk"
    )
    return false
  }

  fun runIntegrityScan(context: Context): SecurityReport {
    val isRooted = isDeviceRooted()
    val isDebuggerAttached = android.os.Debug.isDebuggerConnected()
    val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
        Build.MODEL.contains("google_sdk") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for x86")

    return SecurityReport(
      isRooted = isRooted,
      isDebuggerAttached = isDebuggerAttached,
      isEmulator = isEmulator,
      isSecure = !isRooted && !isDebuggerAttached
    )
  }
}

data class SecurityReport(
  val isRooted: Boolean,
  val isDebuggerAttached: Boolean,
  val isEmulator: Boolean,
  val isSecure: Boolean
)
