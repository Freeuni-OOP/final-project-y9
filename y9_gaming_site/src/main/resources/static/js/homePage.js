async function loadHomeStats() {
    try {
        // 1. Pull the secure token out of your local storage
        const token = localStorage.getItem('token');

        // 2. Attach the token as a Bearer Authorization Header
        const res = await fetch("/stats/home", {
            method: "GET",
            headers: {
                "Authorization": token ? `Bearer ${token}` : "",
                "Content-Type": "application/json"
            }
        });

        if (!res.ok || res.redirected) {
            console.warn("Unauthorized API access, kicking back to login...");
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