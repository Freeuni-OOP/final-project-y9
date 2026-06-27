// მოთამაშისთვის ვინახავ რომელ თამაშში რა დროს ატარებს
window.gameStartTime = null;
window.activeGameTitle = null;
window.activeGameId = null;
window.activeGameCategory = null;

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = "/login";
        return;
    }
    loadHomeStats();
    //navbar is doing loaduserProfiles function
});


async function loadHomeStats() {
    try {
        const token = localStorage.getItem('token');

        // ვამზადებთ ჰედერებს. თუ ტოკენი არსებობს, ვაყოლებთ, თუ არა - ცარიელია
        const headers = { "Content-Type": "application/json" };
        if (token) {
            headers["Authorization"] = `Bearer ${token}`;
        }
        const role = localStorage.getItem('role');
        if (role === 'ADMIN') {
            document.getElementById('adminLink').style.display = 'block';
        }


        const res = await fetch("/stats/home", {
            method: "GET",
            headers: headers
        });

        // შესწორდა აქ: ლოგინზე გადავა მხოლოდ მაშინ, თუ ბექენდმა სპეციალურად 401 (Unauthorized) დააბრუნა
        if (res.status === 401) {
            localStorage.removeItem('token');
            window.location.href = "/login";
            return;
        }

        if (!res.ok) {
            throw new Error(`Server returned status ${res.status}`);
        }

        const data = await res.json();
        renderTotalUsers(data.totalUsers);
        renderTopPlayers(data.topPlayers);
        renderRecentAchievements(data.recentAchievements);

    } catch (err) {
        console.error("Failed to load home stats:", err);
        showError();
    }
}

function renderTotalUsers(total) {
    const el = document.getElementById("total-users");
    if (el) animateCount(el, 0, total, 1200);
}

function animateCount(el, from, to, duration) {
    const startTime = performance.now();

    function tick(now) {
        const elapsed  = now - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const eased    = 1 - Math.pow(1 - progress, 3);
        el.textContent = Math.round(from + (to - from) * eased).toLocaleString();

        if (progress < 1) requestAnimationFrame(tick);
    }

    requestAnimationFrame(tick);
}

function renderTopPlayers(players) {
    const list = document.getElementById("top-players");
    if (!list) return;
    list.innerHTML = "";

    const medals = ["🥇", "🥈", "🥉"];

    if (!players || players.length === 0) {
        list.innerHTML = `<li style="color:var(--muted);font-size:.85rem">No top players available.</li>`;
        return;
    }

    players.forEach((player, i) => {
        const li = document.createElement("li");
        li.className = "leaderboard__item";

        // შეცდომის თავიდან ასაცილებლად, თუ default.png-მდე მისასვლელი გზა შეიცვალა
        const avatarSrc = player.avatarUrl ? escHtml(player.avatarUrl) : '/img/avatars/default.png';

        li.innerHTML = `
  <span class="leaderboard__rank">${medals[i] ?? (i + 1)}</span>
  <img  class="leaderboard__avatar"
        src="${avatarSrc}"
        style="width: 50px; height: 50px; object-fit: cover; border-radius: 50%;" 
        alt="${escHtml(player.username)}'s avatar"
        onerror="this.onerror=null; this.src='/img/avatars/default.png';" />
  <div class="leaderboard__info">
    <div class="leaderboard__name">${escHtml(player.username)}</div>
    <div class="leaderboard__score">${player.score ? player.score.toLocaleString() : 0} pts</div>
  </div>
`;
        list.appendChild(li);
    });
}

function renderRecentAchievements(achievements) {
    const list = document.getElementById("recent-achievements");
    if (!list) return;
    list.innerHTML = "";

    if (!achievements || achievements.length === 0) {
        list.innerHTML = `<li style="color:var(--muted);font-size:.85rem">No recent achievements.</li>`;
        return;
    }

    achievements.forEach(ach => {
        const li = document.createElement("li");
        li.className = "achievements__item";

        const iconSrc = ach.iconUrl ? escHtml(ach.iconUrl) : '/img/ach/default.png';

        li.innerHTML = `
      <img  class="achievements__icon"
            src="${iconSrc}"
            alt="${escHtml(ach.achievementName)}"
            onerror="this.onerror=null; this.src='/img/ach/default.png';" />
      <div class="achievements__info">
        <div class="achievements__name">${escHtml(ach.achievementName)}</div>
        <div class="achievements__meta">${escHtml(ach.username)} · ${escHtml(ach.earnedAt)}</div>
      </div>
    `;
        list.appendChild(li);
    });
}

function showError() {
    ["top-players", "recent-achievements"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.innerHTML = `<li style="color:var(--muted);font-size:.85rem">Could not load data.</li>`;
    });
    const hero = document.getElementById("total-users");
    if (hero) hero.textContent = "—";
}

function escHtml(str) {
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

window.handlePlayGame = function(id, type, sourceUrl, title) {
    const modal = document.getElementById('gameTheaterModal');
    const iframe = document.getElementById('gameIframe');
    const titleHeader = document.getElementById('theaterGameTitle');

    titleHeader.textContent = title.toUpperCase();
    iframe.src = sourceUrl;
    modal.classList.remove('hidden');

    window.gameStartTime = Date.now();
    window.activeGameTitle = title;
    window.activeGameId = id;

    const globalGames = window.cachedGamesList || (typeof cachedGamesList !== 'undefined' ? cachedGamesList : null);
    if (globalGames && Array.isArray(globalGames)) {
        const foundGame = globalGames.find(g => Number(g.id) === Number(id));
        window.activeGameCategory = foundGame ? foundGame.category : "ARCADE";
    } else {
        window.activeGameCategory = "ARCADE";
    }
    console.log(`[TRACKER] Started: ${window.activeGameTitle}`);
};

window.closeGameTheater = function() {
    const modal = document.getElementById('gameTheaterModal');
    const iframe = document.getElementById('gameIframe');

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
    if (!token) {
        console.error("[TRACKER] Failed to send metrics: Token missing in localStorage");
        return;
    }

    try {
        console.log(`[TRACKER] Dispatching payload to server...`);
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
        console.log(`[TRACKER] Server response code: ${res.status}`);
    } catch (err) {
        console.error("Time tracking sync aborted:", err);
    }
}