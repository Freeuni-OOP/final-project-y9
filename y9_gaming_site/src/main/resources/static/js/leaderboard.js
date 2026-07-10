let currentGame = 'Joker';
let currentFilter = 'alltime';

async function selectGame(gameType, button) {
    currentGame = gameType;
    document.querySelectorAll('.game-btn').forEach(btn => btn.classList.remove('active'));
    button.classList.add('active');
    await loadLeaderboard();
}

async function selectFilter(filterType, button) {
    currentFilter = filterType;
    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
    button.classList.add('active');
    await loadLeaderboard();
}

async function changeGame() {
    const selectBox = document.getElementById("gameSelect");
    currentGame = selectBox.value;
    await loadLeaderboard();
}

async function loadLeaderboard() {
    const loading = document.getElementById('loading');
    const tbody = document.getElementById('leaderboard-body');

    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:#ccc; padding:20px;">Synchronizing database rows...</td></tr>`;

    let url = `/api/leaderboard/${currentGame}`;
    if (currentFilter === 'today') {
        url = `/api/leaderboard/${currentGame}/today`;
    }

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(url, {
            method: "GET",
            headers: {
                "Authorization": token ? `Bearer ${token}` : "",
                "Content-Type": "application/json"
            }
        });

        loading.style.display = 'none';

        if (!response.ok) {
            throw new Error(`Server returned status: ${response.status}`);
        }

        const scores = await response.json();

        if (scores.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:#aaa; padding:30px;">🎮 No records found. Be the first to set a score!</td></tr>`;
            return;
        }

        tbody.innerHTML = '';

        scores.forEach((entry, index) => {
            const row = document.createElement('tr');
            row.className = getRankClass(index);

            const playerName = entry.username ? entry.username : `Player #${entry.userId}`;

            row.innerHTML = `
                <td class="rank">${getRankDisplay(index)}</td>
                <td class="player-name">${playerName}</td>
                <td class="score">${formatScore(entry.score)}</td>
                <td class="date">${formatDate(entry.playedAt)}</td>
            `;
            tbody.appendChild(row);
        });

    } catch (error) {
        loading.style.display = 'none';
        console.error('Failed to load leaderboard:', error);
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align:center; color:#ff5599; font-weight:bold; padding:30px;">
                    ❌ Connection Failed to Backend Endpoint. (Check F12 Console)
                </td>
            </tr>`;
    }
}

function getRankDisplay(index) {
    if (index === 0) return '🥇';
    if (index === 1) return '🥈';
    if (index === 2) return '🥉';
    return `#${index + 1}`;
}

function getRankClass(index) {
    if (index === 0) return 'rank-gold';
    if (index === 1) return 'rank-silver';
    if (index === 2) return 'rank-bronze';
    return '';
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function formatScore(score) {
    if (currentGame === 'Wordle') {
        return score === 1 ? '1 guess' : `${score} guesses`;
    }
    if (currentGame === 'Sudoku') {
        const totalSeconds = Math.round(score);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes}:${seconds.toString().padStart(2, '0')}`;
    }
    return `${score.toLocaleString()} pts`;
}

async function loadUserProfile() {
    const token = localStorage.getItem('token');

    if (!token) {
        window.location.href = "/login";
        return;
    }

    try {
        const res = await fetch("/api/users/me", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (res.ok) {
            const user = await res.json();
            const navUser = document.getElementById("nav-username");
            const navAvatar = document.getElementById("nav-avatar");

            if (navUser) navUser.textContent = user.username;
            if (navAvatar && user.avatarUrl && user.avatarUrl !== 'null' && user.avatarUrl !== '') {
                navAvatar.src = user.avatarUrl;
                navAvatar.onerror = function() {
                    this.onerror = null;
                    this.src = '/img/avatars/default.png';
                };
            }
        } else if (res.status === 401 || res.status === 403) {
            localStorage.removeItem('token');
            window.location.href = "/login";
        }
    } catch (err) {
        console.error("Profile navbar sync failed:", err);
    }
}
async function redirectToMyProfile() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = "/login";
        return;
    }

    try {
        const res = await fetch("/api/users/me", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (res.ok) {
            const user = await res.json();
            window.location.href = `/profile/${user.id}`;
        } else {
            window.location.href = "/login";
        }
    } catch (err) {
        console.error("Failed to route to profile view:", err);
        window.location.href = "/login";
    }
}

async function logout() {
    try {
        await fetch("/api/users/logout", { method: "POST" });
    } catch (e) {
        console.error("Backend logout call skipped.");
    }
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    window.location.href = "/login";
}

document.addEventListener("DOMContentLoaded", () => {
    loadUserProfile();
    loadLeaderboard();
});