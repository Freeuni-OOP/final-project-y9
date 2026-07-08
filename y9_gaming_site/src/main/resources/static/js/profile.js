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

loadProfile();
loadAchievements();