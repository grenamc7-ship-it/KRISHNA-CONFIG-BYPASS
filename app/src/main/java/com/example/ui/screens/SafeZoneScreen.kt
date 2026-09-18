package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.GlowingButton
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
  onUpdateUID: (String) -> Unit,
  onToggleProtection: (Boolean) -> Unit,
  onFixAntihack: () -> Unit,
  onRemoveBlacklist: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val scrollState = rememberScrollState()

  var uidInput by remember { mutableStateOf(userSession.uidConfig.ifEmpty { "5128934102" }) }
  var isUidVerified by remember { mutableStateOf(userSession.uidConfig.isNotEmpty()) }

  var isScanningAntihack by remember { mutableStateOf(false) }
  var antihackProgress by remember { mutableStateOf(0f) }

  var isPurgingBlacklist by remember { mutableStateOf(false) }
  var blacklistProgress by remember { mutableStateOf(0f) }

  val infiniteTransition = rememberInfiniteTransition(label = "safe_glow")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.97f,
    targetValue = 1.03f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "safe_pulse"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Safe Zone Banner
    Box(
      modifier = Modifier
        .scale(pulseScale)
        .background(
          color = CyberDarkBg.copy(alpha = 0.8f),
          shape = RoundedCornerShape(16.dp)
        )
        .border(2.dp, NeonGreen, RoundedCornerShape(16.dp))
        .padding(horizontal = 20.dp, vertical = 14.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = NeonGreenBright,
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = "SAFE ZONE ACTIVATED",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = NeonGreenBright,
            letterSpacing = 2.sp
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "NOW YOU ARE ENTERED YOUR SAFE ZONE",
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace,
          color = TextWhite,
          textAlign = TextAlign.Center
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 1. UID VERIFICATION PANEL
    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = true
    ) {
      Text(
        text = "• ENTER YOUR UID - XXXXXXXXXX",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = NeonGreenBright
      )
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            focusedContainerColor = CyberDarkBg.copy(alpha = 0.5f),
            unfocusedContainerColor = CyberDarkBg.copy(alpha = 0.3f)
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("uid_input_field")
        )

        // VERIFY BUTTON
        OutlinedButton(
          onClick = {
            if (uidInput.isNotBlank()) {
              isUidVerified = true
              onUpdateUID(uidInput.trim())
            }
          },
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isUidVerified) StatusSuccess else NeonGreen
          ),
          border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isUidVerified) StatusSuccess else NeonGreen
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("verify_uid_btn")
        ) {
          if (isUidVerified) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("VERIFIED", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          } else {
            Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("VERIFY", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. ENABLE PROTECTION
    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = userSession.isProtectionEnabled
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .background(
                if (userSession.isProtectionEnabled) NeonGreenDark else BloodRedDark,
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (userSession.isProtectionEnabled) Icons.Default.Shield else Icons.Default.LockOpen,
              contentDescription = null,
              tint = if (userSession.isProtectionEnabled) NeonGreenBright else BloodRedGlow,
              modifier = Modifier.size(20.dp)
            )
          }
          Column {
            Text(
              text = "• ENABLE PROTECTION",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = TextWhite
            )
            Text(
              text = if (userSession.isProtectionEnabled) "STATUS: SHIELDED & ACTIVE" else "STATUS: STANDBY",
              fontSize = 11.sp,
              color = if (userSession.isProtectionEnabled) NeonGreenBright else TextMuted
            )
          }
        }

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
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. FIX SYSTEM ANTIHACK
    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = userSession.isAntihackFixed
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "• FIX SYSTEM ANTIHACK",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextWhite
          )
          Text(
            text = if (userSession.isAntihackFixed) "STATUS: ANTIHACK INTEGRITY 100%" else "CORE INTEGRITY: SCAN REQUIRED",
            fontSize = 11.sp,
            color = if (userSession.isAntihackFixed) NeonGreenBright else TextMuted
          )
        }

        OutlinedButton(
          onClick = {
            if (!isScanningAntihack) {
              isScanningAntihack = true
              antihackProgress = 0f
              coroutineScope.launch {
                for (i in 1..10) {
                  delay(150)
                  antihackProgress = i / 10f
                }
                isScanningAntihack = false
                onFixAntihack()
              }
            }
          },
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (userSession.isAntihackFixed) StatusSuccess else BloodRedGlow
          ),
          border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (userSession.isAntihackFixed) StatusSuccess else BloodRedPrimary
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("fix_antihack_btn")
        ) {
          if (isScanningAntihack) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = NeonGreen)
          } else if (userSession.isAntihackFixed) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("FIXED", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          } else {
            Icon(Icons.Default.Build, null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("FIX NOW", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }

      AnimatedVisibility(visible = isScanningAntihack) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          LinearProgressIndicator(
            progress = { antihackProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp)),
            color = NeonGreen,
            trackColor = BloodRedDark.copy(alpha = 0.4f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 4. REMOVE BLACKLIST
    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = userSession.isBlacklistRemoved
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "• REMOVE BLACKLIST",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextWhite
          )
          Text(
            text = if (userSession.isBlacklistRemoved) "BLACKLIST: 0 ENTRIES DETECTED" else "CLEAR ALL BLACKLIST LOGS",
            fontSize = 11.sp,
            color = if (userSession.isBlacklistRemoved) NeonGreenBright else TextMuted
          )
        }

        OutlinedButton(
          onClick = {
            if (!isPurgingBlacklist) {
              isPurgingBlacklist = true
              blacklistProgress = 0f
              coroutineScope.launch {
                for (i in 1..10) {
                  delay(150)
                  blacklistProgress = i / 10f
                }
                isPurgingBlacklist = false
                onRemoveBlacklist()
              }
            }
          },
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (userSession.isBlacklistRemoved) StatusSuccess else BloodRedGlow
          ),
          border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (userSession.isBlacklistRemoved) StatusSuccess else BloodRedPrimary
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("remove_blacklist_btn")
        ) {
          if (isPurgingBlacklist) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = NeonGreen)
          } else if (userSession.isBlacklistRemoved) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("CLEARED", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          } else {
            Icon(Icons.Default.CleaningServices, null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("PURGE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }

      AnimatedVisibility(visible = isPurgingBlacklist) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          LinearProgressIndicator(
            progress = { blacklistProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp)),
            color = BloodRedPrimary,
            trackColor = CyberDarkBg
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // DETAILS SECTION
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

      // WhatsApp
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberDarkBg.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
          .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
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
          Icon(Icons.Default.Send, null, tint = NeonGreenBright)
          Column {
            Text(text = "WhatsApp Support", fontSize = 11.sp, color = TextMuted)
            Text(
              text = config.whatsappNumber,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = TextWhite,
              fontFamily = FontFamily.Monospace
            )
          }
        }
        Text(text = "CHAT >", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreenBright)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Telegram
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberDarkBg.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
          .border(1.dp, BloodRedPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
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
          Icon(Icons.Default.Security, null, tint = BloodRedGlow)
          Column {
            Text(text = "Telegram Channel", fontSize = 11.sp, color = TextMuted)
            Text(
              text = config.telegramHandle,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = TextWhite,
              fontFamily = FontFamily.Monospace
            )
          }
        }
        Text(text = "JOIN >", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BloodRedGlow)
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Logout
    OutlinedButton(
      onClick = onLogout,
      colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
      border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f)),
      modifier = Modifier.fillMaxWidth().testTag("logout_btn")
    ) {
      Icon(Icons.Default.Logout, null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.size(8.dp))
      Text("DISCONNECT RIG", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
  }
}
