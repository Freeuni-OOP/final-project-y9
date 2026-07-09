(function () {
    function ensureContainer() {
        let container = document.getElementById("achievement-toast-container");
        if (!container) {
            container = document.createElement("div");
            container.id = "achievement-toast-container";
            document.body.appendChild(container);
        }
        return container;
    }

    function showOne(achievement) {
        const container = ensureContainer();
        const toast = document.createElement("div");
        toast.className = "achievement-toast";
        toast.innerHTML =
            '<div class="achievement-toast__icon">\uD83C\uDFC6</div>' +
            '<div class="achievement-toast__body">' +
            '<div class="achievement-toast__label">Achievement Unlocked</div>' +
            '<div class="achievement-toast__name"></div>' +
            '<div class="achievement-toast__desc"></div>' +
            "</div>";
        toast.querySelector(".achievement-toast__name").textContent = achievement.name || "";
        toast.querySelector(".achievement-toast__desc").textContent = achievement.description || "";
        container.appendChild(toast);

        requestAnimationFrame(() => toast.classList.add("show"));

        setTimeout(() => {
            toast.classList.remove("show");
            toast.classList.add("hide");
            setTimeout(() => toast.remove(), 400);
        }, 4200);
    }

    window.showAchievementToasts = function (achievements) {
        if (!achievements || !achievements.length) return;
        achievements.forEach((a, i) => setTimeout(() => showOne(a), i * 350));
    };
})();
