/**
 * home.js
 * Fetches /stats/home and populates the page.
 */

document.addEventListener("DOMContentLoaded", () => {
    loadHomeStats();
});

async function loadHomeStats() {
    try {
        const res = await fetch("/stats/home");

        // If Spring Security redirects to /login (302 → text/html), treat as auth error
        if (!res.ok || res.redirected) {
            window.location.href = "/login";
            return;
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

// ── Total Users ────────────────────────────────────────────────────
function renderTotalUsers(total) {
    const el = document.getElementById("total-users");
    animateCount(el, 0, total, 1200);
}

/** Animates a number counting up over `duration` ms */
function animateCount(el, from, to, duration) {
    const startTime = performance.now();

    function tick(now) {
        const elapsed  = now - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const eased    = 1 - Math.pow(1 - progress, 3);   // ease-out cubic
        el.textContent = Math.round(from + (to - from) * eased).toLocaleString();

        if (progress < 1) requestAnimationFrame(tick);
    }

    requestAnimationFrame(tick);
}

// ── Top Players ────────────────────────────────────────────────────
function renderTopPlayers(players) {
    const list = document.getElementById("top-players");
    list.innerHTML = "";

    const medals = ["🥇", "🥈", "🥉"];

    players.forEach((player, i) => {
        const li = document.createElement("li");
        li.className = "leaderboard__item";
        li.innerHTML = `
      <span class="leaderboard__rank">${medals[i] ?? player.rank}</span>
      <img  class="leaderboard__avatar"
            src="${escHtml(player.avatarUrl)}"
            alt="${escHtml(player.username)}'s avatar"
            onerror="this.src='/img/avatars/default.png'" />
      <div class="leaderboard__info">
        <div class="leaderboard__name">${escHtml(player.username)}</div>
        <div class="leaderboard__score">${player.score.toLocaleString()} pts</div>
      </div>
    `;
        list.appendChild(li);
    });
}

// ── Recent Achievements ────────────────────────────────────────────
function renderRecentAchievements(achievements) {
    const list = document.getElementById("recent-achievements");
    list.innerHTML = "";

    achievements.forEach(ach => {
        const li = document.createElement("li");
        li.className = "achievements__item";
        li.innerHTML = `
      <img  class="achievements__icon"
            src="${escHtml(ach.iconUrl)}"
            alt="${escHtml(ach.achievementName)}"
            onerror="this.src='/img/ach/default.png'" />
      <div class="achievements__info">
        <div class="achievements__name">${escHtml(ach.achievementName)}</div>
        <div class="achievements__meta">${escHtml(ach.username)} · ${escHtml(ach.earnedAt)}</div>
      </div>
    `;
        list.appendChild(li);
    });
}

// ── Error state ────────────────────────────────────────────────────
function showError() {
    ["top-players", "recent-achievements"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.innerHTML = `<li style="color:var(--muted);font-size:.85rem">Could not load data.</li>`;
    });
    const hero = document.getElementById("total-users");
    if (hero) hero.textContent = "—";
}

// ── Security helper ────────────────────────────────────────────────
/** Escapes HTML to prevent XSS when inserting server data into innerHTML */
function escHtml(str) {
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}