package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class CyberDrip(
  val xRatio: Float,
  val speed: Float,
  val length: Float,
  val isGreen: Boolean
)

@Composable
fun BloodDripOverlay(
  modifier: Modifier = Modifier
) {
  val drips = remember {
    List(18) {
      CyberDrip(
        xRatio = Random.nextFloat(),
        speed = Random.nextFloat() * 0.5f + 0.5f,
        length = Random.nextFloat() * 90f + 40f,
        isGreen = it % 4 == 0 // mostly red, occasional neon green
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "drip_anim")
  val progress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 6500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "drip_progress"
  )

  Canvas(modifier = modifier.fillMaxSize()) {
    val width = size.width
    val height = size.height

    drips.forEach { drip ->
      val currentY = ((progress * drip.speed * height * 1.3f) % (height + 150f)) - 100f
      val startX = drip.xRatio * width

      val colorStart = if (drip.isGreen) Color(0x9900FF41) else Color(0xAAFF0033)
      val colorEnd = if (drip.isGreen) Color(0x0000FF41) else Color(0x008B0000)

      drawLine(
        brush = Brush.verticalGradient(
          colors = listOf(colorStart, colorEnd),
          startY = currentY,
          endY = currentY + drip.length
        ),
        start = Offset(startX, currentY),
        end = Offset(startX, currentY + drip.length),
        strokeWidth = if (drip.isGreen) 2f else 3.5f
      )

      // Little glowing bead at tip of drip
      drawCircle(
        color = if (drip.isGreen) Color(0xCC39FF14) else Color(0xEEFF1744),
        radius = if (drip.isGreen) 2.5f else 3.5f,
        center = Offset(startX, currentY + drip.length)
      )
    }
  }
}
