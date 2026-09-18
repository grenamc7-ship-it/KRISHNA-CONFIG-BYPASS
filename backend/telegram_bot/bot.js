const { Telegraf, Markup } = require("telegraf");
const admin = require("firebase-admin");

// Initialize Firebase Admin
if (process.env.FIREBASE_SERVICE_ACCOUNT) {
  const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    projectId: "screen-monitor-29225"
  });
} else {
  try {
    admin.initializeApp({ projectId: "screen-monitor-29225" });
  } catch (e) {
    console.warn("Firebase default init notice:", e.message);
  }
}

const db = admin.firestore();
const bot = new Telegraf(process.env.TELEGRAM_BOT_TOKEN || "8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4");
const ADMIN_CHAT_ID = process.env.ADMIN_CHAT_ID || "8491850372";

const isAdmin = (ctx) => {
  if (!ADMIN_CHAT_ID) return true;
  return String(ctx.from.id) === String(ADMIN_CHAT_ID);
};

bot.start((ctx) => {
  if (!isAdmin(ctx)) return ctx.reply("⛔ Unauthorized.");
  ctx.reply(
    "⚡ *KRISHNA CONFIG — ADMIN CONTROL PANEL* ⚡\n" +
    "Use commands or inline buttons to control the system.",
    {
      parse_mode: "Markdown",
      ...Markup.inlineKeyboard([
        [Markup.button.callback("📊 Stats", "cmd_stats"), Markup.button.callback("⏳ Pending", "cmd_pending")],
        [Markup.button.callback("🛡️ Toggle Anti-Hack", "cmd_antihack"), Markup.button.callback("⚠️ Toggle Maint", "cmd_maint")]
      ])
    }
  );
});

bot.command("setqr", async (ctx) => {
  if (!isAdmin(ctx)) return;
  const url = ctx.message.text.split(" ")[1];
  if (!url) return ctx.reply("Usage: /setqr <url>");
  await db.collection("settings").document("config").set({ qrImageUrl: url }, { merge: true });
  ctx.reply(`✅ QR updated: ${url}`);
});

bot.command("setupi", async (ctx) => {
  if (!isAdmin(ctx)) return;
  const upi = ctx.message.text.split(" ")[1];
  if (!upi) return ctx.reply("Usage: /setupi <upi_id>");
  await db.collection("settings").document("config").set({ upiId: upi }, { merge: true });
  ctx.reply(`✅ UPI ID updated: ${upi}`);
});

bot.command("setamount", async (ctx) => {
  if (!isAdmin(ctx)) return;
  const amt = ctx.message.text.split(" ")[1];
  if (!amt) return ctx.reply("Usage: /setamount <price>");
  await db.collection("settings").document("config").set({ paymentAmount: amt }, { merge: true });
  ctx.reply(`✅ Price updated: ₹${amt}`);
});

bot.command("pending", async (ctx) => {
  if (!isAdmin(ctx)) return;
  const snapshot = await db.collection("payments").where("status", "==", "pending").get();
  if (snapshot.empty) return ctx.reply("✅ No pending payments.");

  for (const doc of snapshot.docs) {
    const data = doc.data();
    const caption = `⏳ *PENDING APPROVAL*\n👤 User: ${data.email}\n📱 Device: ${data.deviceName}\n💳 Ref: ${data.upiRef}\n💰 Amount: ₹${data.amount}`;
    const keyboard = Markup.inlineKeyboard([
      Markup.button.callback("✅ APPROVE", `approve:${data.userId}`),
      Markup.button.callback("❌ REJECT", `reject:${data.userId}`)
    ]);

    if (data.screenshotUrl) {
      await ctx.replyWithPhoto(data.screenshotUrl, { caption, parse_mode: "Markdown", ...keyboard });
    } else {
      await ctx.reply(caption, { parse_mode: "Markdown", ...keyboard });
    }
  }
});

bot.action(/approve:(.+)/, async (ctx) => {
  const uid = ctx.match[1];
  await db.collection("users").document(uid).set({ verified: true, status: "approved" }, { merge: true });
  await db.collection("payments").document(uid).set({ status: "approved" }, { merge: true });
  await ctx.answerCbQuery("Approved!");
  ctx.editMessageCaption((ctx.callbackQuery.message.caption || "") + "\n\n✅ *STATUS: APPROVED BY ADMIN*", { parse_mode: "Markdown" });
});

bot.action(/reject:(.+)/, async (ctx) => {
  const uid = ctx.match[1];
  await db.collection("users").document(uid).set({ verified: false, status: "rejected" }, { merge: true });
  await db.collection("payments").document(uid).set({ status: "rejected" }, { merge: true });
  await ctx.answerCbQuery("Rejected!");
  ctx.editMessageCaption((ctx.callbackQuery.message.caption || "") + "\n\n❌ *STATUS: REJECTED BY ADMIN*", { parse_mode: "Markdown" });
});

bot.launch().then(() => console.log("🤖 Node.js Krishna Admin Bot Started"));
