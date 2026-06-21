let currentGame = 'wordle';

async function selectGame(gameType, button) {
    currentGame = gameType;

    document.querySelectorAll('.game-btn').forEach(btn => btn.classList.remove('active'));
    button.classList.add('active');

    await loadLeaderboard();
}

async function loadLeaderboard() {
    const loading = document.getElementById('loading');
    const emptyState = document.getElementById('empty-state');
    const table = document.getElementById('leaderboard-table');
    const tbody = document.getElementById('leaderboard-body');

    loading.style.display = 'block';
    emptyState.style.display = 'none';
    table.style.display = 'none';

    try {
        const response = await fetch(`/leaderboard/${currentGame}`);
        const scores = await response.json();

        loading.style.display = 'none';

        if (scores.length === 0) {
            emptyState.style.display = 'block';
            return;
        }

        table.style.display = 'table';
        tbody.innerHTML = '';

        scores.forEach((entry, index) => {
            const row = document.createElement('tr');
            row.className = getRankClass(index);

            row.innerHTML = `
                <td class="rank">${getRankDisplay(index)}</td>
                <td>User #${entry.userId}</td>
                <td class="score">${entry.score}</td>
                <td class="date">${formatDate(entry.playedAt)}</td>
            `;
            tbody.appendChild(row);
        });

    } catch (error) {
        loading.style.display = 'none';
        console.error('Failed to load leaderboard:', error);
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
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

window.onload = loadLeaderboard;