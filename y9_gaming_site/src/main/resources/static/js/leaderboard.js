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

    //loading.style.display = 'block';
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:#ccc; padding:20px;">Synchronizing database rows...</td></tr>`;

    let url = `/leaderboard/${currentGame}`;
    if (currentFilter === 'today') {
        url = `/leaderboard/${currentGame}/today`;
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
                <td class="score">${entry.score.toLocaleString()} pts</td>
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

async function loadUserProfile() {
    const token = localStorage.getItem('token');
    if (!token) return;
    try {
        const res = await fetch("/api/users/me", {
            method: "GET",
            headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" }
        });
        if (res.ok) {
            const user = await res.json();
            if(document.getElementById("nav-username")) document.getElementById("nav-username").textContent = user.username;
            if(document.getElementById("nav-avatar") && user.avatarUrl) document.getElementById("nav-avatar").src = user.avatarUrl;
        }
    } catch (err) { console.error(err); }
}

window.onload = () => {
    loadUserProfile();
    loadLeaderboard();
};