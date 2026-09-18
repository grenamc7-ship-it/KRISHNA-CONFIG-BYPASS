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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.components.BloodyTitle
import com.example.ui.components.Cyber3DButton
import com.example.ui.components.CyberGlassCard
import com.example.ui.theme.BloodRedDark
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.NeonGreenDark
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
      .padding(horizontal = 18.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    BloodyTitle(
      titleSize = 30.sp,
      subtitle = "ACTIVATE PROTECTION • 3D SECURE CHECKOUT",
      showDrips = true,
      dropHeight = 16.dp,
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(NeonGreenDark, CircleShape)
                .border(1.dp, NeonGreenBright, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Key, contentDescription = null, tint = NeonGreenBright, modifier = Modifier.size(18.dp))
            }
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
            .padding(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .scale(pulseScale)
              .background(
                brush = Brush.radialGradient(
                  listOf(Color(0xFFFFB300), Color(0xFF664400), Color.Black)
                ),
                shape = CircleShape
              )
              .border(2.dp, StatusPending, CircleShape)
              .shadow(12.dp, CircleShape, spotColor = StatusPending),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.HourglassTop,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(38.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "WAITING FOR ADMIN APPROVAL...",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = StatusPending,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Your device specifications and payment proof have been dispatched to Telegram Admin & Web Console. Safe Zone will unlock automatically the moment Admin approves.",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          LinearProgressIndicator(
            modifier = Modifier
              .fillMaxWidth(0.85f)
              .height(5.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = NeonGreenBright,
            trackColor = BloodRedDark.copy(alpha = 0.5f)
          )
        }
      }
    } else if (userSession.verificationStatus == VerificationStatus.REJECTED && !isRetryingAfterDecline) {
      // REJECTED STATE
      CyberGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isGreenAccent = false
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(76.dp)
              .background(
                brush = Brush.radialGradient(
                  listOf(BloodRedGlow, BloodRedDark, Color.Black)
                ),
                shape = CircleShape
              )
              .border(2.dp, StatusError, CircleShape)
              .shadow(10.dp, CircleShape, spotColor = StatusError),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Cancel,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(40.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "ACCESS NOT APPROVED",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = StatusError,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Your previous activation submission was declined. Please verify your payment proof screenshot and correct UPI reference number before resubmitting.",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          Cyber3DButton(
            text = "RESUBMIT PAYMENT PROOF",
            onClick = { isRetryingAfterDecline = true },
            isGreen = false,
            icon = Icons.Default.Refresh,
            height = 46.dp,
            fontSize = 12.sp
          )
        }
      }
    } else {
      // PAYMENT FORM (Frosted 3D Glass)
      CyberGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isGreenAccent = true
      ) {
        // Price banner with 3D Holographic Badge
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              brush = Brush.horizontalGradient(
                listOf(Color(0x35003311), Color(0x50001A08), Color(0x35003311))
              ),
              shape = RoundedCornerShape(14.dp)
            )
            .border(1.5.dp, NeonGreen.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "ACTIVATION LICENSE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = NeonGreenBright
            )
            Text(
              text = "LIFETIME FULL ACCESS",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = TextWhite
            )
          }
          Box(
            modifier = Modifier
              .background(
                brush = Brush.verticalGradient(
                  listOf(Color(0xFF00FF66), Color(0xFF006622))
                ),
                shape = RoundedCornerShape(10.dp)
              )
              .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
              .padding(horizontal = 14.dp, vertical = 6.dp)
              .shadow(8.dp, RoundedCornerShape(10.dp), spotColor = NeonGreenBright)
          ) {
            Text(
              text = "₹${config.paymentAmount}",
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = Color.Black
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3D Extruded QR Code Container
        Box(
          modifier = Modifier
            .size(220.dp)
            .align(Alignment.CenterHorizontally)
        ) {
          // 3D Extrusion base
          Box(
            modifier = Modifier
              .matchParentSize()
              .offset(y = 5.dp)
              .background(Color(0x80000000), RoundedCornerShape(16.dp))
          )

          Box(
            modifier = Modifier
              .matchParentSize()
              .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = NeonGreenBright.copy(alpha = 0.5f),
                ambientColor = NeonGreen.copy(alpha = 0.35f)
              )
              .background(Color.White, RoundedCornerShape(16.dp))
              .border(2.5.dp, NeonGreen, RoundedCornerShape(16.dp))
              .padding(12.dp)
          ) {
            AsyncImage(
              model = qrCodeApiUrl,
              contentDescription = "UPI QR Code",
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxSize()
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // UPI ID copy row (3D Beveled Box)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(CyberDarkBg.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .border(1.5.dp, BloodRedPrimary.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(text = "UPI ID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text(
              text = config.upiId,
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = TextWhite
            )
          }
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                tint = NeonGreenBright
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

        // 3D Screenshot Selector Button
        Cyber3DButton(
          text = if (selectedImageUri != null) "SCREENSHOT ATTACHED ✓" else "UPLOAD PAYMENT SCREENSHOT",
          onClick = { imagePickerLauncher.launch("image/*") },
          isGreen = selectedImageUri != null,
          icon = Icons.Default.UploadFile,
          height = 46.dp,
          fontSize = 11.sp,
          testTag = "upload_screenshot_btn"
        )

        // Thumbnail preview if chosen
        if (selectedImageUri != null) {
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(2.dp, NeonGreen, RoundedCornerShape(12.dp))
              .shadow(10.dp, RoundedCornerShape(12.dp), spotColor = NeonGreenBright)
          ) {
            AsyncImage(
              model = selectedImageUri,
              contentDescription = "Attached payment screenshot",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
            focusedContainerColor = CyberDarkBg.copy(alpha = 0.45f),
            unfocusedContainerColor = CyberDarkBg.copy(alpha = 0.35f)
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("upi_ref_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Proof 3D Button
        Cyber3DButton(
          text = "SUBMIT PAYMENT PROOF",
          onClick = {
            isRetryingAfterDecline = false
            onSubmitPayment(selectedImageUri, upiRef.trim())
          },
          isGreen = false,
          icon = Icons.Default.AccountBalanceWallet,
          isLoading = isLoading,
          enabled = !isLoading,
          height = 52.dp,
          fontSize = 13.sp,
          testTag = "submit_payment_btn"
        )
      }
    }
  }
}
