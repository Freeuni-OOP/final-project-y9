const API = "/api/joker";

// --- State Variables ---
let selectedPlayerCount = "THREE";
let selectedRoundOption = "SHORT_8";
let selectedJokerAmount = 1;
let selectedAllowRandoms = false;

// --- Wizard State ---
let currentStep = 1;
const totalSteps = 4;

// --- Sub-View Switcher Logic ---
function switchView(viewId) {
    setMsg("", "");

    // დეაქტივაცია ყველა პანელის
    document.querySelectorAll('.view-panel').forEach(panel => {
        panel.classList.remove('active');
    });

    // გააქტიურება სასურველი პანელის
    const activePanel = document.getElementById(viewId);
    if (activePanel) {
        activePanel.classList.add('active');
    }

    // დინამიური სუბტიტრები
    const subtitle = document.getElementById('panel-subtitle');
    if (viewId === 'view-create') {
        subtitle.textContent = "ახალი თამაშის პარამეტრები";
        resetWizard(); // ყოველ შესვლაზე ვიზარდი იწყება თავიდან
    }
    else if (viewId === 'view-browse') {
        subtitle.textContent = "აქტიური საჯარო ოთახები";
        loadLobbies();
    }
    else if (viewId === 'view-join') subtitle.textContent = "შეუერთდით მეგობრის პარტიას";
    else subtitle.textContent = "აირჩიეთ მოქმედება გასაგრძელებლად";
}

// --- Wizard ნავიგაციის ფუნქციები ---
function updateWizardUI() {
    // 1. ნაბიჯების დამალვა/ჩვენება
    document.querySelectorAll(".wizard-step").forEach(step => {
        step.classList.remove("active");
        if (parseInt(step.dataset.step) === currentStep) {
            step.classList.add("active");
        }
    });

    // 2. პროგრესის ზოლის განახლება (25%, 50%, 75%, 100%)
    const progressFill = document.getElementById("wizardProgress");
    if (progressFill) {
        const percent = (currentStep / totalSteps) * 100;
        progressFill.style.width = `${percent}%`;
    }

    // 3. ღილაკების ტექსტების და მუშაობის მართვა
    const backBtn = document.getElementById("wizardBackBtn");
    const nextBtn = document.getElementById("wizardNextBtn");

    if (currentStep === 1) {
        backBtn.textContent = "← მთავარი"; // პირველ ნაბიჯზე უკან დაბრუნება მთავარ მენიუში აბრუნებს
    } else {
        backBtn.textContent = "← უკან";
    }

    if (currentStep === totalSteps) {
        nextBtn.textContent = "ოთახის შექმნა 🃏";
        nextBtn.className = "joker-btn joker-btn--primary"; // ბოლო ნაბიჯზე ხდება მთავარი აქტიური ღილაკი
    } else {
        nextBtn.textContent = "შემდეგი →";
        nextBtn.className = "joker-btn joker-btn--primary";
    }
}

function handleWizardNext() {
    if (currentStep < totalSteps) {
        currentStep++;
        updateWizardUI();
    } else {
        // თუ ბოლო ნაბიჯზე ვართ, პირდაპირ ვიძახებთ ოთახის შექმნის ფუნქციას
        createGame();
    }
}

function handleWizardBack() {
    if (currentStep > 1) {
        currentStep--;
        updateWizardUI();
    } else {
        switchView('view-main'); // თუ პირველზეა, აბრუნებს მთავარ გვერდზე
    }
}

function resetWizard() {
    currentStep = 1;
    updateWizardUI();
}

// --- Toggle configurations (შესწორებული ახალი სტრუქტურისთვის) ---
document.querySelectorAll(".wizard-step .joker-toggle-group").forEach(group => {
    const stepNum = parseInt(group.closest(".wizard-step").dataset.step);

    group.querySelectorAll(".joker-toggle").forEach(btn => {
        btn.addEventListener("click", () => {
            group.querySelectorAll(".joker-toggle").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            const val = btn.dataset.value;

            // ნაბიჯების მიხედვით ვანაწილებთ მნიშვნელობებს ცვლადებში
            if (stepNum === 1) selectedPlayerCount  = val;
            if (stepNum === 2) selectedRoundOption  = val;
            if (stepNum === 3) selectedJokerAmount  = parseInt(val);
            if (stepNum === 4) selectedAllowRandoms = val === "true";
        });
    });
});

// --- Action Logic: Create Game Room ---
async function createGame() {
    const token = localStorage.getItem("token");
    setMsg("", "");
    try {
        const res = await fetch(`${API}/create`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                playerCount:  selectedPlayerCount,
                roundOption:  selectedRoundOption,
                jokerAmount:  selectedJokerAmount,
                allowRandoms: selectedAllowRandoms
            })
        });
        if (!res.ok) {
            const err = await res.text();
            setMsg(err || "შეცდომა", "error");
            return;
        }
        const state = await res.json();
        const roomCode = state.room.roomId;
        window.location.href = `/joker/${roomCode}`;
    } catch (err) {
        console.error(err);
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- Action Logic: Join Room manually via Explicit Code ---
async function joinByCode() {
    const token = localStorage.getItem("token");
    const code = document.getElementById("roomCodeInput").value.trim().toUpperCase();
    if (!code) { setMsg("შეიყვანე ოთახის კოდი", "error"); return; }

    try {
        const res = await fetch(`${API}/${code}/join`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            const err = await res.text();
            setMsg(err || "შეცდომა", "error");
            return;
        }
        window.location.href = `/joker/${code}`;
    } catch (err) {
        console.error(err);
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- Action Logic: Load Open Public Lobbies ---
async function loadLobbies() {
    const token = localStorage.getItem("token");
    const list = document.getElementById("lobbiesList");
    list.innerHTML = `<p style="color:#888; text-align:center; font-size:0.85rem;">იტვირთება...</p>`;

    try {
        const res = await fetch(`${API}/lobbies`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("failed");
        const lobbies = await res.json();

        if (!lobbies || lobbies.length === 0) {
            list.innerHTML = `<p style="color:#888; text-align:center; font-size:0.85rem;">ღია ლობი არ არის</p>`;
            return;
        }

        list.innerHTML = "";
        lobbies.forEach(lobby => {
            const players = lobby.players || [];
            const config  = lobby.config || {};
            const roomCode = lobby.room?.roomId || "—";
            const filled   = players.length;
            const required = config.players || "?";
            const rounds   = config.totalRounds || "?";

            const item = document.createElement("div");
            item.className = "joker-lobby-item";
            item.setAttribute("data-code", roomCode.toUpperCase());
            item.innerHTML = `
                <div class="joker-lobby-info">
                    <div class="joker-lobby-code">🃏 ${roomCode}</div>
                    <div class="joker-lobby-meta">
                        ${filled}/${required} მოთამაშე &nbsp;·&nbsp; ${rounds} რაუნდი
                    </div>
                </div>
                <button class="joker-lobby-join" onclick="joinLobby('${roomCode}')">
                    შესვლა
                </button>
            `;
            list.appendChild(item);
        });
    } catch (err) {
        list.innerHTML = `<p style="color:var(--pink); text-align:center; font-size:0.85rem;">ვერ ჩაიტვირთა</p>`;
    }
}

// --- Quick Client-Side Filter for Scrolling Area ---
function filterLobbies() {
    const filterText = document.getElementById("searchLobbyInput").value.trim().toUpperCase();
    const items = document.querySelectorAll(".joker-lobby-item");

    items.forEach(item => {
        const itemCode = item.getAttribute("data-code") || "";
        if (itemCode.includes(filterText)) {
            item.style.display = "flex";
        } else {
            item.style.display = "none";
        }
    });
}

async function joinLobby(roomCode) {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API}/${roomCode}/join`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            const err = await res.text();
            setMsg(err || "შეცდომა", "error");
            return;
        }
        window.location.href = `/joker/${roomCode}`;
    } catch (err) {
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- UI Notification Helpers ---
function setMsg(text, type) {
    const el = document.getElementById("lobbyMsg");
    if(el) {
        el.textContent = text;
        el.className = "joker-msg" + (type ? " " + type : "");
    }
}

// --- Init Session Check ---
document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    if (!token) { window.location.href = "/login"; return; }
});