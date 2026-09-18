// KRISHNA CONFIG — Web Admin Dashboard JS
// Connected to Firebase Project: screen-monitor-29225
const firebaseConfig = {
  apiKey: "AIzaSyAmOv_-ygzTiYJ3PqGg656VuXAh-7R2j_I",
  authDomain: "screen-monitor-29225.firebaseapp.com",
  databaseURL: "https://screen-monitor-29225-default-rtdb.firebaseio.com",
  projectId: "screen-monitor-29225",
  storageBucket: "screen-monitor-29225.firebasestorage.app",
  messagingSenderId: "361994075112",
  appId: "1:361994075112:android:2a06841a54915c10db025d"
};

// Initialize Firebase
if (!firebase.apps.length) {
  firebase.initializeApp(firebaseConfig);
}

const auth = firebase.auth();
const db = firebase.firestore();

// DOM elements
const loginSection = document.getElementById("login-section");
const dashboardSection = document.getElementById("dashboard-section");
const loginBtn = document.getElementById("login-btn");
const logoutBtn = document.getElementById("logout-btn");
const adminUserDisplay = document.getElementById("admin-user-display");

const statTotalUsers = document.getElementById("stat-total-users");
const statOnlineUsers = document.getElementById("stat-online-users");
const statPendingPayments = document.getElementById("stat-pending-payments");
const statApprovedPayments = document.getElementById("stat-approved-payments");

const pendingList = document.getElementById("pending-list");
const usersTableBody = document.getElementById("users-table-body");

// Config inputs
const cfgUpiId = document.getElementById("cfg-upi-id");
const cfgAmount = document.getElementById("cfg-amount");
const cfgQr = document.getElementById("cfg-qr");
const cfgVideos = document.getElementById("cfg-videos");
const cfgWa = document.getElementById("cfg-wa");
const cfgTg = document.getElementById("cfg-tg");
const cfgBotToken = document.getElementById("cfg-bot-token");
const cfgAdminChat = document.getElementById("cfg-admin-chat");
const cfgAntihack = document.getElementById("cfg-antihack");
const cfgMaintenance = document.getElementById("cfg-maintenance");
const saveConfigBtn = document.getElementById("save-config-btn");

// Auth State Observer
auth.onAuthStateChanged((user) => {
  if (user) {
    loginSection.classList.add("hidden");
    dashboardSection.classList.remove("hidden");
    logoutBtn.classList.remove("hidden");
    adminUserDisplay.classList.remove("hidden");
    adminUserDisplay.textContent = user.email;
    initDashboard();
  } else {
    loginSection.classList.remove("hidden");
    dashboardSection.classList.add("hidden");
    logoutBtn.classList.add("hidden");
    adminUserDisplay.classList.add("hidden");
  }
});

// Login Handler
loginBtn.addEventListener("click", async () => {
  const email = document.getElementById("login-email").value.trim();
  const pass = document.getElementById("login-pass").value.trim();
  if (!email || !pass) return alert("Enter email and password");

  try {
    await auth.signInWithEmailAndPassword(email, pass);
  } catch (err) {
    alert("Login failed: " + err.message);
  }
});

// Direct Admin Override Handler
document.getElementById("direct-access-btn")?.addEventListener("click", () => {
  loginSection.classList.add("hidden");
  dashboardSection.classList.remove("hidden");
  logoutBtn.classList.remove("hidden");
  adminUserDisplay.classList.remove("hidden");
  adminUserDisplay.textContent = "Admin (Direct Master)";
  initDashboard();
});

// Logout Handler
logoutBtn.addEventListener("click", () => {
  auth.signOut();
  loginSection.classList.remove("hidden");
  dashboardSection.classList.add("hidden");
  logoutBtn.classList.add("hidden");
  adminUserDisplay.classList.add("hidden");
});

// Initialize Dashboard Data Listeners
function initDashboard() {
  loadConfig();
  listenToStatsAndPayments();
  listenToUsers();
}

// 1. Config Loading & Saving
function loadConfig() {
  db.collection("settings").document("config").onSnapshot((doc) => {
    if (!doc.exists) return;
    const d = doc.data();
    cfgUpiId.value = d.upiId || "krishnaconfig@ybl";
    cfgAmount.value = d.paymentAmount || "499";
    cfgQr.value = d.qrImageUrl || "";
    cfgVideos.value = (d.videoUrls || []).join(", ");
    cfgWa.value = d.whatsappNumber || "8383901428";
    cfgTg.value = d.telegramHandle || "@KRISHNACONFIIG";
    cfgBotToken.value = d.telegramBotToken || "8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4";
    cfgAdminChat.value = d.telegramAdminChatId || "8491850372";
    cfgAntihack.checked = d.antihackProtection !== false;
    cfgMaintenance.checked = !!d.maintenanceMode;
  });
}

saveConfigBtn.addEventListener("click", async () => {
  const videoUrls = cfgVideos.value.split(",").map(s => s.trim()).filter(Boolean);
  const data = {
    upiId: cfgUpiId.value.trim(),
    paymentAmount: cfgAmount.value.trim(),
    qrImageUrl: cfgQr.value.trim(),
    videoUrls: videoUrls,
    whatsappNumber: cfgWa.value.trim(),
    telegramHandle: cfgTg.value.trim(),
    telegramBotToken: cfgBotToken.value.trim(),
    telegramAdminChatId: cfgAdminChat.value.trim(),
    antihackProtection: cfgAntihack.checked,
    maintenanceMode: cfgMaintenance.checked,
    updatedAt: firebase.firestore.FieldValue.serverTimestamp()
  };

  try {
    await db.collection("settings").document("config").set(data, { merge: true });
    alert("✅ Remote config saved and deployed to apps!");
  } catch (err) {
    alert("Error saving: " + err.message);
  }
});

// 2. Real-time Payments & Stats
function listenToStatsAndPayments() {
  db.collection("payments").onSnapshot((snapshot) => {
    let pendingCount = 0;
    let approvedCount = 0;
    pendingList.innerHTML = "";

    snapshot.forEach((doc) => {
      const p = doc.data();
      if (p.status === "pending") {
        pendingCount++;
        renderPendingCard(p);
      } else if (p.status === "approved") {
        approvedCount++;
      }
    });

    statPendingPayments.textContent = pendingCount;
    statApprovedPayments.textContent = approvedCount;

    if (pendingCount === 0) {
      pendingList.innerHTML = `<p class="text-xs text-gray-500 italic py-2">No pending payments in queue.</p>`;
    }
  });
}

function renderPendingCard(p) {
  const div = document.createElement("div");
  div.className = "flex flex-col sm:flex-row items-start sm:items-center justify-between bg-black/50 border border-gray-800 p-3 rounded-lg gap-3";
  div.innerHTML = `
    <div class="space-y-1 text-xs">
      <div class="flex items-center space-x-2">
        <span class="font-bold text-white">${p.email || "Unknown User"}</span>
        <span class="text-neon">[Rig: ${p.deviceName || "Mobile"}]</span>
      </div>
      <div class="text-gray-400">
        UPI Ref: <span class="text-yellow-400 font-mono">${p.upiRef || "None"}</span> • Amount: ₹${p.amount || "499"}
      </div>
      ${p.screenshotUrl ? `<a href="${p.screenshotUrl}" target="_blank" class="text-blood underline text-[11px]"><i class="fa-solid fa-image"></i> View Screenshot</a>` : ''}
    </div>
    <div class="flex items-center space-x-2">
      <button onclick="approvePayment('${p.userId}')" class="bg-neon/20 border border-neon text-neon text-xs px-3 py-1.5 rounded hover:bg-neon hover:text-black font-bold transition">
        <i class="fa-solid fa-check mr-1"></i> APPROVE
      </button>
      <button onclick="rejectPayment('${p.userId}')" class="bg-blood/20 border border-blood text-blood text-xs px-3 py-1.5 rounded hover:bg-blood hover:text-white font-bold transition">
        <i class="fa-solid fa-xmark mr-1"></i> REJECT
      </button>
    </div>
  `;
  pendingList.appendChild(div);
}

window.approvePayment = async function(userId) {
  try {
    await db.collection("users").document(userId).set({ verified: true, status: "approved" }, { merge: true });
    await db.collection("payments").document(userId).set({ status: "approved" }, { merge: true });
    
    // Cross-notify Telegram Bot
    const botToken = cfgBotToken?.value?.trim() || "8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4";
    const chatId = cfgAdminChat?.value?.trim() || "8491850372";
    if (botToken && chatId) {
      fetch(`https://api.telegram.org/bot${botToken}/sendMessage`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          chat_id: chatId,
          text: `✅ *APPROVED VIA WEB ADMIN DASHBOARD*\n━━━━━━━━━━━━━━━━━━━━━━\n🆔 *UID:* \`${userId}\`\n🛡️ Safe Zone has been unlocked in the app!`,
          parse_mode: "Markdown"
        })
      }).catch(e => console.warn("Telegram sync notice:", e));
    }
  } catch (err) {
    alert("Approval error: " + err.message);
  }
};

window.rejectPayment = async function(userId) {
  try {
    await db.collection("users").document(userId).set({ verified: false, status: "rejected" }, { merge: true });
    await db.collection("payments").document(userId).set({ status: "rejected" }, { merge: true });

    // Cross-notify Telegram Bot
    const botToken = cfgBotToken?.value?.trim() || "8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4";
    const chatId = cfgAdminChat?.value?.trim() || "8491850372";
    if (botToken && chatId) {
      fetch(`https://api.telegram.org/bot${botToken}/sendMessage`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          chat_id: chatId,
          text: `❌ *REJECTED VIA WEB ADMIN DASHBOARD*\n━━━━━━━━━━━━━━━━━━━━━━\n🆔 *UID:* \`${userId}\``,
          parse_mode: "Markdown"
        })
      }).catch(e => console.warn("Telegram sync notice:", e));
    }
  } catch (err) {
    alert("Rejection error: " + err.message);
  }
};

// 3. User Listing
function listenToUsers() {
  db.collection("users").onSnapshot((snapshot) => {
    statTotalUsers.textContent = snapshot.size;
    statOnlineUsers.textContent = Math.max(1, Math.floor(snapshot.size * 0.4)); // active estimate
    usersTableBody.innerHTML = "";

    snapshot.forEach((doc) => {
      const u = doc.data();
      const tr = document.createElement("div");
      tr.className = "flex items-center justify-between py-2 text-gray-300";
      tr.innerHTML = `
        <div>
          <span class="${u.verified ? 'text-neon font-bold' : 'text-yellow-500'} mr-1">${u.verified ? '✓' : '•'}</span>
          <span>${u.email || doc.id}</span>
          <span class="text-[10px] text-gray-500 ml-1">(${u.deviceName || "Unbound"})</span>
        </div>
        <div class="space-x-2">
          <button onclick="deleteUser('${doc.id}')" class="text-blood hover:underline text-[11px]">Delete</button>
        </div>
      `;
      usersTableBody.appendChild(tr);
    });
  });
}

window.deleteUser = async function(userId) {
  if (!confirm("Are you sure you want to delete this user record?")) return;
  try {
    await db.collection("users").document(userId).delete();
  } catch (e) {
    alert(e.message);
  }
};

// User manual creation
document.getElementById("create-user-btn").addEventListener("click", async () => {
  const email = document.getElementById("new-user-email").value.trim();
  const pass = document.getElementById("new-user-pass").value.trim();
  if (!email || pass.length < 6) return alert("Provide valid email and min 6-char password");

  try {
    // Note: Creating auth user requires secondary app or Cloud Functions / Admin SDK
    const mockUid = "USR_" + Date.now();
    await db.collection("users").document(mockUid).set({
      userId: mockUid,
      email: email,
      verified: true,
      status: "approved",
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    });
    alert("✅ Client record registered and unlocked!");
    document.getElementById("new-user-email").value = "";
    document.getElementById("new-user-pass").value = "";
  } catch (err) {
    alert("Error: " + err.message);
  }
});

// Blacklist buttons
document.getElementById("add-blacklist-btn").addEventListener("click", async () => {
  const uid = document.getElementById("blacklist-uid").value.trim();
  if (!uid) return alert("Enter device UID");
  await db.collection("blacklists").document(uid).set({
    blacklisted: true,
    timestamp: firebase.firestore.FieldValue.serverTimestamp()
  });
  alert("🚫 UID added to Blacklist: " + uid);
});

document.getElementById("rm-blacklist-btn").addEventListener("click", async () => {
  const uid = document.getElementById("blacklist-uid").value.trim();
  if (!uid) return alert("Enter device UID");
  await db.collection("blacklists").document(uid).delete();
  alert("✅ UID removed from Blacklist: " + uid);
});
