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
    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);
    //stompClient.debug = null;

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
    if (event.type === "NEW_ROUND") {
        // loadState() ისედაც ხდება ყოველ event-ზე, ასე რომ დამატებითი არაფერი არ გჭირდება
        // მაგრამ თუ გინდა სპეციალური მესიჯი:
        setMsg("🎴 ახალი რაუნდი დაიწყო!", "success");
        setTimeout(() => setMsg("", ""), 2000);
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
    const badge = document.getElementById("roomTypeBadge");
    if (badge) {
        const isPublic = gameState.config?.allowRandoms;
        badge.textContent = isPublic ? "საჯარო" : "კერძო";
        badge.className = "joker-room-type-badge " + (isPublic ? "public" : "private");
    }
    const leaveBtn = document.getElementById("leaveBtn");
    if (leaveBtn) {
        leaveBtn.style.display = (status === "WAITING") ? "block" : "none";
    }
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

    const showBidPanel = (status === "BIDDING" && isMyTurn);
    document.getElementById("bidPanel").classList.toggle("hidden", !showBidPanel);

    if (showBidPanel) {
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

    // status message — always computed last so it's not overwritten by anything else
    if (status === "WAITING") {
        setMsg("⏳ ველოდებით მოთამაშეების შევსებას მაგიდასთან...", "info");
    }
    else if (status === "BIDDING") {
        if (isMyTurn) {
            // შეტყობინება უშუალოდ იმ იუზერისთვის, ვისი პრედიქციის დროც არის
            setMsg("🔮 შენი პრედიქციის დროა! აირჩიე ციფრი ქვემოთ პანელზე და დააჭირე დადასტურებას.", "success");
        } else {
            // შეტყობინება ყველასთვის, თუ ვინ ფიქრობს ამ წამს
            const thinkingPlayer = players[currPlayerIdx]?.username || "მოთამაშე";
            setMsg(`💭 ფიქრობს პრედიქციაზე: ${thinkingPlayer}...`, "info");
        }
    }
    else if (status === "PLAYING") {
        if (isMyTurn) {
            setMsg("🃏 შენი ჩამოსვლის დროა! აირჩიე კარტი შენი ხელიდან.", "success");
        } else {
            const activePlayer = players[currPlayerIdx]?.username || "მოთამაშე";
            setMsg(`🕐 ჩამოდის კარტს: ${activePlayer}...`, "info");
        }
    }
    else if (status === "ROUND_END") {
        setMsg("🏁 რაუნდი დასრულდა! ითვლება ქულები...", "info");
    }
}

function renderPlayers(players, currPlayerIdx, status) {
    const list = document.getElementById("playersList");
    list.innerHTML = "";

    const dealerIdx = gameState.dealer;

    players.forEach((p, i) => {
        const div = document.createElement("div");

        // თუ მოთამაშის ჯერია, ვანთებთ ეკრანზე
        const isCurrentTurn = (i === currPlayerIdx && status !== "WAITING");
        div.className = "joker-player-row" + (isCurrentTurn ? " active-turn" : "");

        // დამრიგებლის ნიშნაკი
        const dealerBadge = (i === dealerIdx) ? `<span class="dealer-badge" style="color:var(--gold); font-size:0.75rem; margin-left:6px;">👑 დამრიგებელი</span>` : "";

        // დინამიური ტექსტი: თუ თამაში ჯერ BIDDING ფაზაშია და იუზერმა უკვე თქვა, გამოვაჩინოთ, თორემ — "ფიქრობს..."
        // (თუ მიმდინარე ინდექსი მასზე წინაა, ესე იგი უკვე თქვა)
        let prophecyStatus = "ფიქრობს...";

        // თუ თამაშის სტატუსი უკვე PLAYING-ია, ბიდები ყველასი ვიცით
        if (status === "PLAYING" || status === "ROUND_END") {
            prophecyStatus = `ბიდი: ${p.prophecy}`;
        } else if (status === "BIDDING") {
            // ბიდინგის დროს გამოვაჩინოთ მხოლოდ მათთვის, ვინც უკვე თქვა
            if (i !== currPlayerIdx && p.prophecy >= 0) {
                prophecyStatus = `ბიდი: ${p.prophecy}`;
            }
        }

        // რეალურად წაყვანილი ხელები მიმდინარე რაუნდში
        const takenTricksText = `წაყვანილი: ${p.current}`;

        div.innerHTML = `
            <div>
                <div class="joker-player-name">
                    ${p.username} ${p.userId === myUserId ? "(შენ)" : ""} 
                    ${dealerBadge}
                </div>
                <div class="joker-player-bid" style="font-size:0.85rem; color:#aaa;">
                    <span style="color:var(--gold);">${prophecyStatus}</span> | ${takenTricksText}
                </div>
            </div>
            <div class="joker-player-score" style="font-weight:bold; color:#fff;">${p.totalScore} ქ.</div>
        `;
        list.appendChild(div);
    });

    // ცარიელი სლოტები
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

async function leaveRoom() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API}/${roomCode}/leave`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.ok) {
            window.location.href = "/joker";
        } else {
            const err = await res.text();
            setMsg(err || "შეცდომა", "error");
        }
    } catch (e) {
        setMsg("კავშირის შეცდომა", "error");
    }
}