package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloodRedDark
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onTimeout: () -> Unit,
  modifier: Modifier = Modifier
) {
  var introStep by remember { mutableStateOf(0) }

  // 5-second timer with dynamic status updates
  LaunchedEffect(Unit) {
    delay(1000)
    introStep = 1
    delay(1200)
    introStep = 2
    delay(1300)
    introStep = 3
    delay(1500)
    onTimeout()
  }

  val infiniteTransition = rememberInfiniteTransition(label = "splash_glow")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_alpha"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // 3D Glowing Cyber Crest Icon
      Box(
        modifier = Modifier
          .size(140.dp)
          .scale(pulseScale)
          .shadow(
            elevation = 30.dp,
            shape = CircleShape,
            ambientColor = BloodRedPrimary,
            spotColor = NeonGreen
          )
          .border(
            width = 3.dp,
            brush = Brush.sweepGradient(
              listOf(
                BloodRedPrimary,
                NeonGreen,
                BloodRedGlow,
                NeonGreenBright,
                BloodRedPrimary
              )
            ),
            shape = CircleShape
          )
          .background(
            brush = Brush.radialGradient(
              listOf(
                BloodRedDark.copy(alpha = 0.85f),
                CyberDarkBg.copy(alpha = 0.95f)
              )
            ),
            shape = CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.Shield,
          contentDescription = "Krishna Config Shield",
          tint = BloodRedGlow,
          modifier = Modifier.size(72.dp)
        )
        Icon(
          imageVector = Icons.Filled.Security,
          contentDescription = null,
          tint = NeonGreen.copy(alpha = glowAlpha),
          modifier = Modifier.size(38.dp)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      // 3D Title with Visceral Bloody Style, Dripping Crimson and Neon-Green Highlights
      com.example.ui.components.BloodyTitle(
        titleSize = 34.sp,
        subtitle = "V3.0 ULTIMATE SAFE SYSTEM",
        showDrips = true,
        dropHeight = 18.dp
      )

      Spacer(modifier = Modifier.height(30.dp))

      // Loading step indicator
      val statusText = when (introStep) {
        0 -> "INITIALIZING CRYPTO CORE..."
        1 -> "ESTABLISHING SECURE GATEWAY..."
        2 -> "SYNCING REMOTE CONFIG..."
        else -> "SAFE ZONE READY. ACCESSING..."
      }

      Text(
        text = statusText,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = TextMuted,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(14.dp))

      LinearProgressIndicator(
        modifier = Modifier
          .fillMaxWidth(0.7f)
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = NeonGreen,
        trackColor = BloodRedDark.copy(alpha = 0.4f)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Skip button
      TextButton(onClick = onTimeout) {
        Text(
          text = "SKIP INTRO >>",
          color = TextMuted.copy(alpha = 0.8f),
          fontSize = 12.sp,
          letterSpacing = 1.sp
        )
      }
    }
  }
}
