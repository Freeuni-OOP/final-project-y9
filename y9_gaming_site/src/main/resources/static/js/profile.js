const API_BASE = "/api/users";

const avatarImg = document.getElementById("avatar-img");
const usernameDisplay = document.getElementById("username-display");
const errorMessage = document.getElementById("error-message");
const uploadSection = document.getElementById("avatar-upload-section");
const avatarForm = document.getElementById("avatar-form");
const avatarInput = document.getElementById("avatar-input");
const uploadStatus = document.getElementById("upload-status");

function showError(message) {
    errorMessage.textContent = message;
    errorMessage.hidden = false;
}

// Format seconds into readable hours/minutes format
function formatPlaytime(totalSeconds) {
    if (!totalSeconds || totalSeconds < 60) return `${totalSeconds || 0}s`;
    const mins = Math.floor(totalSeconds / 60);
    if (mins < 60) return `${mins}m`;
    const hrs = Math.floor(mins / 60);
    const remainingMins = mins % 60;
    return remainingMins > 0 ? `${hrs}h ${remainingMins}m` : `${hrs}h`;
}

async function loadProfile() {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/login";
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/${userId}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            showError("Could not load this profile.");
            return;
        }

        const profile = await response.json();

        usernameDisplay.textContent = profile.username;
        if (profile.avatarUrl) {
            avatarImg.src = profile.avatarUrl;
        }

        const loggedInUsername = localStorage.getItem("username");
        const navUsername = document.getElementById("nav-username");
        const navAvatar = document.getElementById("nav-avatar");

        if (navUsername) {
            navUsername.textContent = loggedInUsername || "...";
        }
        if (navAvatar && loggedInUsername && loggedInUsername === profile.username && profile.avatarUrl) {
            navAvatar.src = profile.avatarUrl;
        }

        if (loggedInUsername && loggedInUsername === profile.username) {
            uploadSection.hidden = false;
        }
    } catch (error) {
        showError("Connection failed.");
    }
}

// NEW: Load Analytics Data (Top 3 Games & Top 5 Categories)
async function loadGameAnalytics() {
    const token = localStorage.getItem("token");
    if (!token) return;

    const headers = {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
    };

    // Pull Top 3 Favorite Games
    try {
        const gamesRes = await fetch("/api/games/my-top-3", { headers });
        if (gamesRes.ok) {
            const games = await gamesRes.json();
            renderTopGames(games);
        }
    } catch (err) {
        console.error("Error loading top games:", err);
    }

    // Pull Top 5 Favorite Categories
    try {
        const catsRes = await fetch("/api/games/my-top-categories", { headers });
        if (catsRes.ok) {
            const categories = await catsRes.json();
            renderTopCategories(categories);
        }
    } catch (err) {
        console.error("Error loading top categories:", err);
    }
}

function renderTopGames(games) {
    const container = document.getElementById("top-games-container");
    if (!container) return;

    if (!games || games.length === 0) {
        container.innerHTML = `<p class="ach-empty">No games tracked yet.</p>`;
        return;
    }

    container.innerHTML = games.map((g, index) => {
        let rankClass = "rank-silver";
        if (index === 0) rankClass = "rank-gold";
        if (index === 1) rankClass = "rank-platinum";

        // Clicking wraps action to launch the game dashboard immediately
        return `
            <div class="stat-clickable-item" onclick="window.location.href='/games?play=${encodeURIComponent(g.gameTitle)}'">
                <div class="stat-main-info">
                    <span class="stat-item-title">${escapeHtml(g.gameTitle)}</span>
                    <span class="stat-item-subtitle">Playtime: ${formatPlaytime(g.totalTimeSeconds)}</span>
                </div>
                <span class="badge ${rankClass}">#${index + 1}</span>
            </div>
        `;
    }).join("");
}

function renderTopCategories(categories) {
    const container = document.getElementById("top-categories-container");
    if (!container) return;

    if (!categories || categories.length === 0) {
        container.innerHTML = `<p class="ach-empty">No categories recorded.</p>`;
        return;
    }

    container.innerHTML = categories.map(c => `
        <div class="stat-clickable-item" onclick="window.location.href='/games?category=${encodeURIComponent(c.category)}'">
            <div class="stat-main-info">
                <span class="stat-item-title">${escapeHtml(c.category)}</span>
                <span class="stat-item-subtitle">Total Accumulated Time</span>
            </div>
            <span class="rarest-count">${formatPlaytime(c.totalTimeSeconds)}</span>
        </div>
    `).join("");
}

// shows selected files name
avatarInput.addEventListener("change", function () {
    if (avatarInput.files && avatarInput.files[0]) {
        uploadStatus.textContent = avatarInput.files[0].name;
    } else {
        uploadStatus.textContent = "No file selected.";
    }
});

// handles image upload
avatarForm.addEventListener("submit", function (event) {
    event.preventDefault();

    const file = avatarInput.files[0];
    if (!file) {
        uploadStatus.textContent = "Choose a photo first.";
        return;
    }

    const token = localStorage.getItem("token");
    const formData = new FormData();
    formData.append("avatar", file);

    uploadStatus.textContent = "Uploading...";

    fetch(`${API_BASE}/avatar`, {
        method: "POST",
        headers: { "Authorization": "Bearer " + token },
        body: formData
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (text) {
                    throw new Error(text || "Upload failed.");
                });
            }
            return response.json();
        })
        .then(function (result) {
            avatarImg.src = result.avatarUrl;

            // Also update the navbar avatar simultaneously
            const navAvatar = document.getElementById("nav-avatar");
            if (navAvatar) {
                navAvatar.src = result.avatarUrl;
            }

            uploadStatus.textContent = "Avatar updated successfully!";
        })
        .catch(function (err) {
            uploadStatus.textContent = err.message;
        });
});

async function logout() {
    try {
        await fetch("/api/users/logout", { method: "POST" });
    } catch (e) {
        console.error("Backend logout clean call skipped.");
    }
    localStorage.clear();
    window.location.href = "/login";
}

function escapeHtml(s) {
    return (s || "").replace(/[&<>"']/g, c =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

async function loadAchievements() {
    const token = localStorage.getItem("token");
    if (!token) return;
    const headers = { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" };
    try {
        const [rarestRes, allRes] = await Promise.all([
            fetch(`/achievements/${userId}/rarest?limit=3`, { headers }),
            fetch(`/achievements/${userId}/view`, { headers })
        ]);
        if (rarestRes.ok) renderRarest(await rarestRes.json());
        if (allRes.ok) renderAllAchievements(await allRes.json());
    } catch (err) {
        console.error("Could not load achievements:", err);
    }
}

function renderRarest(items) {
    const el = document.getElementById("rarest-achievements");
    if (!el) return;
    if (!items || items.length === 0) { el.innerHTML = ""; return; } // blank when none
    el.innerHTML = items.map(a => `
        <div class="rarest-item">
            <div class="rarest-info">
                <span class="rarest-name">${escapeHtml(a.name)}</span>
                <span class="rarest-desc">${escapeHtml(a.description || "")}</span>
            </div>
                <span class="rarest-count" title="players who have this">${a.earnedCount}</span>
        </div>`).join("");
}

function renderAllAchievements(items) {
    const el = document.getElementById("achievements-grid");
    if (!el) return;
    if (!items || items.length === 0) {
        el.innerHTML = `<p class="ach-empty">No achievements yet — go play!</p>`;
        return;
    }
    el.innerHTML = items.map(a => `
        <div class="achievement-badge unlocked" title="${escapeHtml(a.description || "")}">
            <span class="badge-title">${escapeHtml(a.name)}</span>
        </div>`).join("");
}

let myId = null;
let friendshipStatus = null;

async function loadFriendSection(){
    const token = localStorage.getItem("token");
    if(!token){
        return;
    }

    try {
        const res = await fetch("/api/users/me", {
            headers: {"Authorization": "Bearer " + token}
        });
        if (!res.ok) {
            return;
        }

        const me = await res.json();
        myId = me.id;

        if (String(myId) === String(userId)) {
            return;
        }

        const statusRes = await fetch("/friends/status?myId=" + myId + "&otherId=" + userId);
        if (!statusRes) {
            return;
        }

        friendshipStatus = await statusRes.text();
        friendshipStatus = friendshipStatus.replace(/"/g, "");

        const section = document.getElementById("friend-section");
        const btn = document.getElementById("friend-btn");
        const text = document.getElementById("friend-status-text");

        section.style.display = "block";

        if (friendshipStatus === "FRIENDS") {
            btn.style.display = "none";
            text.textContent = "✅ FRIENDS"
        } else if (friendshipStatus === "PENDING") {
            btn.style.display = "none";
            text.textContent = "⏳ The request has been sent";
        } else {
            btn.textContent = "➕ Add friend";
            btn.style.display = "block";
        }
    }catch (err){
        console.error(err);
    }
}

async function handleFriendBtn(){
    const token = localStorage.getItem("token");

    try {
        const res = await fetch("/friends/request", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({senderId: myId, receiverId: userId})
        });

        if (!res.ok) {
           const text = await res.text();
           alert(text || "Request could not be sent");
           return;
        }

        friendshipStatus = "PENDING";

        document.getElementById("friend-btn").style.display = "none";
        document.getElementById("friend-status-text").textContent = "⏳ The request has been sent";

    }catch (err){
        alert("Connection error");
    }
}

document.addEventListener("DOMContentLoaded", function (){
    loadFriendSection();
});

//initializers
loadProfile();
loadAchievements();
loadGameAnalytics();