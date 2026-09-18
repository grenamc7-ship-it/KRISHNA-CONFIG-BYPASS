package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.RemoteAppConfig
import com.example.data.UserSession
import com.example.data.VerificationStatus
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.theme.BloodRedDark
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusPending
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun PaymentScreen(
  userSession: UserSession,
  config: RemoteAppConfig,
  onSubmitPayment: (Uri?, String) -> Unit,
  isLoading: Boolean,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var upiRef by remember { mutableStateOf("") }
  var isRetryingAfterDecline by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  // Image Picker Launcher
  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    selectedImageUri = uri
  }

  // Generate dynamic UPI QR image URL
  val upiUrl = "upi://pay?pa=${config.upiId}&pn=${Uri.encode(config.upiName)}&am=${config.paymentAmount}&cu=INR"
  val qrCodeApiUrl = if (config.qrImageUrl.isNotBlank()) {
    config.qrImageUrl
  } else {
    "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + Uri.encode(upiUrl)
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    com.example.ui.components.BloodyTitle(
      titleSize = 28.sp,
      subtitle = "ACTIVATE PROTECTION • PAY VIA UPI",
      showDrips = true,
      dropHeight = 14.dp,
      modifier = Modifier.padding(bottom = 18.dp)
    )

    // UNIQUE ACTIVATION KEY CARD (Every user gets a unique generated key)
    if (userSession.activationKey.isNotBlank()) {
      CyberGlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 14.dp),
        isGreenAccent = true
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Key, contentDescription = null, tint = NeonGreenBright, modifier = Modifier.size(20.dp))
            Column {
              Text(
                text = "DEVICE ACTIVATION KEY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
              )
              Text(
                text = userSession.activationKey,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = NeonGreenBright
              )
            }
          }
          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("Activation Key", userSession.activationKey)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "Activation Key Copied!", Toast.LENGTH_SHORT).show()
            }
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Key", tint = TextWhite)
          }
        }
      }
    }

    // PENDING WAITING STATE (Awaiting ONLY Telegram Bot admin approval)
    if (userSession.verificationStatus == VerificationStatus.PENDING && !isRetryingAfterDecline) {
      CyberGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isGreenAccent = false,
        pulsateGlow = true
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .scale(pulseScale)
              .background(CyberDarkBg.copy(alpha = 0.5f), CircleShape)
              .border(2.dp, StatusPending, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.HourglassTop,
              contentDescription = null,
              tint = StatusPending,
              modifier = Modifier.size(40.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "WAITING FOR ADMIN APPROVAL...",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = StatusPending,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Your device specifications and payment proof have been sent to Telegram Admin. As soon as the Admin approves on Telegram Bot, Safe Zone will unlock automatically in real-time.",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          LinearProgressIndicator(
            modifier = Modifier
              .fillMaxWidth(0.8f)
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp)),
            color = NeonGreen,
            trackColor = BloodRedDark.copy(alpha = 0.5f)
          )
        }
      }
      return
    }

    // REJECTED / DECLINED STATE NOTICE
    if (userSession.verificationStatus == VerificationStatus.REJECTED) {
      CyberGlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        isGreenAccent = false,
        pulsateGlow = true
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.Warning, null, tint = StatusError, modifier = Modifier.size(26.dp))
            Text(
              text = "STATUS: DECLINED BY ADMIN",
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = StatusError
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Your payment submission was declined by Admin on Telegram Bot. Please verify your UPI UTR reference or attach a clearer payment slip.",
            fontSize = 12.sp,
            color = TextWhite,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedButton(
            onClick = { isRetryingAfterDecline = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text("RE-SUBMIT PAYMENT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }
    }

    // PAYMENT FORM CARD (Glassy aesthetic)
    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = false,
      pulsateGlow = true
    ) {
      // Amount Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberDarkBg.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
          .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
          .padding(12.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "ACTIVATION FEE",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted,
            letterSpacing = 1.sp
          )
          Text(
            text = "₹${config.paymentAmount}",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = NeonGreenBright
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // QR Code Display
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(210.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(3.dp, BloodRedPrimary, RoundedCornerShape(16.dp))
            .padding(10.dp),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = qrCodeApiUrl,
            contentDescription = "UPI QR Code",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // UPI ID copy row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberDarkBg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
          .border(1.dp, BloodRedPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
          .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(text = "UPI ID", fontSize = 10.sp, color = TextMuted)
          Text(
            text = config.upiId,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
          )
        }
        Row {
          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("UPI ID", config.upiId)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "UPI ID copied!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("copy_upi_btn")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy UPI ID",
              tint = NeonGreen
            )
          }

          IconButton(
            onClick = {
              try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUrl))
                context.startActivity(intent)
              } catch (e: Exception) {
                Toast.makeText(context, "No UPI App found", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.testTag("open_upi_app_btn")
          ) {
            Icon(
              imageVector = Icons.Default.OpenInNew,
              contentDescription = "Open UPI App",
              tint = BloodRedGlow
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Screenshot Selector
      OutlinedButton(
        onClick = { imagePickerLauncher.launch("image/*") },
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("upload_screenshot_btn")
      ) {
        Icon(Icons.Default.UploadFile, contentDescription = null)
        Spacer(modifier = Modifier.size(8.dp))
        Text(
          text = if (selectedImageUri != null) "SCREENSHOT ATTACHED ✓" else "UPLOAD PAYMENT SCREENSHOT",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }

      // Thumbnail preview if chosen
      if (selectedImageUri != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
        ) {
          AsyncImage(
            model = selectedImageUri,
            contentDescription = "Attached payment screenshot",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // UPI Reference Number Input
      OutlinedTextField(
        value = upiRef,
        onValueChange = { upiRef = it },
        label = { Text("UPI Ref / UTR No.", color = TextMuted) },
        placeholder = { Text("e.g. 423589124012", color = TextMuted.copy(alpha = 0.5f)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TextWhite,
          unfocusedTextColor = TextWhite,
          focusedBorderColor = NeonGreen,
          unfocusedBorderColor = TextMuted.copy(alpha = 0.4f),
          focusedContainerColor = CyberDarkBg.copy(alpha = 0.35f),
          unfocusedContainerColor = CyberDarkBg.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("upi_ref_input")
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Submit Proof Button
      GlowingButton(
        onClick = {
          isRetryingAfterDecline = false
          onSubmitPayment(selectedImageUri, upiRef.trim())
        },
        isGreen = false,
        enabled = !isLoading,
        testTag = "submit_payment_btn"
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = TextWhite
          )
        } else {
          Icon(Icons.Default.AccountBalanceWallet, null)
          Spacer(modifier = Modifier.size(8.dp))
          Text(
            text = "SUBMIT PAYMENT PROOF",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
        }
      }
    }
  }
}
