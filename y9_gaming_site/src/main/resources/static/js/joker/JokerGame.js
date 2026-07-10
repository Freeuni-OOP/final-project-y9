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

    // get my userId (ველოდებით პასუხს აუცილებლად!)
    try {
        const res = await fetch("/api/users/me", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.ok) {
            const me = await res.json();
            myUserId = me.id;
        }
    } catch (e) {
        console.error("Failed to load user", e);
    }

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

    // players list & history
    renderPlayers(players, currPlayerIdx, status);
    renderHistory(players);

    // trump setter panel
    const dealerIdx = gameState.dealer;
    const isDealer  = players[dealerIdx]?.userId === myUserId;
    const needsTrump = (trump === null || trump === "NONE") && status === "BIDDING";
    document.getElementById("trumpSetterPanel").classList.toggle("hidden", !(isDealer && needsTrump));


    // --- bid panel (სრული მათემატიკური წრე ბიდების რაოდენობის მიხედვით) ---
    let activePlayer = null;

    if (status === "BIDDING") {
        // 1. ვითვლით, რამდენმა ადამიანმა თქვა უკვე ბიდი
        const biddedCount = players.filter(p => p.prophecy >= 0).length;

        // 2. ფორმულა: (დამრიგებელი + 1 + უკვე ნათქვამი ბიდები) ნაშთი მოთამაშეების რაოდენობაზე
        const activeIdx = (gameState.dealer + 1 + biddedCount) % players.length;
        activePlayer = players[activeIdx];
    } else {
        // თამაშის (PLAYING) ფაზაში ჩვეულებრივ ვენდობით ბექენდის ინდექსს
        activePlayer = players[currPlayerIdx];
    }

    // დაზღვევა: თუ მაინც რამე გაუთვალისწინებელი მოხდა, ვიპოვოთ პირველი, ვისაც ბიდი არ უთქვამს
    if (!activePlayer && status === "BIDDING") {
        activePlayer = players.find(p => p.prophecy === undefined || p.prophecy < 0);
    }

    // 🌟 იმისათვის, რომ renderPlayers-შიც სწორი მწვანე ჩარჩო (აქტიური ტური) დაიხატოს,
    // გადავაწეროთ ლოკალური ცვლადი რეალური ინდექსით
    const realActiveIdx = activePlayer ? players.indexOf(activePlayer) : currPlayerIdx;

    const isMyTurn = activePlayer && String(activePlayer.userId) === String(myUserId);
    const myPlayer = players.find(p => String(p.userId) === String(myUserId));

    const bidPanel = document.getElementById("bidPanel");
    const bidInfoEl = document.getElementById("bidInfo");
    const typeDisplay = document.getElementById("gameTypeDisplay");

    const bidControls = document.getElementById("bidControls");
    const submitBidBtn = document.getElementById("submitBidBtn");

    const playTurnPanel = document.getElementById("playTurnPanel");
    const playTurnInfoEl = document.getElementById("playTurnInfo");

    // ბიდინგის პანელი ჩანს მხოლოდ BIDDING-ში
    bidPanel.classList.toggle("hidden", status !== "BIDDING");
    // ჩამოსვლის ("ვისი ჯერია") პანელი ჩანს მხოლოდ PLAYING-ში
    if (playTurnPanel) playTurnPanel.classList.toggle("hidden", status !== "PLAYING");

    if (status === "BIDDING") {
        const totalCards = gameState.cardsThisRound; // მიმდინარე რაუნდის კარტები

        // ვითვლით უკვე თქმული ბიდების ჯამს
        let totalExistingBids = 0;
        let biddedPlayersCount = 0;

        players.forEach(p => {
            if (p.prophecy >= 0) {
                totalExistingBids += p.prophecy;
                biddedPlayersCount++;
            }
        });

        const activePlayerName = activePlayer ? activePlayer.username : "მოთამაშე";
        const isLastPlayer = (biddedPlayersCount === players.length - 1);
        let forbiddenBid = totalCards - totalExistingBids;

        if (isMyTurn) {
            if (bidControls) bidControls.style.display = "flex";
            if (submitBidBtn) submitBidBtn.style.display = "block";

            // ტექსტური მინიშნება უშუალოდ შენთვის
            if (isLastPlayer && forbiddenBid >= 0 && forbiddenBid <= totalCards) {
                bidInfoEl.innerHTML = `
                    <div style="font-size: 1.2rem; color: var(--gold); font-weight: bold; margin-bottom: 5px;">🔮 შენი ჯერია!</div>
                    <div style="color: #eee;">რა არის შენი პრედიქცია? (მიუთითე 0 – ${totalCards})</div>
                    <div style="color: var(--pink); font-weight: bold; margin-top: 5px; font-size: 0.9rem;">⚠️ შენ ხარ ბოლო (დამრიგებელი), ${forbiddenBid}-ს თქმა არ შეგიძლია!</div>
                `;
            } else {
                bidInfoEl.innerHTML = `
                    <div style="font-size: 1.2rem; color: var(--gold); font-weight: bold; margin-bottom: 5px;">🔮 შენი ჯერია!</div>
                    <div style="color: #eee;">რა არის შენი პრედიქცია? (მიუთითე 0 – ${totalCards} ხელი)</div>
                `;
            }
        } else {
            // ⏳ 2. თუ სხვისი ჯერია - სრულიად ვმალავთ ღილაკებს და ვწერთ მოთამაშის სახელს
            if (bidControls) bidControls.style.display = "none";
            if (submitBidBtn) submitBidBtn.style.display = "none";

            bidInfoEl.innerHTML = `
                <div style="padding: 10px 0; text-align: center;">
                    <span style="color: #aaa; font-size: 1rem;">💭 პრედიქციის მოლოდინი:</span> 
                    <b style="color: var(--orchid-2); font-size: 1.2rem; margin-left: 5px;">${activePlayerName}</b>
                </div>
            `;
        }


    }

    // --- თამაშის ტიპი (შეტენვა/წაგლეჯვა) — ჩანს ბიდინგის დასრულების შემდეგაც, მთელი რაუნდის განმავლობაში ---
    if (typeDisplay) {
        if (status === "BIDDING" || status === "PLAYING" || status === "ROUND_END") {
            const totalCards = gameState.cardsThisRound;
            let totalExistingBids = 0;
            players.forEach(p => { if (p.prophecy >= 0) totalExistingBids += p.prophecy; });
            const biddedPlayersCount = players.filter(p => p.prophecy >= 0).length;

            typeDisplay.classList.remove("hidden");
            typeDisplay.classList.remove("gt-neutral", "gt-shetenva", "gt-tsaglejva");

            if (totalExistingBids > totalCards) {
                typeDisplay.textContent = "💥 თამაშის ტიპი: წაგლეჯვა";
                typeDisplay.classList.add("gt-tsaglejva");
            } else if (totalExistingBids < totalCards && biddedPlayersCount === players.length) {
                typeDisplay.textContent = "🃏 თამაშის ტიპი: შეტენვა";
                typeDisplay.classList.add("gt-shetenva");
            } else {
                typeDisplay.textContent = `მიმდინარე ბიდების ჯამი: ${totalExistingBids} / ${totalCards}`;
                typeDisplay.classList.add("gt-neutral");
            }
        } else {
            typeDisplay.classList.add("hidden");
        }
    }

    // --- ჩამოსვლის ("ვისი ჯერია") პანელი — PLAYING ფაზაში, იგივე ლოგიკით რაც ბიდინგში ---
    if (status === "PLAYING" && playTurnInfoEl) {
        if (isMyTurn) {
            playTurnInfoEl.innerHTML = `
                <div style="font-size: 1.2rem; color: var(--gold); font-weight: bold; margin-bottom: 5px;">🃏 შენი ჩამოსვლის დროა!</div>
                <div style="color: #eee;">აირჩიე კარტი შენი ხელიდან</div>
            `;
        } else {
            const waitingName = activePlayer ? activePlayer.username : "მოთამაშე";
            playTurnInfoEl.innerHTML = `
                <div style="padding: 10px 0; text-align: center;">
                    <span style="color: #aaa; font-size: 1rem;">🕐 ელოდება სვლას:</span>
                    <b style="color: var(--orchid-2); font-size: 1.2rem; margin-left: 5px;">${waitingName}</b>
                </div>
            `;
        }
    }

    // trick cards
    renderTrick(gameState.currentTrick);

    // hand cards
    renderHand(myPlayer?.cardList || [], status, isMyTurn);

    // --- სტატუს მესიჯების ბარი (ამოღებულია BIDDING-ის დუბლიკატები) ---
    if (status === "WAITING") {
        setMsg("⏳ ველოდებით მოთამაშეების შევსებას მაგიდასთან...", "info");
    }
    else if (status === "BIDDING") {
        // ბიდინგის დროს ზედა ზოლი ცარიელია, რადგან პანელი თავად აკეთებს ყველაფერს სუფთად
        setMsg("", "");
    }
    else if (status === "PLAYING") {
        setMsg("", ""); // ინფორმაცია უკვე playTurnPanel-ში ჩანს
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

        const isCurrentTurn = (i === currPlayerIdx && status !== "WAITING");
        div.className = "joker-player-row" + (isCurrentTurn ? " active-turn" : "");

        const dealerBadge = (i === dealerIdx) ? `<span class="dealer-badge" style="color:var(--gold); font-size:0.75rem; margin-left:6px;">👑 დამრიგებელი</span>` : "";

        // დინამიური სტატუსის ტექსტი
        let prophecyStatus = "";

        if (status === "PLAYING" || status === "ROUND_END") {
            prophecyStatus = `ბიდი: ${p.prophecy}`;
        } else if (status === "BIDDING") {
            if (p.prophecy >= 0) {
                prophecyStatus = `ბიდი: ${p.prophecy}`;
            } else if (i === currPlayerIdx) {
                prophecyStatus = "ფიქრობს...";
            } else {
                prophecyStatus = "ელოდება";
            }
        } else {
            prophecyStatus = "ლოდინი";
        }

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
        const playedSoFar = (gameState.currentTrick && gameState.currentTrick.playedCards) || [];
        const isLeading = playedSoFar.length === 0;
        openJokerModal(card, isLeading);
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

function openJokerModal(card, isLeading) {
    const suitSection = document.getElementById("jokerSuitSection");
    const titleEl = document.getElementById("jokerModalTitle");

    if (isLeading) {
        // ✅ ლიდერობისას — ცვეტის არჩევაც საჭიროა
        if (titleEl) titleEl.textContent = "ჯოკერის მოთხოვნა";
        if (suitSection) suitSection.classList.remove("hidden");

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

        pendingJokerSuit = null; // ჯერ არ არჩეულა ცვეტი
    } else {
        if (titleEl) titleEl.textContent = "ჯოკერის გამოყენება";
        if (suitSection) suitSection.classList.add("hidden");

        pendingJokerSuit = "NONE"; // ცვეტი არარელევანტურია, წინასწარ ვავსებთ
    }

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
            let displayMsg = err;
            if (err.includes("illegal card")) {
                displayMsg = "თქვენ ვერ ითამაშებთ ამ ბარათს — არასწორი სვლაა!";
            }
            setMsg(displayMsg || "არასწორი სვლა", "error");
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

function toggleHistoryModal() {
    const modal = document.getElementById("historyModal");
    if (modal) {
        modal.classList.toggle("hidden");
    }
}


function renderHistory(players) {
    const wrap = document.getElementById("historyTableWrap");
    if (!wrap) return;

    if (!players || !players.length || !players[0].roundHistory || players[0].roundHistory.length === 0) {
        wrap.innerHTML = `<p style="color: #888; font-size: 0.9rem; text-align: center; padding: 20px;">ჯერ არცერთი რაუნდი არ დასრულებულა. ცხრილი შეივსება პირველივე რაუნდის ქულების დათვლისას.</p>`;
        return;
    }

    const roundCount = players[0].roundHistory.length;

    let html = `<table class="joker-history-table" style="width:100%; border-collapse:collapse;">
                    <thead>
                        <tr style="border-bottom: 2px solid rgba(179,39,201,0.4);">
                            <th style="padding: 10px; color:var(--orchid-2);">რდ</th>`;

    players.forEach(p => {
        html += `<th style="padding: 10px; color:var(--orchid-2);">${p.username}</th>`;
    });

    html += `           </tr>
                    </thead>
                    <tbody>`;

    for (let r = 0; r < roundCount; r++) {
        html += `<tr style="border-bottom: 1px solid rgba(255,255,255,0.05);">
                    <td style="padding: 10px; font-weight: bold; color: #aaa;">${players[0].roundHistory[r].round}</td>`;

        players.forEach(p => {
            const rec = p.roundHistory[r];
            if (!rec) return;

            const hit = rec.bid === rec.taken;
            const textStyle = hit ? "color: #5af55a; background: rgba(90,245,90,0.02);" : "color: var(--pink); background: rgba(255,107,157,0.02);";
            const scoreSign = rec.score >= 0 ? "+" : "";

            html += `<td style="padding: 8px; text-align: center; ${textStyle}">
                        <div style="font-weight: bold; font-size:0.9rem;">${rec.bid} ➔ ${rec.taken}</div>
                        <span style="font-size: 0.72rem; opacity: 0.85;">(${scoreSign}${rec.score})</span>
                     </td>`;
        });

        html += `</tr>`;
    }

    // 🏆 სულ (ჯამური ქულები)
    html += `<tr class="hist-total-row" style="border-top: 2px solid rgba(179,39,201,0.5); background: rgba(224, 161, 27, 0.05);">
                <td style="padding: 12px; font-weight: bold; color: var(--gold);">სულ</td>`;
    players.forEach(p => {
        html += `<td style="padding: 12px; font-weight: bold; color: var(--gold); font-size: 1rem;">${p.totalScore}</td>`;
    });
    html += `</tr>`;

    html += `</tbody></table>`;
    wrap.innerHTML = html;
}