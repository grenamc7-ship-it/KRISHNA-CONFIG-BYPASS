package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BloodRedDark
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.NeonGreenDark
import com.example.ui.theme.TextWhite

@Composable
fun GlowingButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isGreen: Boolean = true,
  enabled: Boolean = true,
  testTag: String = "glowing_button",
  content: @Composable RowScope.() -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "btn_glow")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "btn_pulse"
  )

  val primaryColor = if (isGreen) NeonGreen else BloodRedPrimary
  val darkBase = if (isGreen) NeonGreenDark else BloodRedDark
  val glowColor = if (isGreen) NeonGreenBright else BloodRedGlow
  val textColor = if (isGreen) CyberDarkBg else TextWhite

  Button(
    onClick = onClick,
    enabled = enabled,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = if (isGreen) primaryColor else darkBase,
      contentColor = textColor,
      disabledContainerColor = Color(0x33444444),
      disabledContentColor = Color(0x66888888)
    ),
    border = BorderStroke(
      width = 1.5.dp,
      brush = Brush.horizontalGradient(
        colors = listOf(
          glowColor.copy(alpha = pulseAlpha),
          primaryColor,
          glowColor.copy(alpha = pulseAlpha)
        )
      )
    ),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = 50.dp)
      .shadow(
        elevation = if (enabled) 10.dp else 0.dp,
        shape = RoundedCornerShape(12.dp),
        ambientColor = primaryColor.copy(alpha = 0.5f),
        spotColor = glowColor.copy(alpha = 0.7f)
      )
      .testTag(testTag),
    content = content
  )
}
