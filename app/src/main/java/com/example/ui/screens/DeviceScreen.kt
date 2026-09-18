package com.example.ui.screens

import android.os.Build
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun DeviceScreen(
  initialDeviceName: String,
  onProceed: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val defaultDevice = remember {
    if (initialDeviceName.isNotBlank()) initialDeviceName
    else "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}"
  }
  var deviceName by remember { mutableStateOf(defaultDevice) }
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 24.dp, vertical = 36.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "HARDWARE BINDING",
      fontSize = 26.sp,
      fontWeight = FontWeight.ExtraBold,
      fontFamily = FontFamily.Monospace,
      color = BloodRedPrimary,
      letterSpacing = 2.sp,
      textAlign = TextAlign.Center
    )

    Text(
      text = "SECURE DEVICE REGISTRATION",
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = NeonGreenBright,
      letterSpacing = 2.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
    )

    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = true,
      pulsateGlow = true
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 16.dp)
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .background(CyberDarkBg, CircleShape)
            .border(1.5.dp, NeonGreen, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = NeonGreen
          )
        }
        Column {
          Text(
            text = "TARGET HARDWARE",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
          )
          Text(
            text = "BIND TO THIS MOBILE RIG",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
          )
        }
      }

      Text(
        text = "ENTER DEVICE NAME",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = NeonGreenBright,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
      )

      OutlinedTextField(
        value = deviceName,
        onValueChange = { deviceName = it },
        placeholder = { Text("e.g. OnePlus 11R / ROG Phone 7", color = TextMuted) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TextWhite,
          unfocusedTextColor = TextWhite,
          focusedBorderColor = NeonGreen,
          unfocusedBorderColor = TextMuted.copy(alpha = 0.4f),
          focusedContainerColor = CyberDarkBg.copy(alpha = 0.4f),
          unfocusedContainerColor = CyberDarkBg.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("device_name_input")
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Device detected telemetry
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberDarkBg.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
          .border(1.dp, BloodRedPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
          .padding(12.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "• ARCHITECTURE: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
          )
          Text(
            text = "• OS VERSION: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
          )
          Text(
            text = "• KERNEL SECURITY: READY FOR SHIELDING",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = NeonGreen
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      GlowingButton(
        onClick = { onProceed(deviceName.trim()) },
        isGreen = true,
        testTag = "proceed_to_payment_btn"
      ) {
        Text(
          text = "CONFIRM & PROCEED TO ACTIVATION",
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
          imageVector = Icons.Default.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
