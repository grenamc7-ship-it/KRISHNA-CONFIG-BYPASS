package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloodRedDark
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.NeonGreenBright

/**
 * High-impact Cyberpunk Bloody Title for "KRISHNA CONFIG"
 * Featuring deep arterial blood gradients, glowing specular drips, neon green toxic edges,
 * animated pulsating visceral aura, and micro blood drop droplets.
 */
@Composable
fun BloodyTitle(
  modifier: Modifier = Modifier,
  titleSize: TextUnit = 32.sp,
  subtitleSize: TextUnit = 12.sp,
  subtitle: String? = null,
  showDrips: Boolean = true,
  dropHeight: Dp = 14.dp
) {
  val infiniteTransition = rememberInfiniteTransition(label = "bloody_anim")

  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "bloody_pulse"
  )

  val bloodGlowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.65f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "blood_glow"
  )

  val dripAnimationProgress by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "drip_flow"
  )

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bloody Text Layer with stacked 3D shadows and arterial blood styling
    Box(contentAlignment = Alignment.Center) {
      // Background Glow Layer (visceral blood red aura)
      Text(
        text = "KRISHNA CONFIG",
        fontSize = titleSize,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 4.sp,
        textAlign = TextAlign.Center,
        style = TextStyle(
          color = BloodRedPrimary.copy(alpha = bloodGlowAlpha * 0.7f),
          shadow = Shadow(
            color = BloodRedGlow,
            offset = Offset(0f, 0f),
            blurRadius = 38f * bloodGlowAlpha
          )
        )
      )

      // Foreground Bloody Core with multi-stop rich arterial blood & crimson gradient
      Text(
        text = "KRISHNA CONFIG",
        fontSize = titleSize,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 4.sp,
        textAlign = TextAlign.Center,
        style = TextStyle(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFFF5252), // Bright visceral blood crimson top
              Color(0xFFFF0033), // Pure blood red
              Color(0xFFB71C1C), // Deep arterial crimson
              Color(0xFF5A000E)  // Thick dried blood base
            )
          ),
          shadow = Shadow(
            color = Color(0xFF000000),
            offset = Offset(3f, 4f),
            blurRadius = 8f
          )
        )
      )
    }

    // Organic animated blood drips and splatter line immediately underneath the title
    if (showDrips) {
      Canvas(
        modifier = Modifier
          .fillMaxWidth(0.85f)
          .height(dropHeight)
          .padding(top = 2.dp)
      ) {
        val w = size.width
        val h = size.height

        // Sinuous arterial base vein
        val veinPath = Path().apply {
          moveTo(0f, 2f)
          cubicTo(w * 0.25f, 4f, w * 0.75f, 1f, w, 3f)
        }
        drawPath(
          path = veinPath,
          brush = Brush.horizontalGradient(
            listOf(
              Color.Transparent,
              BloodRedDark,
              BloodRedPrimary,
              BloodRedGlow,
              BloodRedPrimary,
              BloodRedDark,
              Color.Transparent
            )
          ),
          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
        )

        // Hanging, elongated dripping blood droplets positioned under key characters
        val dripRatios = listOf(0.12f, 0.28f, 0.44f, 0.62f, 0.78f, 0.90f)
        dripRatios.forEachIndexed { idx, ratio ->
          val dripX = w * ratio
          val maxLen = h * (0.5f + (idx % 3) * 0.22f)
          val currentLen = maxLen * dripAnimationProgress

          // Tapered drip line
          drawLine(
            brush = Brush.verticalGradient(
              colors = listOf(BloodRedPrimary, Color(0xFF8B0000)),
              startY = 2f,
              endY = currentLen
            ),
            start = Offset(dripX, 2f),
            end = Offset(dripX, currentLen),
            strokeWidth = if (idx % 2 == 0) 3.2f else 2.4f
          )

          // Blood droplet teardrop bead at tip
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(Color(0xFFFF3366), BloodRedPrimary, Color(0xFF4A000B)),
              center = Offset(dripX, currentLen),
              radius = 5.5f
            ),
            radius = if (idx % 2 == 0) 5.5f else 4.2f,
            center = Offset(dripX, currentLen)
          )

          // Specular white-hot highlight on the droplet tip
          drawCircle(
            color = Color(0xCCFFFFFF),
            radius = 1.6f,
            center = Offset(dripX - 1.2f, currentLen - 1.2f)
          )
        }
      }
    }

    if (subtitle != null) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
      ) {
        Text(
          text = "• ",
          fontSize = subtitleSize,
          fontWeight = FontWeight.Bold,
          color = BloodRedGlow
        )
        Text(
          text = subtitle,
          fontSize = subtitleSize,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = NeonGreenBright,
          letterSpacing = 2.sp
        )
        Text(
          text = " •",
          fontSize = subtitleSize,
          fontWeight = FontWeight.Bold,
          color = BloodRedGlow
        )
      }
    }
  }
}
