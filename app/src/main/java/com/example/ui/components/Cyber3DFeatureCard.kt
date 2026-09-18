package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloodRedDark
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.NeonGreenDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * 3D Beveled Feature Module Card
 * Gives every individual toggle/feature in the Safe Zone physical depth,
 * holographic badges, 3D extruded icons, and animated status glows.
 */
@Composable
fun Cyber3DFeatureCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  isActive: Boolean,
  modifier: Modifier = Modifier,
  actionContent: @Composable () -> Unit
) {
  val accentColor = if (isActive) NeonGreen else BloodRedPrimary
  val brightGlow = if (isActive) NeonGreenBright else BloodRedGlow
  val darkBg = if (isActive) NeonGreenDark else BloodRedDark

  val infiniteTransition = rememberInfiniteTransition(label = "feat_pulse")
  val pulseGlow by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "feat_glow"
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    // 3D Shadow extrusion behind card
    Box(
      modifier = Modifier
        .matchParentSize()
        .offset(y = 4.dp)
        .background(
          brush = Brush.verticalGradient(
            listOf(
              Color.Transparent,
              if (isActive) Color(0x50003311) else Color(0x50330008)
            )
          ),
          shape = RoundedCornerShape(18.dp)
        )
    )

    // Primary 3D Beveled Card Container
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(
          elevation = 10.dp,
          shape = RoundedCornerShape(18.dp),
          ambientColor = accentColor.copy(alpha = 0.35f),
          spotColor = brightGlow.copy(alpha = 0.5f)
        )
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0x35FFFFFF), // Top specular reflection
              if (isActive) Color(0x2A00260D) else Color(0x2A2A000A),
              CyberDarkBg.copy(alpha = 0.55f),
              Color(0x40000000)
            )
          ),
          shape = RoundedCornerShape(18.dp)
        )
        .border(
          width = 1.5.dp,
          brush = Brush.linearGradient(
            colors = listOf(
              brightGlow.copy(alpha = pulseGlow),
              accentColor.copy(alpha = 0.4f),
              Color(0x25FFFFFF)
            )
          ),
          shape = RoundedCornerShape(18.dp)
        )
        .drawBehind {
          // Specular top highlight bevel
          drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(16.dp.toPx(), 1.dp.toPx()),
            end = Offset(size.width - 16.dp.toPx(), 1.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
          )
        }
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          // 3D Sphere Hologram Icon Box
          Box(
            modifier = Modifier
              .size(44.dp)
              .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                spotColor = brightGlow
              )
              .background(
                brush = Brush.radialGradient(
                  colors = listOf(
                    if (isActive) Color(0xFF00FF66) else Color(0xFFFF3366),
                    darkBg,
                    Color.Black
                  )
                ),
                shape = CircleShape
              )
              .border(
                1.5.dp,
                brush = Brush.linearGradient(
                  listOf(brightGlow, Color.Transparent)
                ),
                shape = CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            androidx.compose.material3.Icon(
              imageVector = icon,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }

          Column {
            Text(
              text = title,
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = TextWhite,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = subtitle,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              fontFamily = FontFamily.Monospace,
              color = if (isActive) NeonGreenBright else TextMuted
            )
          }
        }

        // Action Content (Switch / 3D Button)
        Box(
          modifier = Modifier.padding(start = 8.dp),
          contentAlignment = Alignment.CenterEnd
        ) {
          actionContent()
        }
      }
    }
  }
}
