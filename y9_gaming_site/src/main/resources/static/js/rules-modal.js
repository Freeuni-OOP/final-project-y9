(function () {
    const API = "/api/rules";

    function authHeaders() {
        const h = { "Content-Type": "application/json" };
        const token = localStorage.getItem("token");
        if (token) h["Authorization"] = `Bearer ${token}`;
        return h;
    }

    function closeLabel() {
        return document.documentElement.lang === "ka" ? "გასაგებია" : "Got it";
    }

    function ensureOverlay() {
        let overlay = document.getElementById("rules-modal-overlay");
        if (overlay) return overlay;

        overlay = document.createElement("div");
        overlay.id = "rules-modal-overlay";
        overlay.className = "rules-modal-overlay";
        overlay.innerHTML =
            '<div class="rules-modal-card">' +
            '<h2 id="rules-modal-title"></h2>' +
            '<ul class="rules-modal-list" id="rules-modal-list"></ul>' +
            '<button class="rules-modal-close" id="rules-modal-close">' + closeLabel() + "</button>" +
            "</div>";
        document.body.appendChild(overlay);

        overlay.addEventListener("click", (e) => {
            if (e.target === overlay) closeRulesModal();
        });
        overlay.querySelector(".rules-modal-card").addEventListener("click", (e) => e.stopPropagation());
        overlay.querySelector("#rules-modal-close").addEventListener("click", closeRulesModal);

        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") closeRulesModal();
        });

        return overlay;
    }

    function closeRulesModal() {
        const overlay = document.getElementById("rules-modal-overlay");
        if (overlay) overlay.classList.remove("open");
    }

    function renderRules(gameRules) {
        const overlay = ensureOverlay();
        overlay.querySelector("#rules-modal-title").textContent = gameRules.title || "";

        const list = overlay.querySelector("#rules-modal-list");
        list.innerHTML = "";
        (gameRules.rules || []).forEach((rule) => {
            const li = document.createElement("li");
            li.textContent = rule;
            list.appendChild(li);
        });

        overlay.classList.add("open");
    }

    function showGameRules(gameKey) {
        fetch(`${API}/${gameKey}`, { headers: authHeaders() })
            .then((res) => {
                if (!res.ok) throw new Error("rules fetch failed: " + res.status);
                return res.json();
            })
            .then(renderRules)
            .catch((err) => console.error("Could not load game rules:", err));
    }

    window.showGameRules = showGameRules;

    document.addEventListener("DOMContentLoaded", () => {
        const gameKey = document.body.dataset.gameKey;
        if (!gameKey) return;

        const seenKey = `rulesSeen:${gameKey}`;
        if (!localStorage.getItem(seenKey)) {
            showGameRules(gameKey);
            localStorage.setItem(seenKey, "1");
        }
    });
})();
