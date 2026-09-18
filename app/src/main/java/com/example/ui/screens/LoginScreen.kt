package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.GlowingButton
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
  var isRegisterMode by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var showForgotDialog by remember { mutableStateOf(false) }
  var resetEmail by remember { mutableStateOf("") }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 24.dp, vertical = 36.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Header
    Text(
      text = "KRISHNA CONFIG",
      fontSize = 28.sp,
      fontWeight = FontWeight.ExtraBold,
      fontFamily = FontFamily.Monospace,
      color = BloodRedPrimary,
      letterSpacing = 3.sp,
      textAlign = TextAlign.Center
    )

    Text(
      text = if (isRegisterMode) "CREATE NEW ACCOUNT" else "SECURITY AUTHORIZATION",
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = NeonGreenBright,
      letterSpacing = 2.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
    )

    CyberGlassCard(
      modifier = Modifier.fillMaxWidth(),
      isGreenAccent = isRegisterMode,
      pulsateGlow = true
    ) {
      // Toggle Tabs
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        Text(
          text = "SIGN IN",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = if (!isRegisterMode) NeonGreen else TextMuted,
          modifier = Modifier
            .clickable { isRegisterMode = false }
            .padding(8.dp)
            .testTag("tab_signin")
        )
        Text(
          text = "|",
          color = TextMuted.copy(alpha = 0.5f),
          modifier = Modifier.padding(8.dp)
        )
        Text(
          text = "REGISTER",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = if (isRegisterMode) BloodRedPrimary else TextMuted,
          modifier = Modifier
            .clickable { isRegisterMode = true }
            .padding(8.dp)
            .testTag("tab_register")
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Email field
      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email / Username", color = TextMuted) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Email",
            tint = if (isRegisterMode) BloodRedPrimary else NeonGreen
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
          focusedContainerColor = CyberDarkBg.copy(alpha = 0.4f),
          unfocusedContainerColor = CyberDarkBg.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(10.dp),
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
            tint = if (isRegisterMode) BloodRedPrimary else NeonGreen
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
          focusedContainerColor = CyberDarkBg.copy(alpha = 0.4f),
          unfocusedContainerColor = CyberDarkBg.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(10.dp),
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
              fontSize = 12.sp
            )
          }
        }
      } else {
        Spacer(modifier = Modifier.height(16.dp))
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Button
      GlowingButton(
        onClick = {
          if (isRegisterMode) {
            onRegister(email.trim(), password.trim())
          } else {
            onLogin(email.trim(), password.trim())
          }
        },
        isGreen = !isRegisterMode,
        enabled = !isLoading,
        testTag = "auth_action_button"
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = CyberDarkBg
          )
        } else {
          Icon(
            imageVector = Icons.Default.VpnKey,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.size(8.dp))
          Text(
            text = if (isRegisterMode) "CREATE ACCOUNT" else "INITIALIZE ACCESS",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
        }
      }
    }
  }

  // Forgot password dialog
  if (showForgotDialog) {
    AlertDialog(
      onDismissRequest = { showForgotDialog = false },
      title = {
        Text(
          text = "RESET PASSWORD",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = BloodRedPrimary
        )
      },
      text = {
        Column {
          Text(
            text = "Enter your registered email to receive a password reset link.",
            color = TextWhite,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(12.dp))
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
          Text("SEND LINK", color = NeonGreen, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showForgotDialog = false }) {
          Text("CANCEL", color = TextMuted)
        }
      },
      containerColor = CyberDarkBg,
      shape = RoundedCornerShape(16.dp)
    )
  }
}
