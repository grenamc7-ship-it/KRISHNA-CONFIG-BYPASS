package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RemoteAppConfig
import com.example.data.UserSession
import com.example.ui.components.BloodyTitle
import com.example.ui.components.Cyber3DButton
import com.example.ui.components.Cyber3DFeatureCard
import com.example.ui.components.CyberGlassCard
import com.example.ui.theme.BloodRedDark
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.NeonGreenDark
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SafeZoneScreen(
  userSession: UserSession,
  config: RemoteAppConfig,
  onToggleProtection: (Boolean) -> Unit,
  onFixAntihack: () -> Unit,
  onRemoveBlacklist: () -> Unit,
  onUpdateUID: (String) -> Unit,
  onLogout: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var uidInput by remember { mutableStateOf(userSession.userId) }
  var isUidVerified by remember { mutableStateOf(userSession.userId.isNotBlank()) }

  var isScanningAntihack by remember { mutableStateOf(false) }
  var antihackProgress by remember { mutableFloatStateOf(0f) }

  var isPurgingBlacklist by remember { mutableStateOf(false) }
  var blacklistProgress by remember { mutableFloatStateOf(0f) }

  // Check Manage External Storage Permission
  fun checkStoragePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      Environment.isExternalStorageManager()
    } else {
      true
    }
  }

  var hasStorageAccess by remember { mutableStateOf(checkStoragePermission()) }

  // Continuous listener for permission return
  LaunchedEffect(Unit) {
    while (true) {
      delay(1200)
      hasStorageAccess = checkStoragePermission()
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "sz_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.985f,
    targetValue = 1.015f,
    animationSpec = infiniteRepeatable(
      animation = tween(1600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "sz_scale"
  )

  val neonGlowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "sz_glow"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 18.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // 3D Bloody Cyber Title with Animated Drips
    BloodyTitle(
      titleSize = 30.sp,
      subtitle = "SAFE ZONE ACTIVATED • RIG SHIELDED",
      showDrips = true,
      dropHeight = 16.dp,
      modifier = Modifier.padding(bottom = 16.dp)
    )

    // Ultra-3D Holographic Safe Zone Shield Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .scale(pulseScale)
    ) {
      // 3D Shadow Base
      Box(
        modifier = Modifier
          .matchParentSize()
          .offset(y = 5.dp)
          .background(
            brush = Brush.verticalGradient(
              listOf(Color.Transparent, Color(0x60003311), Color(0x90001808))
            ),
            shape = RoundedCornerShape(20.dp)
          )
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(20.dp),
            spotColor = NeonGreenBright.copy(alpha = neonGlowAlpha * 0.8f),
            ambientColor = NeonGreen.copy(alpha = 0.4f)
          )
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(
                Color(0x35FFFFFF),
                Color(0x30002A0D),
                CyberDarkBg.copy(alpha = 0.6f),
                Color(0x50000000)
              )
            ),
            shape = RoundedCornerShape(20.dp)
          )
          .border(
            width = 2.dp,
            brush = Brush.linearGradient(
              listOf(NeonGreenBright.copy(alpha = neonGlowAlpha), NeonGreen.copy(alpha = 0.4f), Color.White.copy(alpha = 0.3f))
            ),
            shape = RoundedCornerShape(20.dp)
          )
          .drawBehind {
            // Specular top highlight
            drawLine(
              color = Color.White.copy(alpha = 0.6f),
              start = Offset(24.dp.toPx(), 1.dp.toPx()),
              end = Offset(size.width - 24.dp.toPx(), 1.dp.toPx()),
              strokeWidth = 2.dp.toPx()
            )
          }
          .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(NeonGreenDark, CircleShape)
                .border(1.5.dp, NeonGreenBright, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = NeonGreenBright,
                modifier = Modifier.size(20.dp)
              )
            }
            Text(
              text = "SAFE ZONE ACTIVATED",
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = NeonGreenBright,
              letterSpacing = 2.sp
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "NOW YOU ARE ENTERED YOUR SAFE ZONE",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = TextWhite,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 1. ALL FILES ACCESS PERMISSION (3D FEATURE CARD)
    Cyber3DFeatureCard(
      title = "• ALL FILES ACCESS PERMISSION",
      subtitle = if (hasStorageAccess) "PERMISSION: GRANTED ✓" else "STORAGE ACCESS: REQUIRED",
      icon = Icons.Default.FolderShared,
      isActive = hasStorageAccess
    ) {
      Cyber3DButton(
        text = if (hasStorageAccess) "ACTIVE ✓" else "GRANT",
        onClick = {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
              val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
              }
              context.startActivity(intent)
            } catch (e: Exception) {
              val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
              context.startActivity(intent)
            }
          } else {
            hasStorageAccess = true
            Toast.makeText(context, "Storage Access Active", Toast.LENGTH_SHORT).show()
          }
        },
        isGreen = hasStorageAccess,
        modifier = Modifier.width(115.dp),
        height = 42.dp,
        fontSize = 11.sp,
        testTag = "grant_storage_btn"
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2. UID VERIFICATION (3D GLASS CARD)
    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = isUidVerified
    ) {
      Text(
        text = "• VERIFY UID FOR SAFE ZONE",
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = TextWhite
      )
      Text(
        text = if (isUidVerified) "UID LINKED & VERIFIED ✓" else "ENTER IN-GAME / DEVICE UID TO BIND",
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = if (isUidVerified) NeonGreenBright else TextMuted
      )
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = uidInput,
          onValueChange = { uidInput = it },
          placeholder = { Text("Enter UID", color = TextMuted) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedBorderColor = NeonGreen,
            unfocusedBorderColor = TextMuted.copy(alpha = 0.4f),
            focusedContainerColor = CyberDarkBg.copy(alpha = 0.45f),
            unfocusedContainerColor = CyberDarkBg.copy(alpha = 0.35f)
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("uid_input_field")
        )

        Cyber3DButton(
          text = if (isUidVerified) "VERIFIED" else "VERIFY",
          onClick = {
            if (uidInput.isNotBlank()) {
              isUidVerified = true
              onUpdateUID(uidInput.trim())
            }
          },
          isGreen = isUidVerified,
          icon = if (isUidVerified) Icons.Default.CheckCircle else Icons.Default.VerifiedUser,
          modifier = Modifier.width(125.dp),
          height = 48.dp,
          fontSize = 11.sp,
          testTag = "verify_uid_btn"
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 3. ENABLE PROTECTION (3D FEATURE CARD)
    Cyber3DFeatureCard(
      title = "• ENABLE PROTECTION",
      subtitle = if (userSession.isProtectionEnabled) "STATUS: SHIELDED & ACTIVE" else "STATUS: STANDBY",
      icon = if (userSession.isProtectionEnabled) Icons.Default.Shield else Icons.Default.LockOpen,
      isActive = userSession.isProtectionEnabled
    ) {
      Switch(
        checked = userSession.isProtectionEnabled,
        onCheckedChange = { onToggleProtection(it) },
        colors = SwitchDefaults.colors(
          checkedThumbColor = NeonGreenBright,
          checkedTrackColor = NeonGreenDark,
          uncheckedThumbColor = BloodRedPrimary,
          uncheckedTrackColor = CyberDarkBg
        ),
        modifier = Modifier.testTag("protection_toggle")
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 4. FIX SYSTEM ANTIHACK (3D FEATURE CARD)
    Cyber3DFeatureCard(
      title = "• FIX SYSTEM ANTIHACK",
      subtitle = if (userSession.isAntihackFixed) "STATUS: INTEGRITY 100%" else "CORE INTEGRITY: SCAN REQUIRED",
      icon = Icons.Default.Build,
      isActive = userSession.isAntihackFixed
    ) {
      Cyber3DButton(
        text = if (userSession.isAntihackFixed) "FIXED ✓" else "FIX NOW",
        onClick = {
          if (!isScanningAntihack) {
            isScanningAntihack = true
            antihackProgress = 0f
            coroutineScope.launch {
              for (i in 1..10) {
                delay(140)
                antihackProgress = i / 10f
              }
              isScanningAntihack = false
              onFixAntihack()
            }
          }
        },
        isLoading = isScanningAntihack,
        isGreen = userSession.isAntihackFixed,
        modifier = Modifier.width(115.dp),
        height = 42.dp,
        fontSize = 11.sp,
        testTag = "fix_antihack_btn"
      )
    }

    AnimatedVisibility(visible = isScanningAntihack) {
      Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        LinearProgressIndicator(
          progress = { antihackProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = NeonGreenBright,
          trackColor = CyberDarkBg
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 5. REMOVE BLACKLIST (3D FEATURE CARD)
    Cyber3DFeatureCard(
      title = "• REMOVE BLACKLIST",
      subtitle = if (userSession.isBlacklistRemoved) "BLACKLIST: 0 ENTRIES" else "PURGE ALL SYSTEM LOGS",
      icon = Icons.Default.CleaningServices,
      isActive = userSession.isBlacklistRemoved
    ) {
      Cyber3DButton(
        text = if (userSession.isBlacklistRemoved) "CLEARED ✓" else "PURGE",
        onClick = {
          if (!isPurgingBlacklist) {
            isPurgingBlacklist = true
            blacklistProgress = 0f
            coroutineScope.launch {
              for (i in 1..10) {
                delay(140)
                blacklistProgress = i / 10f
              }
              isPurgingBlacklist = false
              onRemoveBlacklist()
            }
          }
        },
        isLoading = isPurgingBlacklist,
        isGreen = userSession.isBlacklistRemoved,
        modifier = Modifier.width(115.dp),
        height = 42.dp,
        fontSize = 11.sp,
        testTag = "remove_blacklist_btn"
      )
    }

    AnimatedVisibility(visible = isPurgingBlacklist) {
      Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        LinearProgressIndicator(
          progress = { blacklistProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = BloodRedPrimary,
          trackColor = CyberDarkBg
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 6. DEVICE TELEMETRY & UNIQUE ACTIVATION KEY (3D Cyber Glass Card)
    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = true
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(NeonGreenDark, CircleShape)
            .border(1.dp, NeonGreenBright, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Key, null, tint = NeonGreenBright, modifier = Modifier.size(18.dp))
        }
        Text(
          text = "RIG TELEMETRY & ACTIVATION KEY",
          fontSize = 12.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.Monospace,
          color = NeonGreenBright
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberDarkBg.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
          .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text("• RIG MODEL: ${userSession.deviceModel.ifBlank { Build.MODEL }}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextWhite)
        Text("• ANDROID: ${userSession.androidVersion.ifBlank { "Android " + Build.VERSION.RELEASE }}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextWhite)
        Text("• BUILD OS: ${userSession.osVersion.ifBlank { Build.DISPLAY }}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
        Text("• ACTIVATION KEY:\n  ${userSession.activationKey}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = NeonGreenBright)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 7. OFFICIAL ADMIN CONTACTS (3D Cyber Glass Card)
    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = false
    ) {
      Text(
        text = "OFFICIAL ADMIN CONTACTS",
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        color = BloodRedGlow,
        letterSpacing = 1.5.sp
      )

      Spacer(modifier = Modifier.height(12.dp))

      // WhatsApp 3D Contact Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberDarkBg.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
          .border(1.5.dp, NeonGreen.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
          .clickable {
            try {
              val num = config.whatsappNumber.replace("+", "").replace(" ", "")
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$num"))
              context.startActivity(intent)
            } catch (e: Exception) {
              Toast.makeText(context, "WhatsApp: ${config.whatsappNumber}", Toast.LENGTH_SHORT).show()
            }
          }
          .padding(12.dp)
          .testTag("whatsapp_contact_row"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(NeonGreenDark, CircleShape)
              .border(1.dp, NeonGreenBright, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Send, null, tint = NeonGreenBright, modifier = Modifier.size(16.dp))
          }
          Column {
            Text(text = "WhatsApp Support", fontSize = 11.sp, color = TextMuted)
            Text(
              text = config.whatsappNumber,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = TextWhite,
              fontFamily = FontFamily.Monospace
            )
          }
        }
        Text(text = "CHAT >", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NeonGreenBright)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Telegram 3D Contact Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberDarkBg.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
          .border(1.5.dp, BloodRedPrimary.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
          .clickable {
            try {
              val handle = config.telegramHandle.replace("@", "")
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$handle"))
              context.startActivity(intent)
            } catch (e: Exception) {
              Toast.makeText(context, "Telegram: ${config.telegramHandle}", Toast.LENGTH_SHORT).show()
            }
          }
          .padding(12.dp)
          .testTag("telegram_contact_row"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(BloodRedDark, CircleShape)
              .border(1.dp, BloodRedGlow, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Security, null, tint = BloodRedGlow, modifier = Modifier.size(16.dp))
          }
          Column {
            Text(text = "Telegram Channel", fontSize = 11.sp, color = TextMuted)
            Text(
              text = config.telegramHandle,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = TextWhite,
              fontFamily = FontFamily.Monospace
            )
          }
        }
        Text(text = "JOIN >", fontSize = 11.sp, fontWeight = FontWeight.Black, color = BloodRedGlow)
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Logout Button (3D Cyber Style)
    Cyber3DButton(
      text = "DISCONNECT RIG",
      onClick = onLogout,
      isGreen = false,
      icon = Icons.Default.Logout,
      height = 46.dp,
      fontSize = 12.sp,
      testTag = "logout_btn"
    )
  }
}
