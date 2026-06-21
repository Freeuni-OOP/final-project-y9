async function loadAchievements(userId) {
    const grid = document.getElementById('achievements-grid');

    try {
        // achievements od user
        const response = await fetch(`/achievements/${userId}`);
        const userAchievements = await response.json();

        // every achievement
        const allResponse = await fetch(`/achievements/catalog`);
        const allAchievements = await allResponse.json();

        grid.innerHTML = '';

        if (userAchievements.length === 0) {
            grid.innerHTML = '<p class="no-achievements">No achievements yet. Keep playing!</p>';
            return;
        }

        userAchievements.forEach(ua => {
            const achievement = allAchievements.find(a => a.id === ua.achievementId);
            if (!achievement) return;

            const badge = document.createElement('div');
            badge.className = 'achievement-badge';
            badge.title = achievement.description;
            badge.innerHTML = `
                <img src="/images/achievements/${achievement.icon}" alt="${achievement.name}">
                <span class="achievement-name">${achievement.name}</span>
            `;
            grid.appendChild(badge);
        });

    } catch (error) {
        console.error('Failed to load achievements:', error);
        grid.innerHTML = '<p class="no-achievements">Could not load achievements.</p>';
    }
}