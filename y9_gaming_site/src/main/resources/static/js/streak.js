async function loadStreak(userId) {
    try {
        const response = await fetch(`/streak/${userId}`);
        const data = await response.json();

        const countEl = document.getElementById('streak-count');

        if (data && data.currentStreak) {
            countEl.textContent = data.currentStreak;
        } else {
            countEl.textContent = '0';
        }
    } catch (error) {
        console.error('Failed to load streak:', error);
        document.getElementById('streak-count').textContent = '0';
    }
}