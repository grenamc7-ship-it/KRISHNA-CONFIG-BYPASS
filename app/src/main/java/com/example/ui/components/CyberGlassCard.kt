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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberCardGlass
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright

/**
 * Enhanced Ultra-3D Holographic Cyber Glass Card
 * - True 3D depth with dual-layered extrusion shadows
 * - Beveled metallic specular cyber edges
 * - Interactive subtle 3D tilt & dynamic holographic sheen sweep
 * - Cybernetic 3D corner bracket extrusions
 */
@Composable
fun CyberGlassCard(
  modifier: Modifier = Modifier,
  isGreenAccent: Boolean = false,
  cornerRadius: Dp = 22.dp,
  pulsateGlow: Boolean = false,
  elevation3D: Dp = 20.dp,
  content: @Composable ColumnScope.() -> Unit
) {
  val primaryAccent = if (isGreenAccent) NeonGreen else BloodRedPrimary
  val brightAccent = if (isGreenAccent) NeonGreenBright else BloodRedGlow
  val secondaryAccent = if (isGreenAccent) BloodRedPrimary else NeonGreen

  val infiniteTransition = rememberInfiniteTransition(label = "3d_card_anim")

  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.55f,
    targetValue = if (pulsateGlow) 1.0f else 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "3d_glow_alpha"
  )

  // Floating 3D subtle breathing offset
  val floatZ by infiniteTransition.animateFloat(
    initialValue = -1.5f,
    targetValue = 1.5f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "3d_float_z"
  )

  // Sweeping holographic sheen position
  val sheenProgress by infiniteTransition.animateFloat(
    initialValue = -0.5f,
    targetValue = 1.5f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "3d_sheen_progress"
  )

  Box(
    modifier = modifier
      .graphicsLayer {
        translationY = floatZ
        cameraDistance = 16f * density
        rotationX = 0.6f
      }
  ) {
    // 3D Back Extrusion Shadow Layer (Gives physical extruded base)
    Box(
      modifier = Modifier
        .matchParentSize()
        .offset(x = 0.dp, y = 6.dp)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.Transparent,
              if (isGreenAccent) Color(0x60002A0B) else Color(0x603D000B),
              if (isGreenAccent) Color(0x90001505) else Color(0x90220006)
            )
          ),
          shape = RoundedCornerShape(cornerRadius)
        )
    )

    // Primary Floating Card with Dual 3D Spotlight Shadows
    Card(
      shape = RoundedCornerShape(cornerRadius),
      colors = CardDefaults.cardColors(
        containerColor = CyberCardGlass
      ),
      border = BorderStroke(
        width = 1.8.dp,
        brush = Brush.linearGradient(
          colors = listOf(
            brightAccent.copy(alpha = glowAlpha),
            secondaryAccent.copy(alpha = 0.45f),
            primaryAccent.copy(alpha = glowAlpha * 0.95f),
            Color(0x30FFFFFF)
          )
        )
      ),
      modifier = Modifier
        .fillMaxWidth()
        .shadow(
          elevation = elevation3D,
          shape = RoundedCornerShape(cornerRadius),
          ambientColor = primaryAccent.copy(alpha = 0.45f),
          spotColor = brightAccent.copy(alpha = 0.65f)
        )
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          // Multi-layer 3D holographic specular interior
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0x40FFFFFF), // Top 3D beveled light catch
                Color(0x18300810),
                Color(0x22050E06),
                Color(0x35190008),
                Color(0x45000000)  // Deep 3D bottom bevel base
              )
            )
          )
          .drawBehind {
            val w = size.width
            val h = size.height
            val stroke = 3.dp.toPx()
            val tickLen = 16.dp.toPx()

            // Dynamic Sweeping Holographic Sheen Line
            val sheenX = w * sheenProgress
            drawLine(
              brush = Brush.horizontalGradient(
                colors = listOf(
                  Color.Transparent,
                  brightAccent.copy(alpha = 0.25f),
                  Color.White.copy(alpha = 0.45f),
                  brightAccent.copy(alpha = 0.25f),
                  Color.Transparent
                ),
                startX = sheenX - 60.dp.toPx(),
                endX = sheenX + 60.dp.toPx()
              ),
              start = Offset(sheenX - 60.dp.toPx(), 0f),
              end = Offset(sheenX + 60.dp.toPx(), h),
              strokeWidth = 2.5.dp.toPx()
            )

            // Top Specular 3D Highlight Bevel Edge
            drawLine(
              brush = Brush.horizontalGradient(
                colors = listOf(
                  Color.Transparent,
                  Color.White.copy(alpha = 0.6f),
                  brightAccent.copy(alpha = 0.8f),
                  Color.Transparent
                )
              ),
              start = Offset(20.dp.toPx(), 1.dp.toPx()),
              end = Offset(w - 20.dp.toPx(), 1.dp.toPx()),
              strokeWidth = 1.5.dp.toPx()
            )

            // 3D Corner Bracket Chiseled Accents (Top-Left)
            drawLine(
              color = brightAccent.copy(alpha = glowAlpha),
              start = Offset(0f, tickLen),
              end = Offset(0f, 0f),
              strokeWidth = stroke
            )
            drawLine(
              color = brightAccent.copy(alpha = glowAlpha),
              start = Offset(0f, 0f),
              end = Offset(tickLen, 0f),
              strokeWidth = stroke
            )

            // 3D Corner Bracket Chiseled Accents (Top-Right)
            drawLine(
              color = primaryAccent.copy(alpha = glowAlpha * 0.7f),
              start = Offset(w - tickLen, 0f),
              end = Offset(w, 0f),
              strokeWidth = stroke
            )
            drawLine(
              color = primaryAccent.copy(alpha = glowAlpha * 0.7f),
              start = Offset(w, 0f),
              end = Offset(w, tickLen),
              strokeWidth = stroke
            )

            // 3D Corner Bracket Chiseled Accents (Bottom-Right)
            drawLine(
              color = brightAccent.copy(alpha = glowAlpha),
              start = Offset(w, h - tickLen),
              end = Offset(w, h),
              strokeWidth = stroke
            )
            drawLine(
              color = brightAccent.copy(alpha = glowAlpha),
              start = Offset(w - tickLen, h),
              end = Offset(w, h),
              strokeWidth = stroke
            )

            // 3D Corner Bracket Chiseled Accents (Bottom-Left)
            drawLine(
              color = primaryAccent.copy(alpha = glowAlpha * 0.7f),
              start = Offset(0f, h - tickLen),
              end = Offset(0f, h),
              strokeWidth = stroke
            )
            drawLine(
              color = primaryAccent.copy(alpha = glowAlpha * 0.7f),
              start = Offset(0f, h),
              end = Offset(tickLen, h),
              strokeWidth = stroke
            )
          }
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          content = content
        )
      }
    }
  }
}
