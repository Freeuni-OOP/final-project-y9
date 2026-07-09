const API = "/api/joker";

// --- State ---
let roomCode = null;
let myUserId = null;
let gameState = null;
let selectedCard = null;       // { suit, value, isJoker }
let pendingJokerSuit = null;   // suit picked in modal

// --- Init ---
document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("token");
    if (!token) { window.location.href = "/login"; return; }

    // get roomCode from URL: /joker/JOKER-XXXXXXXX
    roomCode = window.location.pathname.split("/").pop();

    // get my userId
    try {
        const res = await fetch("/api/users/me", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.ok) {
            const me = await res.json();
            myUserId = me.id;
        }
    } catch (e) { console.error("Failed to load user", e); }

    await loadState();
    connectWebSocket();
});

// --- Load state from REST ---
async function loadState() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API}/${roomCode}/state`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) { setMsg("ვერ ჩაიტვირთა", "error"); return; }
        gameState = await res.json();
        render();
    } catch (e) {
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- WebSocket (STOMP) ---
function connectWebSocket() {
    // SockJS + STOMP
    const socket = new SockJS("/ws");
    const stompClient = Stomp.client(socket); // loaded via CDN in HTML
    stompClient.debug = null; // silence logs

    stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/joker/${roomCode}`, (message) => {
            const event = JSON.parse(message.body);
            handleWsEvent(event);
        });
    });
}

function handleWsEvent(event) {
    // on any event, reload state from REST so we always have fresh data
    loadState();
    // show a quick message for trick won
    if (event.type === "TRICK_WON") {
        setMsg("✅ " + event.data, "success");
        setTimeout(() => setMsg("", ""), 2000);
    }
    if (event.type === "GAME_OVER") {
        setMsg("🏆 თამაში დასრულდა!", "success");
        const me = event.data && event.data.players && event.data.players.find(p => p.userId === myUserId);
        if (me && me.newAchievements && me.newAchievements.length && window.showAchievementToasts) {
            window.showAchievementToasts(me.newAchievements);
        }
    }
}

// --- Render ---
function render() {
    if (!gameState) return;

    const status   = gameState.status;
    const currRound = gameState.currRound;
    const totalRounds = gameState.config?.totalRounds || "?";
    const trump    = gameState.trumpSuit;
    const players  = gameState.players || [];
    const currPlayerIdx = gameState.currPlayer;

    // topbar
    document.getElementById("roomCodeDisplay").textContent = roomCode;
    document.getElementById("roundDisplay").textContent    = `${currRound} / ${totalRounds}`;
    document.getElementById("trumpDisplay").textContent    = trumpDisplay(trump);
    document.getElementById("statusDisplay").textContent   = statusDisplay(status);

    // start button — show only to host when WAITING and full
    const isHost  = players.length > 0 && players[0].userId === myUserId;
    const isFull  = players.length === gameState.config?.players;
    const startBtn = document.getElementById("startBtn");
    startBtn.style.display = (isHost && status === "WAITING" && isFull) ? "block" : "none";

    // players list
    renderPlayers(players, currPlayerIdx, status);

    // trump setter panel
    const dealerIdx = gameState.dealer;
    const isDealer  = players[dealerIdx]?.userId === myUserId;
    const needsTrump = (trump === null || trump === "NONE") && status === "BIDDING";
    document.getElementById("trumpSetterPanel").classList.toggle("hidden", !(isDealer && needsTrump));

    // bid panel
    const isMyTurn  = players[currPlayerIdx]?.userId === myUserId;
    const myPlayer  = players.find(p => p.userId === myUserId);
    const notBidYet = myPlayer && myPlayer.prophecy < 0;
    document.getElementById("bidPanel").classList.toggle("hidden", !(status === "BIDDING" && isMyTurn && notBidYet));

    if (status === "BIDDING" && isMyTurn && notBidYet) {
        const players = gameState.players || [];
        const totalCards = gameState.currRound; // მიმდინარე რაუნდის კარტები

        // 1. ვითვლით უკვე თქმული ბიდების ჯამს და რამდენი მოთამაშე დარჩა
        let totalExistingBids = 0;
        let biddedPlayersCount = 0;

        players.forEach(p => {
            if (p.prophecy >= 0) {
                totalExistingBids += p.prophecy;
                biddedPlayersCount++;
            }
        });

        const bidInfoEl = document.getElementById("bidInfo");

        // 2. ვამოწმებთ, ვართ თუ არა ბოლო მოთამაშე
        const isLastPlayer = (biddedPlayersCount === players.length - 1);
        let forbiddenBid = totalCards - totalExistingBids;

        if (isLastPlayer && forbiddenBid >= 0 && forbiddenBid <= totalCards) {
            bidInfoEl.innerHTML = `შენ ხარ ბოლო! მიუთითე 0 – ${totalCards} <br><span style="color:var(--pink); font-weight:bold;">⚠️ ${forbiddenBid}-ს გარდა ხარ!</span>`;
        } else {
            bidInfoEl.textContent = `მიუთითე 0 – ${totalCards} ხელს შორის`;
        }

        // 3. შეტენვა vs წაგლეჯვა სტატუსის ჩვენება
        const typeDisplay = document.getElementById("gameTypeDisplay");
        if (typeDisplay) {
            if (totalExistingBids > totalCards) {
                typeDisplay.textContent = "💥 თამაშის ტიპი: წაგლეჯვა";
                typeDisplay.style.color = "var(--pink)";
            } else if (totalExistingBids < totalCards && biddedPlayersCount === players.length) {
                // თუ ყველამ თქვა და ჯამი ნაკლებია
                typeDisplay.textContent = "🃏 თამაშის ტიპი: შეტენვა";
                typeDisplay.style.color = "var(--clubs)";
            } else {
                // სანამ თამაში მიმდინარეობს (ბოლო მოთამაშემდე) შეგვიძლია მიმდინარე ტენდენცია ვაჩვენოთ
                typeDisplay.textContent = `მიმდინარე ბიდების ჯამი: ${totalExistingBids} / ${totalCards}`;
                typeDisplay.style.color = "#aaa";
            }
        }
    }

    // trick cards
    renderTrick(gameState.currentTrick);

    // hand cards
    renderHand(myPlayer?.cardList || [], status, isMyTurn);
}

function renderPlayers(players, currPlayerIdx, status) {
    const list = document.getElementById("playersList");
    list.innerHTML = "";
    players.forEach((p, i) => {
        const div = document.createElement("div");
        div.className = "joker-player-row" + (i === currPlayerIdx && status !== "WAITING" ? " active-turn" : "");
        const prophecyText = p.prophecy >= 0 ? `პროფეცია: ${p.prophecy}` : "ჯერ არ უბიდია";
        div.innerHTML = `
            <div>
                <div class="joker-player-name">${p.username} ${p.userId === myUserId ? "(შენ)" : ""}</div>
                <div class="joker-player-bid">${prophecyText} · ხელი: ${p.current}</div>
            </div>
            <div class="joker-player-score">${p.totalScore} ქ.</div>
        `;
        list.appendChild(div);
    });

    // empty slots
    const required = gameState.config?.players || 4;
    for (let i = players.length; i < required; i++) {
        const div = document.createElement("div");
        div.className = "joker-player-slot empty";
        div.innerHTML = `<div class="joker-slot-dot empty"></div><span style="color:#555;">ლოდინი...</span>`;
        list.appendChild(div);
    }
}

function renderTrick(trick) {
    const area = document.getElementById("trickCards");
    area.innerHTML = "";
    if (!trick) return;
    const plays = trick.playedCards || [];
    plays.forEach(play => {
        const card = buildCardEl(play.card, play.player?.username, false);
        card.classList.add("played");
        area.appendChild(card);
    });
}

function renderHand(cards, status, isMyTurn) {
    const area = document.getElementById("handCards");
    area.innerHTML = "";

    cards.forEach(card => {
        const el = buildCardEl(card, null, status === "PLAYING" && isMyTurn);
        el.addEventListener("click", () => {
            if (status !== "PLAYING" || !isMyTurn) return;
            selectCard(el, card);
        });
        area.appendChild(el);
    });
}

function selectCard(el, card) {
    // deselect previous
    document.querySelectorAll(".joker-card.selected").forEach(c => c.classList.remove("selected"));
    selectedCard = card;
    el.classList.add("selected");

    if (card.isJoker) {
        // show joker modal
        openJokerModal(card);
    } else {
        playSelectedCard("NONE", "NONE");
    }
}

// --- Build card element ---
function buildCardEl(card, label, clickable) {
    const div = document.createElement("div");
    div.className = "joker-card";

    if (card.isJoker) {
        div.classList.add("joker-card--joker");
        div.innerHTML = `
            <div class="card-suit">🃏</div>
            <div class="card-value">${card.value === 15 ? "J1" : "J2"}</div>
            ${label ? `<div style="font-size:0.65rem;color:#ccc;margin-top:4px;">${label}</div>` : ""}
        `;
    } else {
        const suitClass = suitToClass(card.suit);
        div.classList.add(`joker-card--${suitClass}`);
        div.innerHTML = `
            <div class="card-suit">${suitSymbol(card.suit)}</div>
            <div class="card-value">${valueDisplay(card.value)}</div>
            ${label ? `<div style="font-size:0.65rem;color:#999;margin-top:4px;">${label}</div>` : ""}
        `;
    }

    if (!clickable) div.style.cursor = "default";
    return div;
}

// --- Joker modal ---
function openJokerModal(card) {
    const picker = document.getElementById("jokerSuitPicker");
    picker.innerHTML = "";
    ["HEARTS", "DIAMONDS", "CLUBS", "SPADES"].forEach(suit => {
        const btn = document.createElement("button");
        btn.className = "joker-suit-btn";
        btn.innerHTML = `${suitSymbol(suit)} ${suitName(suit)}`;
        btn.onclick = () => {
            picker.querySelectorAll(".joker-suit-btn").forEach(b => b.style.background = "");
            btn.style.background = "rgba(179,39,201,0.4)";
            pendingJokerSuit = suit;
        };
        picker.appendChild(btn);
    });
    pendingJokerSuit = null;
    document.getElementById("jokerModal").classList.remove("hidden");
}

function confirmJoker(announcement) {
    if (!pendingJokerSuit) {
        setMsg("აირჩიე ცვეტი", "error");
        return;
    }
    document.getElementById("jokerModal").classList.add("hidden");
    playSelectedCard(announcement, pendingJokerSuit);
}

function cancelJoker() {
    document.getElementById("jokerModal").classList.add("hidden");
    selectedCard = null;
    document.querySelectorAll(".joker-card.selected").forEach(c => c.classList.remove("selected"));
}

// --- Play card ---
async function playSelectedCard(jokerCall, declaredSuit) {
    if (!selectedCard) return;
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API}/${roomCode}/play`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                suit:         selectedCard.suit || "NONE",
                value:        selectedCard.value,
                jokerCall:    jokerCall,
                declaredSuit: declaredSuit
            })
        });
        if (!res.ok) {
            const err = await res.text();
            setMsg(err || "არასწორი სვლა", "error");
        } else {
            selectedCard = null;
            await loadState();
        }
    } catch (e) {
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- Bid ---
let currentBid = 0;

function changeBid(delta) {
    const val = document.getElementById("bidValue");
    currentBid = Math.max(0, currentBid + delta);
    val.textContent = currentBid;
}

async function submitBid() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API}/${roomCode}/bid`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ bid: currentBid })
        });
        if (!res.ok) {
            const err = await res.text();
            setMsg(err || "შეცდომა", "error");
        } else {
            currentBid = 0;
            document.getElementById("bidValue").textContent = "0";
            await loadState();
        }
    } catch (e) {
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- Set trump ---
async function setTrump(suit) {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API}/${roomCode}/trump`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ suit })
        });
        if (!res.ok) {
            const err = await res.text();
            setMsg(err || "შეცდომა", "error");
        } else {
            await loadState();
        }
    } catch (e) {
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- Start game ---
async function startGame() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API}/${roomCode}/start`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            const err = await res.text();
            setMsg(err || "შეცდომა", "error");
        } else {
            await loadState();
        }
    } catch (e) {
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- Helpers ---
function setMsg(text, type) {
    const el = document.getElementById("gameMsg");
    el.textContent = text;
    el.className = "joker-msg" + (type ? " " + type : "");
}

function trumpDisplay(trump) {
    if (!trump || trump === "NONE") return "კოზირი არ არის";
    return suitSymbol(trump) + " " + suitName(trump);
}

function statusDisplay(status) {
    const map = {
        WAITING: "ლოდინი",
        BIDDING: "ჩამოსვლა",
        PLAYING: "თამაში",
        ROUND_END: "რაუნდი დასრულდა",
        FINISHED: "დასრულდა"
    };
    return map[status] || status;
}

function suitSymbol(suit) {
    return { HEARTS: "♥", DIAMONDS: "♦", CLUBS: "♣", SPADES: "♠" }[suit] || "?";
}

function suitName(suit) {
    return { HEARTS: "გული", DIAMONDS: "აგური", CLUBS: "ჯვარი", SPADES: "ყვავი" }[suit] || suit;
}

function suitToClass(suit) {
    return { HEARTS: "hearts", DIAMONDS: "diamonds", CLUBS: "clubs", SPADES: "spades" }[suit] || "spades";
}

function valueDisplay(value) {
    return { 11: "J", 12: "Q", 13: "K", 14: "A" }[value] || String(value);
}

function renderPlayers(players, currPlayerIdx, status) {
    const list = document.getElementById("playersList");
    list.innerHTML = "";

    // ვიღებთ ბექენდიდან დამრიგებლის ინდექსს
    const dealerIdx = gameState.dealer;

    players.forEach((p, i) => {
        const div = document.createElement("div");

        // თუ მოთამაშის ინდექსი ემთხვევა მიმდინარე რიგს, ვადებთ ანთებულ კლასს
        const isCurrentTurn = (i === currPlayerIdx && status !== "WAITING");
        div.className = "joker-player-row" + (isCurrentTurn ? " active-turn" : "");

        // 1. ვამოწმებთ არის თუ არა დამრიგებელი
        const dealerBadge = (i === dealerIdx) ? `<span class="dealer-badge" style="color:var(--gold); font-size:0.75rem; margin-left:6px;">👑 დაარიგა</span>` : "";

        // 2. ბიდების (პროფეციის) და მიმდინარე წაყვანილი ხელების ტექსტი
        const prophecyText = p.prophecy >= 0 ? `ბიდი: ${p.prophecy}` : "ფიქრობს...";
        const currentTricksText = `წაყვანილი: ${p.current}`;

        div.innerHTML = `
            <div>
                <div class="joker-player-name">
                    ${p.username} ${p.userId === myUserId ? "(შენ)" : ""} 
                    ${dealerBadge}
                </div>
                <div class="joker-player-bid">${prophecyText} · ${currentTricksText}</div>
            </div>
            <div class="joker-player-score">${p.totalScore} ქ.</div>
        `;
        list.appendChild(div);

        // 3. სპეციალური შეტყობინება, თუ ჩემი ჯერია ჩამოსვლის
        if (isCurrentTurn && p.userId === myUserId) {
            setMsg("🔮 შენი ჯერია, იმოქმედე!", "success");
        }
    });

    // ცარიელი სლოტების ვიზუალი (თუ ვინმე აკლია)
    const required = gameState.config?.players || 4;
    for (let i = players.length; i < required; i++) {
        const div = document.createElement("div");
        div.className = "joker-player-slot empty";
        div.innerHTML = `<div class="joker-slot-dot empty"></div><span style="color:#555;">ლოდინი...</span>`;
        list.appendChild(div);
    }
}