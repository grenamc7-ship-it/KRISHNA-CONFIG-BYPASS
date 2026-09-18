package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloodRedDark
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.NeonGreenDark
import com.example.ui.theme.TextWhite

/**
 * 3D Beveled Cyber Tactile Button
 * Features physical tactile press sinking (3D push effect), extruded bottom base bevel,
 * dynamic animated neon/crimson specular sheen, and glowing laser borders.
 */
@Composable
fun Cyber3DButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  isGreen: Boolean = true,
  isLoading: Boolean = false,
  enabled: Boolean = true,
  height: Dp = 50.dp,
  fontSize: TextUnit = 13.sp,
  testTag: String = ""
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val primaryColor = if (isGreen) NeonGreen else BloodRedPrimary
  val brightGlow = if (isGreen) NeonGreenBright else BloodRedGlow
  val darkBase = if (isGreen) NeonGreenDark else BloodRedDark

  val infiniteTransition = rememberInfiniteTransition(label = "btn_sheen")
  val sheenProgress by infiniteTransition.animateFloat(
    initialValue = -0.3f,
    targetValue = 1.3f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "btn_sheen_x"
  )

  val pressOffsetY = if (isPressed) 3.5.dp else 0.dp
  val elevationDp = if (isPressed) 2.dp else 8.dp

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(height + 4.dp)
  ) {
    // 3D Bottom Extrusion Shadow Base
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(height)
        .offset(y = 4.dp)
        .background(
          brush = Brush.verticalGradient(
            listOf(darkBase.copy(alpha = 0.8f), Color.Black)
          ),
          shape = RoundedCornerShape(14.dp)
        )
    )

    // Interactive Floating Button Face (sinks down when pressed)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(height)
        .offset(y = pressOffsetY)
        .shadow(
          elevation = elevationDp,
          shape = RoundedCornerShape(14.dp),
          spotColor = brightGlow,
          ambientColor = primaryColor
        )
        .background(
          brush = Brush.verticalGradient(
            colors = if (isGreen) listOf(
              Color(0xFF00FF66), // Bright neon green top
              Color(0xFF00B33C),
              Color(0xFF00591F)  // Deep cyber base
            ) else listOf(
              Color(0xFFFF3355), // Crimson blood top
              Color(0xFFCC0029),
              Color(0xFF550011)  // Coagulated dark base
            )
          ),
          shape = RoundedCornerShape(14.dp)
        )
        .drawBehind {
          val w = size.width
          val h = size.height

          // Specular Top Bevel Edge
          drawLine(
            color = Color.White.copy(alpha = 0.55f),
            start = Offset(12.dp.toPx(), 1.dp.toPx()),
            end = Offset(w - 12.dp.toPx(), 1.dp.toPx()),
            strokeWidth = 2.dp.toPx()
          )

          // Sweeping Laser Light Sheen
          val scanX = w * sheenProgress
          drawLine(
            brush = Brush.horizontalGradient(
              colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.4f),
                Color.Transparent
              ),
              startX = scanX - 40.dp.toPx(),
              endX = scanX + 40.dp.toPx()
            ),
            start = Offset(scanX - 40.dp.toPx(), 0f),
            end = Offset(scanX + 40.dp.toPx(), h),
            strokeWidth = 3.dp.toPx()
          )
        }
        .clickable(
          enabled = enabled && !isLoading,
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick
        )
        .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
      contentAlignment = Alignment.Center
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = Color.White,
          strokeWidth = 2.5.dp
        )
      } else {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        ) {
          if (icon != null) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
          }
          Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.8.sp,
            color = Color.White
          )
        }
      }
    }
  }
}
