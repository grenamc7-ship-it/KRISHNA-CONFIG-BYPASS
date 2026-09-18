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


async def start_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        await update.message.reply_text("⛔ Unauthorized. This is a private Admin Bot for Krishna Config.")
        return

    welcome_text = (
        "⚡ *KRISHNA CONFIG MASTER ADMIN BOT* ⚡\n"
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
        "Welcome Admin! You have total command and control over the Krishna Config Android client.\n\n"
        "📋 *COMMANDS:*\n"
        "• `/pending` - View pending payment activation requests\n"
        "• `/approve <uid>` - Instantly approve access & unlock Safe Zone\n"
        "• `/decline <uid>` or `/reject <uid>` - Decline & revoke user access\n"
        "• `/listusers` - List all registered user rigs & activation keys\n"
        "• `/stats` - Live system telemetry & stats\n"
        "• `/setamount <amount>` - Change activation fee (e.g. `/setamount 499`)\n"
        "• `/setupi <upi_id>` - Update UPI ID\n"
        "• `/setqr <image_url>` - Update QR code URL\n"
        "• `/setvideo <url1,url2>` - Update live background video URLs\n"
        "• `/broadcast <message>` - Push alert to all rigs\n"
        "• `/deluser <email>` - Delete user account\n"
        "• `/blacklist <uid>` - Flag / blacklist suspicious device\n"
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    )

    keyboard = InlineKeyboardMarkup([
        [
            InlineKeyboardButton("⏳ PENDING VERIFICATIONS", callback_data="cmd_pending"),
        ],
        [
            InlineKeyboardButton("👥 ALL USERS", callback_data="cmd_listusers"),
            InlineKeyboardButton("📊 SYSTEM STATS", callback_data="cmd_stats"),
        ]
    ])

    await update.message.reply_text(
        welcome_text,
        parse_mode=ParseMode.MARKDOWN,
        reply_markup=keyboard,
    )


async def stats_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return

    try:
        users_count = len(list(db.collection("users").stream()))
        pending_count = len(list(db.collection("payments").where("status", "==", "pending").stream()))
        approved_count = len(list(db.collection("payments").where("status", "==", "approved").stream()))

        stats_text = (
            "📊 *KRISHNA CONFIG LIVE SYSTEM STATS*\n"
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
            f"👥 *Total Registered Rigs:* `{users_count}`\n"
            f"⏳ *Pending Payments:* `{pending_count}`\n"
            f"✅ *Approved Active Licenses:* `{approved_count}`\n"
            f"🛡️ *Antihack Engine:* `ACTIVE`\n"
            f"💎 *Firebase Realtime Sync:* `ONLINE`\n"
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
    except Exception as e:
        stats_text = f"📊 *System Stats Alert:* `{e}`"

    if update.callback_query:
        await update.callback_query.message.reply_text(stats_text, parse_mode=ParseMode.MARKDOWN)
    else:
        await update.message.reply_text(stats_text, parse_mode=ParseMode.MARKDOWN)


async def approve_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return

    if not context.args:
        await update.message.reply_text("Usage: `/approve <user_id>`", parse_mode=ParseMode.MARKDOWN)
        return

    uid = context.args[0]
    try:
        db.collection("users").document(uid).set({"verified": True, "status": "approved"}, merge=True)
        db.collection("payments").document(uid).set({"status": "approved"}, merge=True)
        await update.message.reply_text(
            f"✅ *USER APPROVED & ACTIVATED!*\n🆔 UID: `{uid}`\nSafe Zone has unlocked instantly on their rig.",
            parse_mode=ParseMode.MARKDOWN
        )
    except Exception as e:
        await update.message.reply_text(f"❌ Error approving user: {e}")


async def reject_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return

    if not context.args:
        await update.message.reply_text("Usage: `/decline <user_id>` or `/reject <user_id>`", parse_mode=ParseMode.MARKDOWN)
        return

    uid = context.args[0]
    try:
        db.collection("users").document(uid).set({"verified": False, "status": "rejected"}, merge=True)
        db.collection("payments").document(uid).set({"status": "rejected"}, merge=True)
        await update.message.reply_text(
            f"❌ *USER DECLINED!*\n🆔 UID: `{uid}`\nAccess has been locked/declined on the client app.",
            parse_mode=ParseMode.MARKDOWN
        )
    except Exception as e:
        await update.message.reply_text(f"❌ Error declining user: {e}")


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


async def broadcast_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/broadcast <message>`", parse_mode=ParseMode.MARKDOWN)
        return
    msg = " ".join(context.args)
    try:
        message = messaging.Message(
            notification=messaging.Notification(
                title="⚡ Krishna Config Notice",
                body=msg,
            ),
            topic="all_users",
        )
        messaging.send(message)
        await update.message.reply_text(f"📢 *Push broadcast dispatched:* {msg}")
    except Exception as e:
        await update.message.reply_text(f"Broadcast notice: {e}")


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
    try:
        docs = db.collection("users").limit(30).stream()
        lines = ["👥 *REGISTERED USERS & HARDWARE RIGS:*"]
        count = 0
        for doc in docs:
            count += 1
            d = doc.to_dict()
            email = d.get("email", "unknown")
            status = "✅ APPROVED" if d.get("status") == "approved" or d.get("verified") else ("❌ DECLINED" if d.get("status") == "rejected" else "⏳ PENDING")
            device = d.get("deviceName", d.get("deviceModel", "No Rig"))
            key = d.get("activationKey", "N/A")
            android = d.get("androidVersion", "N/A")
            lines.append(f"• `{email}` [{status}]\n  📱 Device: `{device}` | OS: `{android}`\n  🔑 Key: `{key}`\n")

        if count == 0:
            lines.append("_No registered users found yet._")

        text = "\n".join(lines)
    except Exception as e:
        text = f"❌ Error querying users: {e}"

    if update.callback_query:
        await update.callback_query.message.reply_text(text, parse_mode=ParseMode.MARKDOWN)
    else:
        await update.message.reply_text(text, parse_mode=ParseMode.MARKDOWN)


async def pending_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    try:
        docs = db.collection("payments").where("status", "==", "pending").stream()
        count = 0
        for doc in docs:
            count += 1
            d = doc.to_dict()
            uid = d.get("userId", "")
            text = (
                f"⏳ *PENDING ACTIVATION REQUEST*\n"
                f"━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                f"👤 *User:* `{d.get('email')}`\n"
                f"📱 *Device Name:* `{d.get('deviceName')}`\n"
                f"🤖 *Android Version:* `{d.get('androidVersion')}`\n"
                f"📱 *Model:* `{d.get('deviceModel')}`\n"
                f"⚙️ *OS Build:* `{d.get('osVersion')}`\n"
                f"💳 *UPI Ref:* `{d.get('upiRef')}`\n"
                f"💰 *Amount:* ₹`{d.get('amount')}`\n"
                f"🔑 *ACTIVATION KEY:* `{d.get('activationKey')}`\n"
                f"🆔 *UID:* `{uid}`\n"
                f"━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            )
            keyboard = InlineKeyboardMarkup([
                [
                    InlineKeyboardButton("✅ APPROVE", callback_data=f"approve:{uid}"),
                    InlineKeyboardButton("❌ DECLINE", callback_data=f"reject:{uid}"),
                ]
            ])
            screenshot_url = d.get("screenshotUrl")
            if screenshot_url:
                await update.message.reply_photo(photo=screenshot_url, caption=text, parse_mode=ParseMode.MARKDOWN, reply_markup=keyboard)
            else:
                await update.message.reply_text(text, parse_mode=ParseMode.MARKDOWN, reply_markup=keyboard)

        if count == 0:
            msg = "✅ No pending payments in queue."
            if update.callback_query:
                await update.callback_query.message.reply_text(msg)
            else:
                await update.message.reply_text(msg)
    except Exception as e:
        logger.error(f"Error in pending_command: {e}")
        err_msg = f"❌ Error loading pending payments: {e}"
        if update.callback_query:
            await update.callback_query.message.reply_text(err_msg)
        else:
            await update.message.reply_text(err_msg)


async def blacklist_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    if not is_admin(update.effective_user.id):
        return
    if not context.args:
        await update.message.reply_text("Usage: `/blacklist <uid>`", parse_mode=ParseMode.MARKDOWN)
        return
    uid = context.args[0]
    db.collection("users").document(uid).set({"verified": False, "status": "blacklisted"}, merge=True)
    await update.message.reply_text(f"🚫 *Device Blacklisted:* `{uid}`", parse_mode=ParseMode.MARKDOWN)


# Callback Query Handler for Inline Buttons (APPROVE / DECLINE)
async def button_callback(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    query = update.callback_query
    # Crucial: always answer callback query immediately so Telegram client stops spinning
    try:
        await query.answer()
    except Exception as e:
        logger.warning(f"Failed to answer callback query: {e}")

    data = query.data
    logger.info(f"Callback received: {data}")

    try:
        if data.startswith("approve:"):
            uid = data.split(":")[1]
            # Update Firestore records
            db.collection("users").document(uid).set({"verified": True, "status": "approved"}, merge=True)
            db.collection("payments").document(uid).set({"status": "approved"}, merge=True)

            status_suffix = "\n\n✅ *STATUS: APPROVED BY ADMIN (SAFE ZONE UNLOCKED)*"
            try:
                if query.message.caption:
                    await query.edit_message_caption(
                        caption=(query.message.caption or "") + status_suffix,
                        parse_mode=ParseMode.MARKDOWN
                    )
                elif query.message.text:
                    await query.edit_message_text(
                        text=(query.message.text or "") + status_suffix,
                        parse_mode=ParseMode.MARKDOWN
                    )
            except Exception as edit_err:
                logger.warning(f"Could not edit message text/caption: {edit_err}")
                # Fallback: send confirmation text reply
                await query.message.reply_text(
                    f"✅ *SUCCESS: USER APPROVED!*\n🆔 UID: `{uid}`\nSafe Zone has unlocked automatically in real-time.",
                    parse_mode=ParseMode.MARKDOWN
                )

        elif data.startswith("reject:"):
            uid = data.split(":")[1]
            # Update Firestore records
            db.collection("users").document(uid).set({"verified": False, "status": "rejected"}, merge=True)
            db.collection("payments").document(uid).set({"status": "rejected"}, merge=True)

            status_suffix = "\n\n❌ *STATUS: DECLINED BY ADMIN (ACCESS BLOCKED)*"
            try:
                if query.message.caption:
                    await query.edit_message_caption(
                        caption=(query.message.caption or "") + status_suffix,
                        parse_mode=ParseMode.MARKDOWN
                    )
                elif query.message.text:
                    await query.edit_message_text(
                        text=(query.message.text or "") + status_suffix,
                        parse_mode=ParseMode.MARKDOWN
                    )
            except Exception as edit_err:
                logger.warning(f"Could not edit message text/caption: {edit_err}")
                # Fallback: send confirmation text reply
                await query.message.reply_text(
                    f"❌ *USER DECLINED BY ADMIN!*\n🆔 UID: `{uid}`\nAccess locked on client device.",
                    parse_mode=ParseMode.MARKDOWN
                )

        elif data == "cmd_stats":
            await stats_command(update, context)
        elif data == "cmd_pending":
            await pending_command(update, context)
        elif data == "cmd_listusers":
            await listusers_command(update, context)

    except Exception as e:
        logger.error(f"Error handling callback {data}: {e}")
        try:
            await query.message.reply_text(f"⚠️ Action failed: {e}")
        except Exception:
            pass


def main():
    if not BOT_TOKEN:
        print("Error: TELEGRAM_BOT_TOKEN environment variable is not set.", file=sys.stderr)
        sys.exit(1)

    print(f"Starting Krishna Config Admin Bot... Admin Chat ID: {ADMIN_CHAT_ID}")
    app = Application.builder().token(BOT_TOKEN).build()

    app.add_handler(CommandHandler("start", start_command))
    app.add_handler(CommandHandler("stats", stats_command))
    app.add_handler(CommandHandler("pending", pending_command))
    app.add_handler(CommandHandler("approve", approve_command))
    app.add_handler(CommandHandler("decline", reject_command))
    app.add_handler(CommandHandler("reject", reject_command))
    app.add_handler(CommandHandler("listusers", listusers_command))
    app.add_handler(CommandHandler("setupi", setupi_command))
    app.add_handler(CommandHandler("setamount", setamount_command))
    app.add_handler(CommandHandler("setqr", setqr_command))
    app.add_handler(CommandHandler("setvideo", setvideo_command))
    app.add_handler(CommandHandler("broadcast", broadcast_command))
    app.add_handler(CommandHandler("deluser", deluser_command))
    app.add_handler(CommandHandler("blacklist", blacklist_command))
    app.add_handler(CallbackQueryHandler(button_callback))

    app.run_polling()


if __name__ == "__main__":
    main()
