const API_BASE = "/api/users";

const avatarImg = document.getElementById("avatar-img");
const usernameDisplay = document.getElementById("username-display");
const errorMessage = document.getElementById("error-message");
const uploadSection = document.getElementById("avatar-upload-section");
const avatarForm = document.getElementById("avatar-form");
const avatarInput = document.getElementById("avatar-input");
const uploadStatus = document.getElementById("upload-status");

// for game configs
window.cachedGamesList = [];

function showError(message) {
    if (errorMessage) {
        errorMessage.textContent = message;
        errorMessage.hidden = false;
    }
}

function formatPlaytime(totalSeconds) {
    if (!totalSeconds || totalSeconds < 60) return `${totalSeconds || 0}s`;
    const mins = Math.floor(totalSeconds / 60);
    if (mins < 60) return `${mins}m`;
    const hrs = Math.floor(mins / 60);
    const remainingMins = mins % 60;
    return remainingMins > 0 ? `${hrs}h ${remainingMins}m` : `${hrs}h`;
}

async function prefetchGamesCatalog() {
    try {
        const res = await fetch('https://raw.githubusercontent.com/InterstellarNetwork/Interstellar/main/static/assets/json/g.min.json');
        if (!res.ok) return;

        const remoteGames = await res.json();
        window.cachedGamesList = remoteGames
            .filter(game => game && game.name && !game.name.startsWith('!') && !['Steam', 'Amazon Luna', 'Newgrounds'].includes(game.name))
            .map((game, index) => {
                let finalIcon = game.image || game.img || game.logo || '';
                if (!finalIcon) {
                    finalIcon = '/img/games/default.png';
                } else if (finalIcon.startsWith('/')) {
                    finalIcon = `https://raw.githubusercontent.com/InterstellarNetwork/Interstellar/main/static${finalIcon}`;
                }
                return {
                    id: index,
                    title: game.name || 'Unknown Web Game',
                    sourceUrl: game.link || '',
                    iconUrl: finalIcon,
                    gameType: 'OPENSOURCE',
                    category: game.category || 'ARCADE'
                };
            });
    } catch (err) {
        console.error("Profile matching cache loader bypassed:", err);
    }
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
        if (usernameDisplay) usernameDisplay.textContent = profile.username;

        if (avatarImg) {
            avatarImg.src = profile.avatarUrl || "/img/avatars/default.png";
            avatarImg.onerror = function () {
                this.onerror = null;
                this.src = "/img/avatars/default.png";
            };
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

        if (loggedInUsername && loggedInUsername === profile.username && uploadSection) {
            uploadSection.hidden = false;
        }
    } catch (error) {
        console.error("Profile payload processing crashed:", error);
        showError("Connection failed.");
    }
}

async function loadGameAnalytics() {
    const token = localStorage.getItem("token");
    const gamesContainer = document.getElementById("top-games-container");
    const catsContainer = document.getElementById("top-categories-container");

    if (!token) return;

    const headers = {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
    };

    let gamesData = [];
    let categoriesData = [];

    try {
        const gamesRes = await fetch(`/api/games/${userId}/top-3`, { headers });
        if (gamesRes.ok) {
            gamesData = await gamesRes.json();
            renderTopGames(gamesData);
        } else {
            if (gamesContainer) gamesContainer.innerHTML = `<p class="ach-empty">Failed to load statistics.</p>`;
        }
    } catch (err) {
        console.error("Error loading games tracking data:", err);
    }

    try {
        const catsRes = await fetch(`/api/games/${userId}/top-categories`, { headers });
        if (catsRes.ok) {
            categoriesData = await catsRes.json();
            renderTopCategories(categoriesData);
        } else {
            if (catsContainer) catsContainer.innerHTML = `<p class="ach-empty">Failed to load category stats.</p>`;
        }
    } catch (err) {
        console.error("Error loading categories tracking data:", err);
    }

    generateRecommendations(gamesData, categoriesData);
}

function renderTopGames(games) {
    const container = document.getElementById("top-games-container");
    if (!container) return;

    if (!games || games.length === 0) {
        container.innerHTML = `<p class="ach-empty">No games played yet.</p>`;
        return;
    }

    container.className = "profile-mini-grid";

    container.innerHTML = games.map((g, index) => {
        const matched = window.cachedGamesList.find(x => x.title.toLowerCase() === g.gameTitle.toLowerCase());
        const iconSrc = matched ? matched.iconUrl : '/img/games/default.png';
        const categoryKey = matched ? matched.category : "ARCADE";

        const clickAction = matched
            ? `window.handlePlayGame('${matched.id}', '${matched.gameType}', '${matched.sourceUrl}', \`${escapeHtml(g.gameTitle)}\`, '${categoryKey}')`
            : `window.location.href='/home'`;

        return `
            <div class="profile-game-box rank-${index + 1}" onclick="${clickAction}">
                <div class="profile-thumb-wrapper">
                    <img class="profile-mini-icon" 
                         src="${iconSrc}" 
                         alt="${escapeHtml(g.gameTitle)}" 
                         onerror="this.onerror=null; this.src='/img/games/default.png';" />
                </div>
                <span class="profile-box-title" title="${escapeHtml(g.gameTitle)}">${escapeHtml(g.gameTitle)}</span>
                <span class="profile-box-subtitle">${formatPlaytime(g.totalTimeSeconds)}</span>
            </div>
        `;
    }).join("");
}

function renderTopCategories(categories) {
    const container = document.getElementById("top-categories-container");
    if (!container) return;

    if (!categories || categories.length === 0) {
        container.innerHTML = `<p class="ach-empty">No stats tracked yet.</p>`;
        return;
    }

    container.className = "clickable-stats-list";

    container.innerHTML = categories.map(c => {
        const targetUrl = `/home?category=${encodeURIComponent(c.category)}`;

        return `
            <div class="stat-clickable-item" onclick="window.location.href='${targetUrl}'">
                <div class="stat-main-info">
                    <span class="stat-item-title">${escapeHtml(c.category)}</span>
                    <span class="stat-item-subtitle">Click to view all ${escapeHtml(c.category).toLowerCase()} games</span>
                </div>
                <span class="rarest-count">${formatPlaytime(c.totalTimeSeconds)}</span>
            </div>
        `;
    }).join("");
}
// file system listeners
if (avatarInput) {
    avatarInput.addEventListener("change", function () {
        if (avatarInput.files && avatarInput.files[0]) {
            uploadStatus.textContent = avatarInput.files[0].name;
        } else {
            uploadStatus.textContent = "No file selected.";
        }
    });
}

if (avatarForm) {
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
            .then(res => {
                if (!res.ok) throw new Error("Upload failed.");
                return res.json();
            })
            .then(result => {
                if (avatarImg) avatarImg.src = result.avatarUrl;
                const navAvatar = document.getElementById("nav-avatar");
                if (navAvatar) navAvatar.src = result.avatarUrl;
                uploadStatus.textContent = "Avatar updated successfully!";
            })
            .catch(err => {
                uploadStatus.textContent = err.message;
            });
    });
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
    if (!items || items.length === 0) { el.innerHTML = ""; return; }
    el.innerHTML = items.map(a => `
        <div class="rarest-item">
            <div class="rarest-info">
                <span class="rarest-name">${escapeHtml(a.name)}</span>
                <span class="rarest-desc">${escapeHtml(a.description || "")}</span>
            </div>
            <span class="rarest-count">${a.earnedCount}</span>
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

//session analytics tracking
window.gameStartTime = null;
window.activeGameTitle = null;
window.activeGameId = null;
window.activeGameCategory = null;

window.handlePlayGame = function(id, type, sourceUrl, title, category) {
    const modal = document.getElementById('gameTheaterModal');
    const iframe = document.getElementById('gameIframe');
    const titleHeader = document.getElementById('theaterGameTitle');

    if (!modal || !iframe) {
        window.location.href = `/home`;
        return;
    }

    if (titleHeader) titleHeader.textContent = title.toUpperCase();
    iframe.src = sourceUrl;
    modal.classList.remove('hidden');

    window.gameStartTime = Date.now();
    window.activeGameTitle = title;
    window.activeGameId = id;
    window.activeGameCategory = category || "ARCADE";
    console.log(`[PROFILE TRACKER] Active tracking session started for: ${window.activeGameTitle}`);
};


window.closeGameTheater = function() {
    const modal = document.getElementById('gameTheaterModal');
    const iframe = document.getElementById('gameIframe');
    if (!modal || !iframe) return;

    modal.classList.add('hidden');
    iframe.src = "";

    if (window.gameStartTime && window.activeGameTitle) {
        const timeSpentSeconds = Math.max(1, Math.floor((Date.now() - window.gameStartTime) / 1000));
        sendTimeAnalytics(window.activeGameId, window.activeGameTitle, window.activeGameCategory, timeSpentSeconds);
    }

    window.gameStartTime = null;
    window.activeGameTitle = null;
    window.activeGameId = null;
    window.activeGameCategory = null;
};

async function sendTimeAnalytics(id, title, category, seconds) {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
        console.log(`[PROFILE TRACKER] Syncing stats updates for game session...`);
        const res = await fetch("/api/games/track-time", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                gameId: id,
                gameTitle: title,
                category: category || "ARCADE",
                durationSeconds: seconds
            })
        });

        console.log(`[PROFILE TRACKER] Logged time metrics synced successfully (${res.status}). Refreshing view...`);
        loadGameAnalytics();
    } catch (err) {
        console.error("Time tracking sync failed from profile scope:", err);
    }
}

//startup order
document.addEventListener("DOMContentLoaded", async () => {
    if (localStorage.getItem("role") === "ADMIN") {
        const adminLink = document.getElementById("adminLink");
        if (adminLink) adminLink.style.display = "block";
    }

    await prefetchGamesCatalog();
    loadProfile().catch(err => console.error(err));
    loadAchievements().catch(err => console.error(err));
    loadGameAnalytics().catch(err => console.error(err));
    loadFriendsList().catch(err => console.error(err));
});
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

function generateRecommendations(topGames, topCategories) {
    const wrapper = document.getElementById("recommendations-wrapper");
    const container = document.getElementById("recommendations-container");
    if (!container || !wrapper) return;

    if (myId === null || String(myId) !== String(userId)) {
        wrapper.style.display = "none";
        return;
    }

    wrapper.style.display = "block";

    if (!window.cachedGamesList || window.cachedGamesList.length === 0) {
        container.innerHTML = `<p class="ach-empty">Unable to fetch recommendations catalog.</p>`;
        return;
    }

    const playedTitles = new Set((topGames || []).map(g => g.gameTitle.toLowerCase()));
    const favoriteCategories = (topCategories || []).map(c => c.category.toUpperCase());

    const friendElements = document.querySelectorAll(".friend-name");
    const structuralFriendCount = friendElements.length;
    const friendsPool = ["Stickman Archero Fight", "Helix Jump", "Retro Bowl", "Tunnel Rush", "Vex 4"];

    let pool = window.cachedGamesList.filter(game => {
        if (playedTitles.has(game.title.toLowerCase())) return false;

        const matchesCategory = game.category && favoriteCategories.includes(game.category.toUpperCase());
        const matchesFriends = structuralFriendCount > 0 && friendsPool.some(f => f.toLowerCase() === game.title.toLowerCase());

        return matchesCategory || matchesFriends;
    });

    if (pool.length === 0) {
        pool = window.cachedGamesList.filter(game => !playedTitles.has(game.title.toLowerCase()));
    }

    const shuffled = pool.sort(() => 0.5 - Math.random());
    const selectedRecommendations = shuffled.slice(0, 3);

    if (selectedRecommendations.length === 0) {
        container.innerHTML = `<p class="ach-empty">Play more games to unlock tailored content.</p>`;
        return;
    }

    container.className = "profile-mini-grid";
    container.innerHTML = selectedRecommendations.map(game => {
        const categoryKey = game.category || "ARCADE";

        return `
            <div class="profile-game-box recommendation-item" 
                 onclick="window.handlePlayGame('${game.id}', '${game.gameType}', '${game.sourceUrl}', \`${escapeHtml(game.title)}\`, '${categoryKey}')">
                <div class="profile-thumb-wrapper">
                    <img class="profile-mini-icon" 
                         src="${game.iconUrl}" 
                         alt="${escapeHtml(game.title)}" 
                         onerror="this.onerror=null; this.src='/img/games/default.png';" />
                </div>
                <span class="profile-box-title" title="${escapeHtml(game.title)}">${escapeHtml(game.title)}</span>
            </div>
        `;
    }).join("");
}
async function loadFriendsList(){
    const Friends = document.getElementById("friends-list-container");
    if(!Friends){
        return;
    }

    const token = localStorage.getItem("token");
    if(!token){
        return;
    }

    try{
        const res = await fetch(`/friends/accepted/${userId}`,{
            headers: {"Authorization": 'Bearer ${token}'}
        });

        if(!res.ok){
            Friends.innerHTML = `<p class ="ach-empty">Failed to load friends list.</p>`;
            return;
        }

        const friendships = await res.json();

        if(!friendships || friendships.length === 0){
            Friends.innerHTML = `<p class="ach-empty">No friends added yet.</p>`;
            return;
        }

        Friends.innerHTML = "";

        for(const friend of friendships){
            let friendId;

            const currentUserIdStr = String(userId);
            const senderIdStr = String(friend.senderId);

            if(currentUserIdStr === senderIdStr){
                friendId = friend.receiverId;
            }else{
                friendId = friend.senderId;
            }

            try{
                const userRes = await fetch(`/api/users/${friendId}`, {
                    headers: {"Authorization": `Bearer ${token}`}
                });

                if(userRes.ok){
                    const friendshipProfile = await userRes.json();

                    const avatarSrc = friendshipProfile.avatarUrl || "/img/avatars/default.png";

                    const friendProfileUrl = `/profile/${friendId}`;

                    const friendHtml = `<div class="friend-item" onclick="window.location.href='${friendProfileUrl}'" style = "cursor: pointer;">
                        <img src="${avatarSrc}" class="friend-avatar-placeholder" style="object-fit: cover;" 
                                 onerror="this.onerror=null; this.src='/img/avatars/default.png';" />
                            <div class="friend-info">
                                <span class="friend-name">${escapeHtml(friendshipProfile.username)}</span>
                                <span class="friend-status online">View Profile</span>
                            </div>
                            </div>`;
                    Friends.insertAdjacentHTML("beforeend", friendHtml);
                }
            }catch (err){
                console.error("Error fetching friend profile details:", err);
            }
        }
    }catch (error){
        console.error("Error loading friends list:", error);
        Friends.innerHTML = `<p class="ach-empty">Error connecting to friends service.</p>`;
        Friends.innerHTML = `<p class="ach-empty">Error connecting to friends service.</p>`;
    }
}