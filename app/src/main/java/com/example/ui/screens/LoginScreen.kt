package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BloodyTitle
import com.example.ui.components.Cyber3DButton
import com.example.ui.components.CyberGlassCard
import com.example.ui.theme.BloodRedPrimary
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun LoginScreen(
  onLogin: (String, String) -> Unit,
  onRegister: (String, String) -> Unit,
  onForgotPassword: (String) -> Unit,
  isLoading: Boolean,
  modifier: Modifier = Modifier
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var isRegisterMode by remember { mutableStateOf(false) }
  var showForgotDialog by remember { mutableStateOf(false) }
  var resetEmail by remember { mutableStateOf("") }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Header - Visceral 3D Bloody Cyber Title with Specular Drips
    BloodyTitle(
      titleSize = 32.sp,
      subtitle = if (isRegisterMode) "NEW RIG INITIALIZATION" else "SECURITY AUTHORIZATION CONSOLE",
      showDrips = true,
      dropHeight = 16.dp,
      modifier = Modifier.padding(bottom = 22.dp)
    )

    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = !isRegisterMode,
      pulsateGlow = true
    ) {
      // 3D Toggle Tabs Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberDarkBg.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
          .border(1.dp, if (!isRegisterMode) NeonGreen.copy(alpha = 0.4f) else BloodRedPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
          .padding(4.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .background(
                if (!isRegisterMode) NeonGreen.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(8.dp)
              )
              .clickable { isRegisterMode = false }
              .padding(vertical = 10.dp)
              .testTag("tab_signin"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "SIGN IN",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = if (!isRegisterMode) NeonGreenBright else TextMuted,
              letterSpacing = 1.sp
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .background(
                if (isRegisterMode) BloodRedPrimary.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(8.dp)
              )
              .clickable { isRegisterMode = true }
              .padding(vertical = 10.dp)
              .testTag("tab_register"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "REGISTER",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = if (isRegisterMode) BloodRedPrimary else TextMuted,
              letterSpacing = 1.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Email field
      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email / Username", color = TextMuted) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Email",
            tint = if (isRegisterMode) BloodRedPrimary else NeonGreenBright
          )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Email,
          imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TextWhite,
          unfocusedTextColor = TextWhite,
          focusedBorderColor = if (isRegisterMode) BloodRedPrimary else NeonGreen,
          unfocusedBorderColor = TextMuted.copy(alpha = 0.4f),
          focusedContainerColor = CyberDarkBg.copy(alpha = 0.45f),
          unfocusedContainerColor = CyberDarkBg.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("email_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Password field
      OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password", color = TextMuted) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Password",
            tint = if (isRegisterMode) BloodRedPrimary else NeonGreenBright
          )
        },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = "Toggle password",
              tint = TextMuted
            )
          }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
          onDone = {
            if (isRegisterMode) onRegister(email, password) else onLogin(email, password)
          }
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TextWhite,
          unfocusedTextColor = TextWhite,
          focusedBorderColor = if (isRegisterMode) BloodRedPrimary else NeonGreen,
          unfocusedBorderColor = TextMuted.copy(alpha = 0.4f),
          focusedContainerColor = CyberDarkBg.copy(alpha = 0.45f),
          unfocusedContainerColor = CyberDarkBg.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("password_input")
      )

      // Forgot Password link
      if (!isRegisterMode) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(
            onClick = {
              resetEmail = email
              showForgotDialog = true
            },
            modifier = Modifier.testTag("forgot_password_btn")
          ) {
            Text(
              text = "Forgot password?",
              color = BloodRedPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      } else {
        Spacer(modifier = Modifier.height(18.dp))
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action 3D Button
      Cyber3DButton(
        text = if (isRegisterMode) "CREATE RIG ACCOUNT" else "INITIALIZE ACCESS",
        onClick = {
          if (isRegisterMode) {
            onRegister(email.trim(), password.trim())
          } else {
            onLogin(email.trim(), password.trim())
          }
        },
        isGreen = !isRegisterMode,
        icon = Icons.Default.VpnKey,
        isLoading = isLoading,
        enabled = !isLoading,
        height = 52.dp,
        fontSize = 13.sp,
        testTag = "auth_action_button"
      )
    }
  }

  // Forgot password dialog (3D styled container)
  if (showForgotDialog) {
    AlertDialog(
      onDismissRequest = { showForgotDialog = false },
      title = {
        Text(
          text = "RESET ACCESS KEY",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Black,
          color = BloodRedPrimary
        )
      },
      text = {
        Column {
          Text(
            text = "Enter your registered email to receive an instant password reset transmission.",
            color = TextWhite,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(14.dp))
          OutlinedTextField(
            value = resetEmail,
            onValueChange = { resetEmail = it },
            label = { Text("Account Email") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextWhite,
              unfocusedTextColor = TextWhite,
              focusedBorderColor = NeonGreen,
              unfocusedBorderColor = TextMuted
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            onForgotPassword(resetEmail.trim())
            showForgotDialog = false
          }
        ) {
          Text("SEND LINK", color = NeonGreenBright, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
      },
      dismissButton = {
        TextButton(onClick = { showForgotDialog = false }) {
          Text("CANCEL", color = TextMuted, fontFamily = FontFamily.Monospace)
        }
      },
      containerColor = CyberDarkBg,
      shape = RoundedCornerShape(18.dp)
    )
  }
}
