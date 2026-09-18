const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();
const db = admin.firestore();

// Telegram Webhook Handler (Cloud Function)
exports.telegramWebhook = functions.https.onRequest(async (req, res) => {
  if (req.method !== "POST") {
    return res.status(200).send("OK");
  }

  const update = req.body;
  const configDoc = await db.collection("settings").document("config").get();
  const botToken = configDoc.exists ? configDoc.data().telegramBotToken : process.env.TELEGRAM_BOT_TOKEN;

  if (!botToken) {
    console.error("Missing bot token");
    return res.status(200).send("No Token");
  }

  const telegramApi = `https://api.telegram.org/bot${botToken}`;

  try {
    // 1. Handle Inline Button Callbacks (Approve / Reject)
    if (update.callback_query) {
      const cb = update.callback_query;
      const data = cb.data || "";

      if (data.startsWith("approve:")) {
        const userId = data.split(":")[1];
        await db.collection("users").document(userId).set({
          verified: true,
          status: "approved"
        }, { merge: true });
        await db.collection("payments").document(userId).set({
          status: "approved"
        }, { merge: true });

        await axios.post(`${telegramApi}/answerCallbackQuery`, {
          callback_query_id: cb.id,
          text: "User Approved!"
        });

        await axios.post(`${telegramApi}/editMessageCaption`, {
          chat_id: cb.message.chat.id,
          message_id: cb.message.message_id,
          caption: (cb.message.caption || "") + "\n\n✅ *APPROVED BY ADMIN*",
          parse_mode: "Markdown"
        }).catch(() => {});
      } else if (data.startsWith("reject:")) {
        const userId = data.split(":")[1];
        await db.collection("users").document(userId).set({
          verified: false,
          status: "rejected"
        }, { merge: true });
        await db.collection("payments").document(userId).set({
          status: "rejected"
        }, { merge: true });

        await axios.post(`${telegramApi}/answerCallbackQuery`, {
          callback_query_id: cb.id,
          text: "User Rejected!"
        });

        await axios.post(`${telegramApi}/editMessageCaption`, {
          chat_id: cb.message.chat.id,
          message_id: cb.message.message_id,
          caption: (cb.message.caption || "") + "\n\n❌ *REJECTED BY ADMIN*",
          parse_mode: "Markdown"
        }).catch(() => {});
      }
    }

    // 2. Handle Text Commands
    if (update.message && update.message.text) {
      const text = update.message.text.trim();
      const chatId = update.message.chat.id;

      if (text === "/start") {
        await axios.post(`${telegramApi}/sendMessage`, {
          chat_id: chatId,
          text: "⚡ *KRISHNA CONFIG — CLOUD WEBHOOK ACTIVE*\nAdmin controls ready.",
          parse_mode: "Markdown"
        });
      } else if (text.startsWith("/setupi ")) {
        const upi = text.replace("/setupi ", "").trim();
        await db.collection("settings").document("config").set({ upiId: upi }, { merge: true });
        await axios.post(`${telegramApi}/sendMessage`, {
          chat_id: chatId,
          text: `✅ UPI ID changed to: \`${upi}\``,
          parse_mode: "Markdown"
        });
      }
    }
  } catch (err) {
    console.error("Webhook processing error:", err.message);
  }

  return res.status(200).send("OK");
});

// Trigger: When new payment created in Firestore, notify Telegram admin
exports.onNewPaymentSubmission = functions.firestore
  .document("payments/{userId}")
  .onCreate(async (snap, context) => {
    const payment = snap.data();
    const configDoc = await db.collection("settings").document("config").get();
    if (!configDoc.exists) return;

    const config = configDoc.data();
    const botToken = config.telegramBotToken;
    const adminChatId = config.telegramAdminChatId;

    if (!botToken || !adminChatId) return;

    const caption = `⚡ *NEW PAYMENT SUBMITTED*\n👤 User: ${payment.email}\n📱 Device: ${payment.deviceName}\n💳 Ref: ${payment.upiRef}\n💰 Amount: ₹${payment.amount}`;
    const keyboard = {
      inline_keyboard: [
        [
          { text: "✅ APPROVE", callback_data: `approve:${payment.userId}` },
          { text: "❌ REJECT", callback_data: `reject:${payment.userId}` }
        ]
      ]
    };

    try {
      if (payment.screenshotUrl) {
        await axios.post(`https://api.telegram.org/bot${botToken}/sendPhoto`, {
          chat_id: adminChatId,
          photo: payment.screenshotUrl,
          caption: caption,
          parse_mode: "Markdown",
          reply_markup: keyboard
        });
      } else {
        await axios.post(`https://api.telegram.org/bot${botToken}/sendMessage`, {
          chat_id: adminChatId,
          text: caption,
          parse_mode: "Markdown",
          reply_markup: keyboard
        });
      }
    } catch (e) {
      console.error("Telegram notification failed:", e.message);
    }
  });
