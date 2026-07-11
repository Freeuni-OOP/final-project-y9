(function () {
    const API = "/api/challenges";
    let meCache = null;

    function authHeaders(json) {
        const h = {};
        if (json) h["Content-Type"] = "application/json";
        const token = localStorage.getItem("token");
        if (token) h["Authorization"] = `Bearer ${token}`;
        return h;
    }

    async function getMe() {
        if (meCache) return meCache;
        const res = await fetch("/api/users/me", { headers: authHeaders(false) });
        if (!res.ok) return null;
        meCache = await res.json();
        return meCache;
    }

    function ensureToastContainer() {
        let c = document.getElementById("challenge-toast-container");
        if (!c) {
            c = document.createElement("div");
            c.id = "challenge-toast-container";
            document.body.appendChild(c);
        }
        return c;
    }

    function showToast(message, kind) {
        const container = ensureToastContainer();
        const toast = document.createElement("div");
        toast.className = "challenge-toast challenge-toast--" + (kind || "info");
        toast.textContent = message;
        container.appendChild(toast);
        requestAnimationFrame(() => toast.classList.add("show"));
        setTimeout(() => {
            toast.classList.remove("show");
            toast.classList.add("hide");
            setTimeout(() => toast.remove(), 400);
        }, 5000);
    }

    // ---------- offer-to-challenge banner ----------

    function ensureOfferBanner() {
        let el = document.getElementById("challenge-offer-banner");
        if (el) return el;
        el = document.createElement("div");
        el.id = "challenge-offer-banner";
        el.className = "challenge-offer-banner";
        el.innerHTML =
            '<span class="challenge-offer-banner__text"></span>' +
            '<button class="challenge-offer-banner__btn" type="button">\uD83C\uDFC6 Challenge a friend</button>' +
            '<button class="challenge-offer-banner__close" type="button" aria-label="Dismiss">\u2715</button>';
        document.body.appendChild(el);
        el.querySelector(".challenge-offer-banner__close").addEventListener("click", () => hideOfferBanner());
        return el;
    }

    function hideOfferBanner() {
        const el = document.getElementById("challenge-offer-banner");
        if (el) el.classList.remove("show");
    }

    function offerGameChallenge(gameKey, contextId, value) {
        const el = ensureOfferBanner();
        el.querySelector(".challenge-offer-banner__text").textContent = "Nice score!";
        const btn = el.querySelector(".challenge-offer-banner__btn");
        btn.onclick = () => {
            hideOfferBanner();
            openFriendPicker(gameKey, contextId, value);
        };
        requestAnimationFrame(() => el.classList.add("show"));
    }

    // ---------- friend picker modal ----------

    function ensureModal() {
        let overlay = document.getElementById("challenge-modal-overlay");
        if (overlay) return overlay;

        overlay = document.createElement("div");
        overlay.id = "challenge-modal-overlay";
        overlay.className = "challenge-modal-overlay";
        overlay.innerHTML =
            '<div class="challenge-modal-card">' +
            '<h2>Challenge a friend</h2>' +
            '<div class="challenge-modal-list" id="challenge-modal-list"></div>' +
            '<button class="challenge-modal-close" id="challenge-modal-close">Cancel</button>' +
            "</div>";
        document.body.appendChild(overlay);

        overlay.addEventListener("click", (e) => {
            if (e.target === overlay) closeModal();
        });
        overlay.querySelector(".challenge-modal-card").addEventListener("click", (e) => e.stopPropagation());
        overlay.querySelector("#challenge-modal-close").addEventListener("click", closeModal);
        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") closeModal();
        });

        return overlay;
    }

    function closeModal() {
        const overlay = document.getElementById("challenge-modal-overlay");
        if (overlay) overlay.classList.remove("open");
    }

    async function openFriendPicker(gameKey, contextId, value) {
        const overlay = ensureModal();
        const list = overlay.querySelector("#challenge-modal-list");
        list.innerHTML = '<div class="challenge-modal-empty">Loading friends\u2026</div>';
        overlay.classList.add("open");

        const me = await getMe();
        if (!me) {
            list.innerHTML = '<div class="challenge-modal-empty">Please log in.</div>';
            return;
        }

        try {
            const res = await fetch(`/friends/accepted/${me.id}`, { headers: authHeaders(false) });
            const friendships = await res.json();

            if (!friendships || friendships.length === 0) {
                list.innerHTML = '<div class="challenge-modal-empty">Add some friends first!</div>';
                return;
            }

            const friendIds = friendships.map((f) => (f.senderId === me.id ? f.receiverId : f.senderId));
            list.innerHTML = "";

            for (const friendId of friendIds) {
                const userRes = await fetch(`/api/users/${friendId}`, { headers: authHeaders(false) });
                if (!userRes.ok) continue;
                const friend = await userRes.json();

                const item = document.createElement("div");
                item.className = "challenge-modal-item";
                item.innerHTML =
                    '<img class="challenge-modal-avatar" src="' + (friend.avatarUrl || "/img/avatars/default.png") + '" alt="">' +
                    '<span class="challenge-modal-username"></span>';
                item.querySelector(".challenge-modal-username").textContent = friend.username || "";
                item.onclick = () => sendChallenge(friendId, friend.username, gameKey, contextId, value);
                list.appendChild(item);
            }
        } catch (e) {
            console.error("Failed to load friends for challenge:", e);
            list.innerHTML = '<div class="challenge-modal-empty">Could not load friends.</div>';
        }
    }

    async function sendChallenge(receiverId, receiverUsername, gameKey, contextId, value) {
        closeModal();
        try {
            const res = await fetch(API, {
                method: "POST",
                headers: authHeaders(true),
                body: JSON.stringify({ receiverId, gameKey, contextId }),
            });
            if (!res.ok) {
                const msg = await res.text();
                showToast(msg || "Could not send challenge.", "error");
                return;
            }
            showToast("\uD83C\uDFC6 Challenge sent to " + receiverUsername + "!", "success");
        } catch (e) {
            console.error("Failed to send challenge:", e);
            showToast("Could not send challenge.", "error");
        }
    }

    async function submitChallengeAttempt(challengeId, value) {
        try {
            const res = await fetch(`${API}/${challengeId}/submit`, {
                method: "POST",
                headers: authHeaders(true),
                body: JSON.stringify({ value }),
            });
            if (!res.ok) {
                const msg = await res.text();
                showToast(msg || "Could not report challenge result.", "error");
                return null;
            }
            const dto = await res.json();
            const me = await getMe();
            const iWon = me && dto.winnerId === me.id;
            const opponent = me && dto.receiverId === me.id ? dto.senderUsername : dto.receiverUsername;
            showToast(iWon ? "\uD83C\uDFC6 You beat " + opponent + "!" : "\uD83D\uDE05 " + opponent + " keeps the crown this time.",
                iWon ? "success" : "info");
            return dto;
        } catch (e) {
            console.error("Failed to submit challenge attempt:", e);
            showToast("Could not report challenge result.", "error");
            return null;
        }
    }

    window.offerGameChallenge = offerGameChallenge;
    window.submitChallengeAttempt = submitChallengeAttempt;
})();
