#!/usr/bin/env python3
"""
KRISHNA CONFIG — Full Telegram Bot Admin Control Panel
Powered by python-telegram-bot v20+ and firebase-admin.

Usage:
  export TELEGRAM_BOT_TOKEN="your_bot_token"
  export ADMIN_CHAT_ID="your_telegram_chat_id"
  python bot.py
"""

import os
import sys
import json
import logging
from typing import List

from telegram import (
    Update,
    InlineKeyboardButton,
    InlineKeyboardMarkup,
)
from telegram.constants import ParseMode
from telegram.ext import (
    Application,
    CommandHandler,
    CallbackQueryHandler,
    ContextTypes,
    MessageHandler,
    filters,
)

import firebase_admin
from firebase_admin import credentials, firestore, auth, messaging

logging.basicConfig(
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s", level=logging.INFO
)
logger = logging.getLogger("KrishnaBot")

# Initialize Firebase Admin SDK
SERVICE_ACCOUNT_KEY = os.getenv("FIREBASE_SERVICE_ACCOUNT", "serviceAccountKey.json")
if os.path.exists(SERVICE_ACCOUNT_KEY):
    cred = credentials.Certificate(SERVICE_ACCOUNT_KEY)
    firebase_admin.initialize_app(cred, {"projectId": "screen-monitor-29225"})
else:
    try:
        firebase_admin.initialize_app(options={"projectId": "screen-monitor-29225"})
    except Exception as e:
        logger.warning(f"Firebase default initialization notice: {e}")

db = firestore.client()

BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4")
ADMIN_CHAT_ID = os.getenv("ADMIN_CHAT_ID", "8491850372")


def is_admin(user_id: int) -> bool:
    if not ADMIN_CHAT_ID:
        return True
    return str(user_id) == str(ADMIN_CHAT_ID)


def get_admin_keyboard() -> InlineKeyboardMarkup:
    keyboard = [
        [
            InlineKeyboardButton("📊 View Stats", callback_data="cmd_stats"),
            InlineKeyboardButton("⏳ Pending Payments", callback_data="cmd_pending"),
        ],
        [
            InlineKeyboardButton("👥 List Users", callback_data="cmd_listusers"),
            InlineKeyboardButton("🟢 Online Users", callback_data="cmd_online"),
        ],
        [
            InlineKeyboardButton("🛡️ Anti-Hack Toggle", callback_data="cmd_antihack_toggle"),
            InlineKeyboardButton("⚠️ Maintenance Toggle", callback_data="cmd_maint_toggle"),
        ],
        [
            InlineKeyboardButton("📞 Contact Info", callback_data="cmd_details"),
            InlineKeyboardButton("🔄 Refresh Status", callback_data="cmd_refresh"),
        ],
    ]
    return InlineKeyboardMarkup(keyboard)


async def start_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        await update.message.reply_text("⛔ Unauthorized access.")
        return

    text = (
        "⚡ *KRISHNA CONFIG — ADMIN CONTROL PANEL* ⚡\n"
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
        "Welcome Admin! You have 100% full control over the mobile application.\n\n"
        "🛠 *QUICK COMMANDS:*\n"
        "`/stats` — Real-time analytics\n"
        "`/pending` — Pending payments queue\n"
        "`/setqr <image_url>` — Change UPI QR code\n"
        "`/setupi <upi_id>` — Change UPI ID\n"
        "`/setamount <price>` — Change activation price\n"
        "`/setvideo <url1,url2>` — Update looping background videos\n"
        "`/setcontact <wa> <tg>` — Update WhatsApp & Telegram handles\n"
        "`/adduser <email> <pass>` — Create user manually\n"
        "`/deluser <email>` — Delete user\n"
        "`/listusers` — List registered users\n"
        "`/blacklist <uid>` — Blacklist device UID\n"
        "`/removeblacklist <uid>` — Unblacklist device UID\n"
        "`/antihack on|off` — Toggle anti-hack defense\n"
        "`/maintenance on|off` — Toggle maintenance\n"
        "`/broadcast <msg>` — Send push message to all users\n"
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    )
    await update.message.reply_text(
        text, parse_mode=ParseMode.MARKDOWN, reply_markup=get_admin_keyboard()
    )


async def setqr_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/setqr https://example.com/qr.png`", parse_mode=ParseMode.MARKDOWN)
        return
    url = context.args[0]
    db.collection("settings").document("config").set({"qrImageUrl": url}, merge=True)
    await update.message.reply_text(f"✅ *Payment QR Code updated:*\n`{url}`", parse_mode=ParseMode.MARKDOWN)


async def setupi_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/setupi krishnaconfig@ybl`", parse_mode=ParseMode.MARKDOWN)
        return
    upi = context.args[0]
    db.collection("settings").document("config").set({"upiId": upi}, merge=True)
    await update.message.reply_text(f"✅ *UPI ID updated:*\n`{upi}`", parse_mode=ParseMode.MARKDOWN)


async def setamount_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/setamount 499`", parse_mode=ParseMode.MARKDOWN)
        return
    amount = context.args[0]
    db.collection("settings").document("config").set({"paymentAmount": amount}, merge=True)
    await update.message.reply_text(f"✅ *Payment amount updated:* ₹`{amount}`", parse_mode=ParseMode.MARKDOWN)


async def setvideo_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/setvideo url1,url2,url3`", parse_mode=ParseMode.MARKDOWN)
        return
    raw = " ".join(context.args)
    urls = [u.strip() for u in raw.split(",") if u.strip()]
    db.collection("settings").document("config").set({"videoUrls": urls}, merge=True)
    await update.message.reply_text(f"✅ *Updated {len(urls)} background video(s).*")


async def setcontact_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if len(context.args) < 2:
        await update.message.reply_text("Usage: `/setcontact 8383901428 @KRISHNACONFIIG`", parse_mode=ParseMode.MARKDOWN)
        return
    wa, tg = context.args[0], context.args[1]
    db.collection("settings").document("config").set({"whatsappNumber": wa, "telegramHandle": tg}, merge=True)
    await update.message.reply_text(f"✅ *Contacts Updated:*\nWhatsApp: `{wa}`\nTelegram: `{tg}`", parse_mode=ParseMode.MARKDOWN)


async def adduser_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if len(context.args) < 2:
        await update.message.reply_text("Usage: `/adduser user@gmail.com pass123`", parse_mode=ParseMode.MARKDOWN)
        return
    email, password = context.args[0], context.args[1]
    try:
        user = auth.create_user(email=email, password=password)
        db.collection("users").document(user.uid).set(
            {"userId": user.uid, "email": email, "verified": True, "status": "approved"}
        )
        await update.message.reply_text(f"✅ *User Created & Activated:*\n`{email}` (UID: `{user.uid}`)", parse_mode=ParseMode.MARKDOWN)
    except Exception as e:
        await update.message.reply_text(f"❌ Error creating user: {e}")


async def deluser_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/deluser user@gmail.com`", parse_mode=ParseMode.MARKDOWN)
        return
    email = context.args[0]
    try:
        user = auth.get_user_by_email(email)
        auth.delete_user(user.uid)
        db.collection("users").document(user.uid).delete()
        await update.message.reply_text(f"✅ *User Deleted:* `{email}`", parse_mode=ParseMode.MARKDOWN)
    except Exception as e:
        await update.message.reply_text(f"❌ Error deleting user: {e}")


async def listusers_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    docs = db.collection("users").limit(30).stream()
    lines = ["👥 *REGISTERED USERS:*"]
    for doc in docs:
        d = doc.to_dict()
        email = d.get("email", "unknown")
        status = "✅" if d.get("verified") else "⏳"
        device = d.get("deviceName", "No Rig")
        lines.append(f"{status} `{email}` | {device}")
    await update.message.reply_text("\n".join(lines), parse_mode=ParseMode.MARKDOWN)


async def pending_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    docs = db.collection("payments").where("status", "==", "pending").stream()
    count = 0
    for doc in docs:
        count += 1
        d = doc.to_dict()
        uid = d.get("userId", "")
        text = (
            f"⏳ *PENDING APPROVAL*\n"
            f"👤 User: `{d.get('email')}`\n"
            f"📱 Device: `{d.get('deviceName')}`\n"
            f"💳 UPI Ref: `{d.get('upiRef')}`\n"
            f"💰 Amount: ₹`{d.get('amount')}`\n"
            f"🆔 UID: `{uid}`"
        )
        keyboard = InlineKeyboardMarkup([
            [
                InlineKeyboardButton("✅ APPROVE", callback_data=f"approve:{uid}"),
                InlineKeyboardButton("❌ REJECT", callback_data=f"reject:{uid}"),
            ]
        ])
        screenshot_url = d.get("screenshotUrl")
        if screenshot_url:
            await update.message.reply_photo(photo=screenshot_url, caption=text, parse_mode=ParseMode.MARKDOWN, reply_markup=keyboard)
        else:
            await update.message.reply_text(text, parse_mode=ParseMode.MARKDOWN, reply_markup=keyboard)

    if count == 0:
        await update.message.reply_text("✅ No pending payments in queue.")


async def blacklist_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/blacklist <uid>`", parse_mode=ParseMode.MARKDOWN)
        return
    uid = context.args[0]
    db.collection("blacklists").document(uid).set({"blacklisted": True, "timestamp": firestore.SERVER_TIMESTAMP})
    await update.message.reply_text(f"🚫 *UID Blacklisted:* `{uid}`", parse_mode=ParseMode.MARKDOWN)


async def removeblacklist_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/removeblacklist <uid>`", parse_mode=ParseMode.MARKDOWN)
        return
    uid = context.args[0]
    db.collection("blacklists").document(uid).delete()
    await update.message.reply_text(f"✅ *UID Unblacklisted:* `{uid}`", parse_mode=ParseMode.MARKDOWN)


async def antihack_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/antihack on|off`", parse_mode=ParseMode.MARKDOWN)
        return
    state = context.args[0].lower() == "on"
    db.collection("settings").document("config").set({"antihackProtection": state}, merge=True)
    await update.message.reply_text(f"🛡️ Anti-Hack Protection is now *{'ENABLED' if state else 'DISABLED'}*", parse_mode=ParseMode.MARKDOWN)


async def maintenance_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/maintenance on|off`", parse_mode=ParseMode.MARKDOWN)
        return
    state = context.args[0].lower() == "on"
    db.collection("settings").document("config").set({"maintenanceMode": state}, merge=True)
    await update.message.reply_text(f"⚠️ Maintenance Mode is now *{'ACTIVE' if state else 'OFF'}*", parse_mode=ParseMode.MARKDOWN)


async def stats_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    users = len(list(db.collection("users").stream()))
    payments = len(list(db.collection("payments").stream()))
    pending = len(list(db.collection("payments").where("status", "==", "pending").stream()))
    approved = len(list(db.collection("payments").where("status", "==", "approved").stream()))

    text = (
        "📊 *KRISHNA CONFIG — SYSTEM STATS*\n"
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
        f"👥 Total Users: `{users}`\n"
        f"💳 Total Submissions: `{payments}`\n"
        f"✅ Approved: `{approved}`\n"
        f"⏳ Pending: `{pending}`\n"
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    )
    await update.message.reply_text(text, parse_mode=ParseMode.MARKDOWN)


async def broadcast_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/broadcast System maintenance in 10 minutes!`")
        return
    msg = " ".join(context.args)
    try:
        message = messaging.Message(
            notification=messaging.Notification(
                title="⚡ KRISHNA CONFIG ALERT",
                body=msg,
            ),
            topic="all_users",
        )
        messaging.send(message)
        await update.message.reply_text(f"📢 *Push broadcast dispatched:* {msg}")
    except Exception as e:
        await update.message.reply_text(f"Broadcast notice: {e}")


# Callback Query Handler for Inline Buttons (APPROVE / REJECT)
async def button_callback(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    query = update.callback_query
    await query.answer()

    data = query.data
    if data.startswith("approve:"):
        uid = data.split(":")[1]
        db.collection("users").document(uid).set({"verified": True, "status": "approved"}, merge=True)
        db.collection("payments").document(uid).set({"status": "approved"}, merge=True)
        await query.edit_message_caption(
            caption=(query.message.caption or "") + "\n\n✅ *STATUS: APPROVED BY ADMIN*",
            parse_mode=ParseMode.MARKDOWN
        ) if query.message.caption else await query.edit_message_text(
            text=(query.message.text or "") + "\n\n✅ *STATUS: APPROVED BY ADMIN*",
            parse_mode=ParseMode.MARKDOWN
        )
    elif data.startswith("reject:"):
        uid = data.split(":")[1]
        db.collection("users").document(uid).set({"verified": False, "status": "rejected"}, merge=True)
        db.collection("payments").document(uid).set({"status": "rejected"}, merge=True)
        await query.edit_message_caption(
            caption=(query.message.caption or "") + "\n\n❌ *STATUS: REJECTED BY ADMIN*",
            parse_mode=ParseMode.MARKDOWN
        ) if query.message.caption else await query.edit_message_text(
            text=(query.message.text or "") + "\n\n❌ *STATUS: REJECTED BY ADMIN*",
            parse_mode=ParseMode.MARKDOWN
        )
    elif data == "cmd_stats":
        await stats_command(update, context)
    elif data == "cmd_pending":
        await pending_command(update, context)
    elif data == "cmd_listusers":
        await listusers_command(update, context)


def main():
    if BOT_TOKEN == "YOUR_BOT_TOKEN_HERE":
        print("Set TELEGRAM_BOT_TOKEN environment variable first!")
        sys.exit(1)

    app = Application.builder().token(BOT_TOKEN).build()

    app.add_handler(CommandHandler("start", start_command))
    app.add_handler(CommandHandler("setqr", setqr_command))
    app.add_handler(CommandHandler("setupi", setupi_command))
    app.add_handler(CommandHandler("setamount", setamount_command))
    app.add_handler(CommandHandler("setvideo", setvideo_command))
    app.add_handler(CommandHandler("setcontact", setcontact_command))
    app.add_handler(CommandHandler("adduser", adduser_command))
    app.add_handler(CommandHandler("deluser", deluser_command))
    app.add_handler(CommandHandler("listusers", listusers_command))
    app.add_handler(CommandHandler("pending", pending_command))
    app.add_handler(CommandHandler("blacklist", blacklist_command))
    app.add_handler(CommandHandler("removeblacklist", removeblacklist_command))
    app.add_handler(CommandHandler("antihack", antihack_command))
    app.add_handler(CommandHandler("maintenance", maintenance_command))
    app.add_handler(CommandHandler("stats", stats_command))
    app.add_handler(CommandHandler("broadcast", broadcast_command))

    app.add_handler(CallbackQueryHandler(button_callback))

    print("🤖 Krishna Config Telegram Admin Bot is running...")
    app.run_polling()


if __name__ == "__main__":
    main()
