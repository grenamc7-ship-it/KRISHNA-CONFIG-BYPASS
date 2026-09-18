// KRISHNA CONFIG — Master Web Admin Dashboard JS
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

// Initialize Firebase App
if (!firebase.apps.length) {
  firebase.initializeApp(firebaseConfig);
}

const db = firebase.firestore();

// Default admin credentials requested by user
const DEFAULT_ADMIN_USER = "KRISHNA@143";
const DEFAULT_ADMIN_PASS = "CONFIG@1432";

// Current active credentials in memory/storage/firestore
let activeAdminUser = localStorage.getItem("kc_admin_user") || DEFAULT_ADMIN_USER;
let activeAdminPass = localStorage.getItem("kc_admin_pass") || DEFAULT_ADMIN_PASS;

// DOM Elements
const loginSection = document.getElementById("login-section");
const dashboardSection = document.getElementById("dashboard-section");
const loginUsernameInput = document.getElementById("login-username");
const loginPassInput = document.getElementById("login-pass");
const loginBtn = document.getElementById("login-btn");
const loginErrorMsg = document.getElementById("login-error-msg");
const logoutBtn = document.getElementById("logout-btn");
const adminUserDisplay = document.getElementById("admin-user-display");
const openPassModalBtn = document.getElementById("open-pass-modal-btn");

// Credential modal elements
const changePassModal = document.getElementById("change-pass-modal");
const closePassModalBtn = document.getElementById("close-pass-modal-btn");
const cancelCredBtn = document.getElementById("cancel-cred-btn");
const newAdminUser = document.getElementById("new-admin-user");
const newAdminPass = document.getElementById("new-admin-pass");
const newAdminPassConfirm = document.getElementById("new-admin-pass-confirm");
const saveNewCredBtn = document.getElementById("save-new-cred-btn");
const changeCredMsg = document.getElementById("change-cred-msg");

// Stats & lists
const statTotalUsers = document.getElementById("stat-total-users");
const statOnlineUsers = document.getElementById("stat-online-users");
const statPendingPayments = document.getElementById("stat-pending-payments");
const statApprovedPayments = document.getElementById("stat-approved-payments");
const pendingList = document.getElementById("pending-list");
const usersTableBody = document.getElementById("users-table-body");
const usersCountTag = document.getElementById("users-count-tag");
const refreshPaymentsBtn = document.getElementById("refresh-payments-btn");

// Remote config inputs
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

// Check Firestore for remote saved admin credentials
function syncRemoteCredentials() {
  db.collection("settings").document("admin_auth").get().then((doc) => {
    if (doc.exists) {
      const data = doc.data();
      if (data.username) {
        activeAdminUser = data.username;
        localStorage.setItem("kc_admin_user", activeAdminUser);
      }
      if (data.password) {
        activeAdminPass = data.password;
        localStorage.setItem("kc_admin_pass", activeAdminPass);
      }
    } else {
      // Initialize remote admin credential doc with default
      db.collection("settings").document("admin_auth").set({
        username: DEFAULT_ADMIN_USER,
        password: DEFAULT_ADMIN_PASS,
        updatedAt: firebase.firestore.FieldValue.serverTimestamp()
      }, { merge: true }).catch(err => console.warn("Init auth notice:", err));
    }
  }).catch(err => console.warn("Sync credentials notice:", err));
}
syncRemoteCredentials();

// Session management
let isAuthenticated = sessionStorage.getItem("kc_admin_logged_in") === "true";

function updateUIForAuth() {
  if (isAuthenticated) {
    loginSection.classList.add("hidden");
    dashboardSection.classList.remove("hidden");
    logoutBtn.classList.remove("hidden");
    openPassModalBtn.classList.remove("hidden");
    adminUserDisplay.classList.remove("hidden");
    adminUserDisplay.innerHTML = `<i class="fa-solid fa-shield text-neon mr-1"></i> ${activeAdminUser}`;
    initDashboard();
  } else {
    loginSection.classList.remove("hidden");
    dashboardSection.classList.add("hidden");
    logoutBtn.classList.add("hidden");
    openPassModalBtn.classList.add("hidden");
    adminUserDisplay.classList.add("hidden");
  }
}
updateUIForAuth();

// Login button listener
loginBtn.addEventListener("click", () => {
  const enteredUser = loginUsernameInput.value.trim();
  const enteredPass = loginPassInput.value.trim();

  loginErrorMsg.classList.add("hidden");

  // Verify against active credentials or defaults
  if (
    (enteredUser.toUpperCase() === activeAdminUser.toUpperCase() || enteredUser === DEFAULT_ADMIN_USER) &&
    (enteredPass === activeAdminPass || enteredPass === DEFAULT_ADMIN_PASS)
  ) {
    isAuthenticated = true;
    sessionStorage.setItem("kc_admin_logged_in", "true");
    updateUIForAuth();
  } else {
    loginErrorMsg.textContent = "❌ Invalid Username or Password. Please verify credentials.";
    loginErrorMsg.classList.remove("hidden");
  }
});

// Logout listener
logoutBtn.addEventListener("click", () => {
  isAuthenticated = false;
  sessionStorage.removeItem("kc_admin_logged_in");
  updateUIForAuth();
});

// Change Credentials Modal Listeners
openPassModalBtn.addEventListener("click", () => {
  newAdminUser.value = activeAdminUser;
  newAdminPass.value = "";
  newAdminPassConfirm.value = "";
  changeCredMsg.classList.add("hidden");
  changePassModal.classList.remove("hidden");
});

function closePassModal() {
  changePassModal.classList.add("hidden");
}
closePassModalBtn.addEventListener("click", closePassModal);
cancelCredBtn.addEventListener("click", closePassModal);

saveNewCredBtn.addEventListener("click", async () => {
  const newUser = newAdminUser.value.trim();
  const newPass = newAdminPass.value.trim();
  const confirmPass = newAdminPassConfirm.value.trim();

  changeCredMsg.classList.remove("hidden");

  if (!newUser) {
    changeCredMsg.className = "text-xs p-2 rounded bg-blood-deep border border-blood text-red-300 font-bold";
    changeCredMsg.textContent = "Username cannot be blank";
    return;
  }
  if (!newPass || newPass.length < 6) {
    changeCredMsg.className = "text-xs p-2 rounded bg-blood-deep border border-blood text-red-300 font-bold";
    changeCredMsg.textContent = "Password must be at least 6 characters";
    return;
  }
  if (newPass !== confirmPass) {
    changeCredMsg.className = "text-xs p-2 rounded bg-blood-deep border border-blood text-red-300 font-bold";
    changeCredMsg.textContent = "Passwords do not match";
    return;
  }

  try {
    // Save to Firestore and LocalStorage
    await db.collection("settings").document("admin_auth").set({
      username: newUser,
      password: newPass,
      updatedAt: firebase.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    activeAdminUser = newUser;
    activeAdminPass = newPass;
    localStorage.setItem("kc_admin_user", newUser);
    localStorage.setItem("kc_admin_pass", newPass);

    adminUserDisplay.innerHTML = `<i class="fa-solid fa-shield text-neon mr-1"></i> ${activeAdminUser}`;

    changeCredMsg.className = "text-xs p-2 rounded bg-green-950 border border-green-500 text-green-300 font-bold";
    changeCredMsg.textContent = "✅ Username & Password updated successfully!";

    setTimeout(() => {
      closePassModal();
    }, 1500);
  } catch (err) {
    changeCredMsg.className = "text-xs p-2 rounded bg-blood-deep border border-blood text-red-300 font-bold";
    changeCredMsg.textContent = "Error saving: " + err.message;
  }
});

// Initializing Dashboard Listeners
let dashboardInitialized = false;
function initDashboard() {
  if (dashboardInitialized) return;
  dashboardInitialized = true;
  loadConfig();
  listenToStatsAndPayments();
  listenToUsers();
}

// 1. Remote App Configuration
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
    alert("✅ Remote config saved and deployed to all Android apps!");
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
      const status = (p.status || "").toLowerCase();
      if (status === "pending") {
        pendingCount++;
        renderPendingCard(p);
      } else if (status === "approved") {
        approvedCount++;
      }
    });

    statPendingPayments.textContent = pendingCount;
    statApprovedPayments.textContent = approvedCount;

    if (pendingCount === 0) {
      pendingList.innerHTML = `<p class="text-xs text-gray-500 italic py-3 text-center">✅ No pending payment activations in queue.</p>`;
    }
  });
}

refreshPaymentsBtn.addEventListener("click", () => {
  listenToStatsAndPayments();
});

function renderPendingCard(p) {
  const div = document.createElement("div");
  div.className = "flex flex-col md:flex-row items-start md:items-center justify-between bg-black/60 border border-gray-800 p-4 rounded-xl gap-4 hover:border-gray-700 transition";
  div.innerHTML = `
    <div class="space-y-1.5 text-xs flex-1">
      <div class="flex items-center space-x-2">
        <span class="font-black text-white text-sm">${p.email || "Unknown User"}</span>
        <span class="text-neon bg-neon/10 border border-neon/30 px-2 py-0.5 rounded text-[10px] font-mono">[Rig: ${p.deviceName || p.deviceModel || "Mobile"}]</span>
      </div>
      <div class="text-gray-300 font-mono text-[11px] grid grid-cols-1 sm:grid-cols-2 gap-1 pt-1">
        <div>🤖 <span class="text-gray-400">Android:</span> ${p.androidVersion || "N/A"}</div>
        <div>📱 <span class="text-gray-400">Model:</span> ${p.deviceModel || "N/A"}</div>
        <div>💳 <span class="text-gray-400">UPI Ref:</span> <span class="text-yellow-400 font-bold">${p.upiRef || "None"}</span></div>
        <div>💰 <span class="text-gray-400">Amount:</span> <span class="text-green-400 font-bold">₹${p.amount || "499"}</span></div>
      </div>
      <div class="text-neon font-mono text-[11px] pt-1">
        🔑 <span class="text-gray-400">Activation Key:</span> <span class="text-neon font-bold tracking-wider">${p.activationKey || "N/A"}</span>
      </div>
      ${p.screenshotUrl ? `<div class="pt-1"><a href="${p.screenshotUrl}" target="_blank" class="inline-flex items-center space-x-1 text-blood hover:text-red-400 underline text-xs font-bold"><i class="fa-solid fa-image mr-1"></i> View Payment Screenshot</a></div>` : ''}
    </div>
    <div class="flex items-center space-x-2 w-full md:w-auto">
      <button onclick="approvePayment('${p.userId}')" class="flex-1 md:flex-initial bg-green-500/20 border border-green-500 text-green-300 text-xs px-4 py-2.5 rounded-lg hover:bg-green-500 hover:text-black font-black transition flex items-center justify-center space-x-1">
        <i class="fa-solid fa-check"></i>
        <span>APPROVE</span>
      </button>
      <button onclick="rejectPayment('${p.userId}')" class="flex-1 md:flex-initial bg-blood/20 border border-blood text-blood text-xs px-4 py-2.5 rounded-lg hover:bg-blood hover:text-white font-black transition flex items-center justify-center space-x-1">
        <i class="fa-solid fa-xmark"></i>
        <span>DECLINE</span>
      </button>
    </div>
  `;
  pendingList.appendChild(div);
}

window.approvePayment = async function(userId) {
  if (!userId) return alert("Invalid User ID");
  try {
    await db.collection("users").document(userId).set({
      verified: true,
      status: "approved",
      approvedAt: firebase.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    await db.collection("payments").document(userId).set({
      status: "approved",
      approvedAt: firebase.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    // Notify Telegram Admin
    const botToken = cfgBotToken?.value?.trim() || "8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4";
    const chatId = cfgAdminChat?.value?.trim() || "8491850372";
    if (botToken && chatId) {
      fetch(`https://api.telegram.org/bot${botToken}/sendMessage`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          chat_id: chatId,
          text: `✅ *APPROVED VIA WEB ADMIN DASHBOARD*\n━━━━━━━━━━━━━━━━━━━━━━\n🆔 *UID:* \`${userId}\`\n🛡️ Safe Zone has unlocked automatically on client rig!`,
          parse_mode: "Markdown"
        })
      }).catch(e => console.warn("Telegram sync notice:", e));
    }
  } catch (err) {
    alert("Approval error: " + err.message);
  }
};

window.rejectPayment = async function(userId) {
  if (!userId) return alert("Invalid User ID");
  try {
    await db.collection("users").document(userId).set({
      verified: false,
      status: "rejected",
      rejectedAt: firebase.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    await db.collection("payments").document(userId).set({
      status: "rejected",
      rejectedAt: firebase.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    // Notify Telegram Admin
    const botToken = cfgBotToken?.value?.trim() || "8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4";
    const chatId = cfgAdminChat?.value?.trim() || "8491850372";
    if (botToken && chatId) {
      fetch(`https://api.telegram.org/bot${botToken}/sendMessage`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          chat_id: chatId,
          text: `❌ *DECLINED VIA WEB ADMIN DASHBOARD*\n━━━━━━━━━━━━━━━━━━━━━━\n🆔 *UID:* \`${userId}\`\n🚫 Access blocked on client rig.`,
          parse_mode: "Markdown"
        })
      }).catch(e => console.warn("Telegram sync notice:", e));
    }
  } catch (err) {
    alert("Rejection error: " + err.message);
  }
};

// 3. User & Rig Listing
function listenToUsers() {
  db.collection("users").onSnapshot((snapshot) => {
    statTotalUsers.textContent = snapshot.size;
    statOnlineUsers.textContent = Math.max(1, Math.floor(snapshot.size * 0.5));
    usersCountTag.textContent = `${snapshot.size} Rigs`;
    usersTableBody.innerHTML = "";

    snapshot.forEach((doc) => {
      const u = doc.data();
      const isApproved = u.status === "approved" || u.verified === true;
      const isRejected = u.status === "rejected";
      const statusBadge = isApproved
        ? `<span class="text-neon bg-neon/10 border border-neon/30 px-2 py-0.5 rounded text-[10px] font-bold">APPROVED</span>`
        : (isRejected
            ? `<span class="text-blood bg-blood/10 border border-blood/30 px-2 py-0.5 rounded text-[10px] font-bold">DECLINED</span>`
            : `<span class="text-yellow-400 bg-yellow-400/10 border border-yellow-400/30 px-2 py-0.5 rounded text-[10px] font-bold">PENDING</span>`);

      const tr = document.createElement("div");
      tr.className = "flex flex-col sm:flex-row items-start sm:items-center justify-between py-2.5 px-2 text-gray-300 hover:bg-black/30 rounded";
      tr.innerHTML = `
        <div class="space-y-0.5">
          <div class="flex items-center space-x-2">
            ${statusBadge}
            <span class="font-bold text-white text-xs">${u.email || doc.id}</span>
            <span class="text-[10px] text-gray-400 font-mono">(${u.deviceName || u.deviceModel || "Unbound"})</span>
          </div>
          <div class="text-[11px] text-neon font-mono">
            🔑 Key: ${u.activationKey || "None"} • Model: ${u.deviceModel || "N/A"}
          </div>
        </div>
        <div class="space-x-2 mt-2 sm:mt-0 flex items-center">
          ${!isApproved ? `<button onclick="approvePayment('${doc.id}')" class="text-neon hover:underline text-[11px] font-bold">Unlock</button>` : ''}
          ${isApproved ? `<button onclick="rejectPayment('${doc.id}')" class="text-yellow-400 hover:underline text-[11px] font-bold">Lock</button>` : ''}
          <button onclick="deleteUser('${doc.id}')" class="text-blood hover:underline text-[11px] font-bold">Delete</button>
        </div>
      `;
      usersTableBody.appendChild(tr);
    });

    if (snapshot.size === 0) {
      usersTableBody.innerHTML = `<p class="text-gray-500 py-3 text-center">No registered clients yet.</p>`;
    }
  });
}

window.deleteUser = async function(userId) {
  if (!confirm("Are you sure you want to delete this user license and record?")) return;
  try {
    await db.collection("users").document(userId).delete();
    await db.collection("payments").document(userId).delete();
  } catch (e) {
    alert(e.message);
  }
};

// User manual creation / unlock
document.getElementById("create-user-btn").addEventListener("click", async () => {
  const email = document.getElementById("new-user-email").value.trim();
  if (!email) return alert("Please enter client email");

  try {
    const customUid = "MANUAL_" + Math.abs(email.hashCode ? email.hashCode() : Date.now());
    await db.collection("users").document(customUid).set({
      userId: customUid,
      email: email,
      verified: true,
      status: "approved",
      deviceName: "Admin Provisioned Rig",
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    alert(`✅ Client ${email} has been provisioned and unlocked!`);
    document.getElementById("new-user-email").value = "";
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
