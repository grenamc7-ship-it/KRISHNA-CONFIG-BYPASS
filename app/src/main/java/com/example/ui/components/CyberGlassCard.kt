package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberCardGlass
import com.example.ui.theme.NeonGreen

@Composable
fun CyberGlassCard(
  modifier: Modifier = Modifier,
  isGreenAccent: Boolean = false,
  cornerRadius: Dp = 20.dp,
  pulsateGlow: Boolean = false,
  content: @Composable ColumnScope.() -> Unit
) {
  val accentColor = if (isGreenAccent) NeonGreen else BloodRedPrimary
  val secondaryColor = if (isGreenAccent) BloodRedPrimary else NeonGreen

  val infiniteTransition = rememberInfiniteTransition(label = "border_pulse")
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = if (pulsateGlow) 1.0f else 0.7f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_alpha"
  )

  Card(
    shape = RoundedCornerShape(cornerRadius),
    colors = CardDefaults.cardColors(
      containerColor = CyberCardGlass
    ),
    border = BorderStroke(
      width = 1.5.dp,
      brush = Brush.linearGradient(
        colors = listOf(
          accentColor.copy(alpha = glowAlpha),
          secondaryColor.copy(alpha = 0.25f),
          accentColor.copy(alpha = glowAlpha * 0.8f)
        )
      )
    ),
    modifier = modifier
      .shadow(
        elevation = 12.dp,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = accentColor.copy(alpha = 0.4f),
        spotColor = accentColor.copy(alpha = 0.6f)
      )
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0x332A040D),
              Color(0x22050B05),
              Color(0x44140207)
            )
          )
        )
        .drawBehind {
          // Cyber bracket ticks on corners
          val stroke = 3.dp.toPx()
          val tickLen = 16.dp.toPx()
          // Top-left cyber tick
          drawLine(
            color = accentColor,
            start = Offset(0f, tickLen),
            end = Offset(0f, 0f),
            strokeWidth = stroke
          )
          drawLine(
            color = accentColor,
            start = Offset(0f, 0f),
            end = Offset(tickLen, 0f),
            strokeWidth = stroke
          )

          // Bottom-right cyber tick
          val w = size.width
          val h = size.height
          drawLine(
            color = secondaryColor,
            start = Offset(w - tickLen, h),
            end = Offset(w, h),
            strokeWidth = stroke
          )
          drawLine(
            color = secondaryColor,
            start = Offset(w, h - tickLen),
            end = Offset(w, h),
            strokeWidth = stroke
          )
        }
        .padding(20.dp)
    ) {
      Column(content = content)
    }
  }
}
