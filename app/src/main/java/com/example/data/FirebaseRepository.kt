package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

class FirebaseRepository(private val context: Context) {

  private val tag = "KrishnaRepository"
  private val okHttpClient = OkHttpClient()

  private val _config = MutableStateFlow(RemoteAppConfig())
  val config: StateFlow<RemoteAppConfig> = _config.asStateFlow()

  private val _userSession = MutableStateFlow(UserSession())
  val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

  private var firestoreListener: ListenerRegistration? = null

  private var isFirebaseAvailable = false
  private var auth: FirebaseAuth? = null
  private var firestore: FirebaseFirestore? = null
  private var storage: FirebaseStorage? = null
  private var remoteConfig: FirebaseRemoteConfig? = null

  init {
    try {
      if (FirebaseApp.getApps(context).isEmpty()) {
        try {
          FirebaseApp.initializeApp(context)
        } catch (_: Exception) {
          val options = FirebaseOptions.Builder()
            .setApplicationId("1:361994075112:android:2a06841a54915c10db025d")
            .setApiKey("AIzaSyAmOv_-ygzTiYJ3PqGg656VuXAh-7R2j_I")
            .setDatabaseUrl("https://screen-monitor-29225-default-rtdb.firebaseio.com")
            .setProjectId("screen-monitor-29225")
            .setStorageBucket("screen-monitor-29225.firebasestorage.app")
            .build()
          FirebaseApp.initializeApp(context, options)
        }
      }
      auth = FirebaseAuth.getInstance()
      firestore = FirebaseFirestore.getInstance()
      storage = FirebaseStorage.getInstance()
      remoteConfig = FirebaseRemoteConfig.getInstance()
      isFirebaseAvailable = true
      setupRemoteConfig()
      listenToFirestoreConfig()
    } catch (e: Exception) {
      Log.w(tag, "Firebase initialization fallback active: ${e.message}")
      isFirebaseAvailable = false
    }
  }

  private fun setupRemoteConfig() {
    try {
      remoteConfig?.let { rc ->
        val configSettings = FirebaseRemoteConfigSettings.Builder()
          .setMinimumFetchIntervalInSeconds(30)
          .build()
        rc.setConfigSettingsAsync(configSettings)
        rc.fetchAndActivate().addOnCompleteListener { task ->
          if (task.isSuccessful) {
            val videoUrlsJson = rc.getString("background_video_urls")
            if (videoUrlsJson.isNotBlank()) {
              try {
                val jsonArr = JSONArray(videoUrlsJson)
                val list = mutableListOf<String>()
                for (i in 0 until jsonArr.length()) {
                  list.add(jsonArr.getString(i))
                }
                if (list.isNotEmpty()) {
                  _config.value = _config.value.copy(videoUrls = list)
                }
              } catch (_: Exception) {}
            }
            val upiId = rc.getString("upi_id")
            val amount = rc.getString("payment_amount")
            val qr = rc.getString("payment_qr_url")
            val wa = rc.getString("contact_whatsapp")
            val tg = rc.getString("contact_telegram")
            _config.value = _config.value.copy(
              upiId = if (upiId.isNotBlank()) upiId else _config.value.upiId,
              paymentAmount = if (amount.isNotBlank()) amount else _config.value.paymentAmount,
              qrImageUrl = if (qr.isNotBlank()) qr else _config.value.qrImageUrl,
              whatsappNumber = if (wa.isNotBlank()) wa else _config.value.whatsappNumber,
              telegramHandle = if (tg.isNotBlank()) tg else _config.value.telegramHandle
            )
          }
        }
      }
    } catch (e: Exception) {
      Log.e(tag, "Remote config fetch error: ${e.message}")
    }
  }

  private fun listenToFirestoreConfig() {
    try {
      firestore?.collection("settings")?.document("config")
        ?.addSnapshotListener { snapshot, error ->
          if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
          val upi = snapshot.getString("upiId") ?: _config.value.upiId
          val amount = snapshot.getString("paymentAmount") ?: _config.value.paymentAmount
          val qr = snapshot.getString("qrImageUrl") ?: _config.value.qrImageUrl
          val wa = snapshot.getString("whatsappNumber") ?: _config.value.whatsappNumber
          val tg = snapshot.getString("telegramHandle") ?: _config.value.telegramHandle
          val botToken = snapshot.getString("telegramBotToken") ?: _config.value.telegramBotToken
          val adminChatId = snapshot.getString("telegramAdminChatId") ?: _config.value.telegramAdminChatId
          val maintenance = snapshot.getBoolean("maintenanceMode") ?: false
          val antihack = snapshot.getBoolean("antihackProtection") ?: true

          @Suppress("UNCHECKED_CAST")
          val videos = snapshot.get("videoUrls") as? List<String>

          _config.value = _config.value.copy(
            upiId = upi,
            paymentAmount = amount,
            qrImageUrl = qr,
            whatsappNumber = wa,
            telegramHandle = tg,
            telegramBotToken = botToken,
            telegramAdminChatId = adminChatId,
            maintenanceMode = maintenance,
            antihackProtection = antihack,
            videoUrls = if (!videos.isNullOrEmpty()) videos else _config.value.videoUrls
          )
        }
    } catch (e: Exception) {
      Log.w(tag, "Firestore config listen: ${e.message}")
    }
  }

  fun signIn(
    email: String,
    pass: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    if (isFirebaseAvailable && auth != null) {
      auth?.signInWithEmailAndPassword(email, pass)
        ?.addOnSuccessListener { result ->
          val uid = result.user?.uid ?: UUID.randomUUID().toString()
          _userSession.value = _userSession.value.copy(
            userId = uid,
            email = email
          )
          startUserStatusListener(uid)
          onSuccess()
        }
        ?.addOnFailureListener { err ->
          // Fallback to demo mode if auth fails due to test credentials
          onError(err.localizedMessage ?: "Sign in failed")
        }
    } else {
      // Local fallback for offline/emulator
      val mockUid = "DEMO_" + Math.abs(email.hashCode())
      _userSession.value = _userSession.value.copy(
        userId = mockUid,
        email = email
      )
      onSuccess()
    }
  }

  fun signUp(
    email: String,
    pass: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    if (isFirebaseAvailable && auth != null) {
      auth?.createUserWithEmailAndPassword(email, pass)
        ?.addOnSuccessListener { result ->
          val uid = result.user?.uid ?: UUID.randomUUID().toString()
          _userSession.value = _userSession.value.copy(
            userId = uid,
            email = email
          )
          // Register in firestore
          val data = mapOf(
            "userId" to uid,
            "email" to email,
            "verified" to false,
            "status" to "none",
            "createdAt" to System.currentTimeMillis()
          )
          firestore?.collection("users")?.document(uid)?.set(data, SetOptions.merge())
          startUserStatusListener(uid)
          sendTextMessageToTelegram("🚨 *NEW USER SIGN UP*\n━━━━━━━━━━━━━━━━━━━━━━\n👤 Email: `${email}`\n🆔 UID: `${uid}`")
          onSuccess()
        }
        ?.addOnFailureListener { err ->
          onError(err.localizedMessage ?: "Sign up failed")
        }
    } else {
      val mockUid = "DEMO_" + Math.abs(email.hashCode())
      _userSession.value = _userSession.value.copy(
        userId = mockUid,
        email = email
      )
      onSuccess()
    }
  }

  fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
    if (isFirebaseAvailable && auth != null) {
      auth?.sendPasswordResetEmail(email)
        ?.addOnSuccessListener {
          onResult(true, "Reset link dispatched to $email")
        }
        ?.addOnFailureListener {
          onResult(false, it.localizedMessage ?: "Failed to dispatch reset email")
        }
    } else {
      onResult(true, "Demo reset password link simulated for $email")
    }
  }

  fun setDeviceName(deviceName: String) {
    _userSession.value = _userSession.value.copy(deviceName = deviceName)
    val uid = _userSession.value.userId
    if (isFirebaseAvailable && uid.isNotBlank()) {
      firestore?.collection("users")?.document(uid)?.set(
        mapOf("deviceName" to deviceName),
        SetOptions.merge()
      )
    }
    sendTextMessageToTelegram("📱 *DEVICE BOUND*\n━━━━━━━━━━━━━━━━━━━━━━\n👤 User: `${_userSession.value.email.ifBlank { "Mobile Client" }}`\n📱 Rig: `${deviceName}`\n🆔 UID: `${uid}`")
  }

  fun startUserStatusListener(userId: String) {
    firestoreListener?.remove()
    if (!isFirebaseAvailable || firestore == null || userId.isBlank()) return

    firestoreListener = firestore?.collection("users")?.document(userId)
      ?.addSnapshotListener { snapshot, error ->
        if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
        val verified = snapshot.getBoolean("verified") ?: false
        val statusStr = snapshot.getString("status") ?: if (verified) "approved" else "none"
        val status = when (statusStr.lowercase()) {
          "approved" -> VerificationStatus.APPROVED
          "rejected" -> VerificationStatus.REJECTED
          "pending" -> VerificationStatus.PENDING
          else -> if (verified) VerificationStatus.APPROVED else VerificationStatus.NONE
        }
        val uidConfig = snapshot.getString("uidConfig") ?: _userSession.value.uidConfig
        val protection = snapshot.getBoolean("protectionEnabled") ?: _userSession.value.isProtectionEnabled
        val antihack = snapshot.getBoolean("antihackFixed") ?: _userSession.value.isAntihackFixed
        val blacklist = snapshot.getBoolean("blacklistRemoved") ?: _userSession.value.isBlacklistRemoved

        _userSession.value = _userSession.value.copy(
          verificationStatus = status,
          uidConfig = uidConfig,
          isProtectionEnabled = protection,
          isAntihackFixed = antihack,
          isBlacklistRemoved = blacklist
        )
      }
  }

  suspend fun submitPaymentProof(
    screenshotUri: Uri?,
    upiRef: String,
    onComplete: (Boolean, String) -> Unit
  ) = withContext(Dispatchers.IO) {
    val session = _userSession.value
    val cfg = _config.value
    _userSession.value = _userSession.value.copy(
      upiRef = upiRef,
      screenshotUri = screenshotUri?.toString().orEmpty(),
      verificationStatus = VerificationStatus.PENDING
    )

    var storageUrl = ""
    var imageBytes: ByteArray? = null

    // Compress & read screenshot image
    if (screenshotUri != null) {
      try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(screenshotUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteStream)
        imageBytes = byteStream.toByteArray()

        if (isFirebaseAvailable && storage != null) {
          val filename = "payments/${session.userId}_${System.currentTimeMillis()}.jpg"
          val ref = storage?.reference?.child(filename)
          val uploadTask = ref?.putBytes(imageBytes)?.awaitUpload()
          if (uploadTask != null) {
            storageUrl = ref.downloadUrl.awaitUrl()
          }
        }
      } catch (e: Exception) {
        Log.w(tag, "Screenshot processing notice: ${e.message}")
      }
    }

    // Update Firestore record
    if (isFirebaseAvailable && session.userId.isNotBlank()) {
      val paymentDoc = mapOf(
        "userId" to session.userId,
        "email" to session.email,
        "deviceName" to session.deviceName,
        "upiRef" to upiRef,
        "screenshotUrl" to storageUrl,
        "amount" to cfg.paymentAmount,
        "status" to "pending",
        "timestamp" to System.currentTimeMillis()
      )
      firestore?.collection("payments")?.document(session.userId)?.set(paymentDoc, SetOptions.merge())
      firestore?.collection("users")?.document(session.userId)?.set(
        mapOf(
          "status" to "pending",
          "verified" to false,
          "upiRef" to upiRef,
          "screenshotUrl" to storageUrl
        ),
        SetOptions.merge()
      )
    }

    // Send notification to Telegram Bot
    val token = cfg.telegramBotToken.ifBlank { "8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4" }
    val chatId = cfg.telegramAdminChatId.ifBlank { "8491850372" }
    if (token.isNotBlank() && chatId.isNotBlank()) {
      sendToTelegramBot(
        token = token,
        chatId = chatId,
        imageBytes = imageBytes,
        caption = """
          ⚡ *NEW KRISHNA CONFIG PAYMENT* ⚡
          ━━━━━━━━━━━━━━━━━━━━━━
          👤 *User:* `${session.email}`
          🆔 *UID:* `${session.userId}`
          📱 *Device:* `${session.deviceName}`
          💳 *UPI Ref:* `${upiRef}`
          💰 *Amount:* ₹${cfg.paymentAmount}
          🕒 *Time:* `${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}`
          ━━━━━━━━━━━━━━━━━━━━━━
        """.trimIndent(),
        userId = session.userId
      )
    }

    withContext(Dispatchers.Main) {
      onComplete(true, "Payment submitted! Awaiting admin approval.")
    }
  }

  fun sendTextMessageToTelegram(message: String) {
    val cfg = _config.value
    val token = cfg.telegramBotToken.ifBlank { "8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4" }
    val chatId = cfg.telegramAdminChatId.ifBlank { "8491850372" }
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val jsonPayload = JSONObject().apply {
          put("chat_id", chatId)
          put("text", message)
          put("parse_mode", "Markdown")
        }
        val request = Request.Builder()
          .url("https://api.telegram.org/bot$token/sendMessage")
          .post(jsonPayload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
          .build()
        okHttpClient.newCall(request).execute()
      } catch (e: Exception) {
        Log.w(tag, "Telegram text send notice: ${e.message}")
      }
    }
  }

  private fun sendToTelegramBot(
    token: String,
    chatId: String,
    imageBytes: ByteArray?,
    caption: String,
    userId: String
  ) {
    try {
      val inlineKeyboard = JSONObject().apply {
        val row = JSONArray().apply {
          put(JSONObject().apply {
            put("text", "✅ APPROVE")
            put("callback_data", "approve:$userId")
          })
          put(JSONObject().apply {
            put("text", "❌ REJECT")
            put("callback_data", "reject:$userId")
          })
        }
        put("inline_keyboard", JSONArray().put(row))
      }

      if (imageBytes != null && imageBytes.isNotEmpty()) {
        val requestBody = MultipartBody.Builder()
          .setType(MultipartBody.FORM)
          .addFormDataPart("chat_id", chatId)
          .addFormDataPart("caption", caption)
          .addFormDataPart("parse_mode", "Markdown")
          .addFormDataPart("reply_markup", inlineKeyboard.toString())
          .addFormDataPart(
            "photo",
            "proof.jpg",
            imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
          )
          .build()

        val request = Request.Builder()
          .url("https://api.telegram.org/bot$token/sendPhoto")
          .post(requestBody)
          .build()

        okHttpClient.newCall(request).execute()
      } else {
        val jsonPayload = JSONObject().apply {
          put("chat_id", chatId)
          put("text", caption)
          put("parse_mode", "Markdown")
          put("reply_markup", inlineKeyboard)
        }

        val request = Request.Builder()
          .url("https://api.telegram.org/bot$token/sendMessage")
          .post(jsonPayload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
          .build()

        okHttpClient.newCall(request).execute()
      }
    } catch (e: Exception) {
      Log.e(tag, "Failed to send Telegram proof: ${e.message}")
    }
  }

  // Helper for direct test simulation (allowing admin approval demonstration)
  fun simulateAdminDecision(approved: Boolean) {
    val newStatus = if (approved) VerificationStatus.APPROVED else VerificationStatus.REJECTED
    _userSession.value = _userSession.value.copy(
      verificationStatus = newStatus
    )
    val uid = _userSession.value.userId
    if (isFirebaseAvailable && uid.isNotBlank()) {
      firestore?.collection("users")?.document(uid)?.set(
        mapOf(
          "status" to if (approved) "approved" else "rejected",
          "verified" to approved
        ),
        SetOptions.merge()
      )
    }
  }

  fun updateSafeZoneFeature(
    uidConfig: String? = null,
    protectionEnabled: Boolean? = null,
    antihackFixed: Boolean? = null,
    blacklistRemoved: Boolean? = null
  ) {
    _userSession.value = _userSession.value.copy(
      uidConfig = uidConfig ?: _userSession.value.uidConfig,
      isProtectionEnabled = protectionEnabled ?: _userSession.value.isProtectionEnabled,
      isAntihackFixed = antihackFixed ?: _userSession.value.isAntihackFixed,
      isBlacklistRemoved = blacklistRemoved ?: _userSession.value.isBlacklistRemoved
    )
    val uid = _userSession.value.userId
    if (isFirebaseAvailable && uid.isNotBlank()) {
      val map = mutableMapOf<String, Any>()
      uidConfig?.let { map["uidConfig"] = it }
      protectionEnabled?.let { map["protectionEnabled"] = it }
      antihackFixed?.let { map["antihackFixed"] = it }
      blacklistRemoved?.let { map["blacklistRemoved"] = it }
      firestore?.collection("users")?.document(uid)?.set(map, SetOptions.merge())
    }
  }

  fun logout() {
    firestoreListener?.remove()
    try {
      auth?.signOut()
    } catch (_: Exception) {}
    _userSession.value = UserSession()
  }
}

// Coroutine helpers for Firebase tasks without extra heavy dependencies
private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitUpload(): T =
  kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result, null) }
    addOnFailureListener { ex -> continuation.resumeWith(Result.failure(ex)) }
  }

private suspend fun com.google.android.gms.tasks.Task<Uri>.awaitUrl(): String =
  kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { uri -> continuation.resume(uri.toString(), null) }
    addOnFailureListener { ex -> continuation.resumeWith(Result.failure(ex)) }
  }
