package com.example.data

enum class AppScreen {
  SPLASH,
  LOGIN,
  DEVICE,
  PAYMENT,
  SAFE_ZONE
}

enum class VerificationStatus {
  NONE,
  PENDING,
  APPROVED,
  REJECTED
}

data class UserSession(
  val userId: String = "",
  val email: String = "",
  val deviceName: String = "",
  val androidVersion: String = "",
  val deviceModel: String = "",
  val osVersion: String = "",
  val activationKey: String = "",
  val uidConfig: String = "",
  val verificationStatus: VerificationStatus = VerificationStatus.NONE,
  val upiRef: String = "",
  val screenshotUri: String = "",
  val isProtectionEnabled: Boolean = false,
  val isAntihackFixed: Boolean = false,
  val isBlacklistRemoved: Boolean = false,
  val lastUpdated: Long = System.currentTimeMillis()
)

data class RemoteAppConfig(
  val videoUrls: List<String> = listOf(
    "https://res.cloudinary.com/yahhvjki/video/upload/v1789678174/From_Klickpin.com-_957014989582050129-pin-id-957014989582050129_taqtlm.mp4",
    "https://res.cloudinary.com/yahhvjki/video/upload/v1789678172/From_Klickpin.com-_1086423110089551196-pin-id-1086423110089551196_jwzds2.mp4",
    "https://res.cloudinary.com/yahhvjki/video/upload/v1789678166/From_Klickpin.com-_1143421792925928351-pin-id-1143421792925928351_kgb26p.mp4"
  ),
  val upiId: String = "krishnaconfig@ybl",
  val upiName: String = "KRISHNA CONFIG PROTECT",
  val paymentAmount: String = "499",
  val qrImageUrl: String = "",
  val whatsappNumber: String = "8383901428",
  val telegramHandle: String = "@KRISHNACONFIIG",
  val telegramBotToken: String = "8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4",
  val telegramAdminChatId: String = "8491850372",
  val maintenanceMode: Boolean = false,
  val antihackProtection: Boolean = true
)
