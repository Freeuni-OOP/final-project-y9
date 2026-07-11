const API = "/api/games/wordle";
const ANBANI = "აბგდევზთიკლმნოპჟრსტუფქღყშჩცძწჭხჯჰ";
const ANBANI_SET = new Set(ANBANI);
const ROWS = ["აბგდევზთიკლ", "მნოპჟრსტუფქ", "ღყშჩცძწჭხჯჰ"];
const HINT_COST = 50;

let puzzleId = null;
let wordLength = 5;
let maxGuesses = 6;
let status = "IN_PROGRESS";
let currentRow = 0;
let currentGuess = "";
let busy = false;
let loadToken = 0;
let hints = [];
let pointsBalance = 0;
let knownPositions = new Set();
let activeGameChallengeId = null;

let boardEl, kbEl, msgEl, subEl, dailyBtn, practiceBtn, hintBtn, pointsEl;

function setBusy(value) {
    busy = value;
    if (dailyBtn) dailyBtn.disabled = value;
    if (practiceBtn) practiceBtn.disabled = value;
    refreshHintButton();
}

function authHeaders(json = true) {
    const h = {};
    if (json) h["Content-Type"] = "application/json";
    const token = localStorage.getItem("token");
    if (token) h["Authorization"] = `Bearer ${token}`;
    return h;
}

function buildBoard() {
    boardEl.innerHTML = "";
    for (let r = 0; r < maxGuesses; r++) {
        const row = document.createElement("div");
        row.className = "board-row";
        row.dataset.row = r;
        for (let c = 0; c < wordLength; c++) {
            const tile = document.createElement("div");
            tile.className = "tile";
            tile.dataset.row = r;
            tile.dataset.col = c;
            row.appendChild(tile);
        }
        boardEl.appendChild(row);
    }
}

function buildKeyboard() {
    kbEl.innerHTML = "";
    ROWS.forEach(letters => {
        const row = document.createElement("div");
        row.className = "kb-row";
        for (const ch of letters) {
            const key = document.createElement("button");
            key.className = "key";
            key.type = "button";
            key.textContent = ch;
            key.dataset.key = ch;
            key.addEventListener("click", () => addLetter(ch));
            row.appendChild(key);
        }
        kbEl.appendChild(row);
    });

    const actionRow = document.createElement("div");
    actionRow.className = "row";

    const enter = document.createElement("button");
    enter.className = "key key--wide";
    enter.type = "button";
    enter.textContent = "ENTER";
    enter.addEventListener("click", submitGuess);

    const del = document.createElement("button");
    del.className = "key key--wide";
    del.type = "button";
    del.textContent = "⌫";
    del.setAttribute("aria-label", "delete");
    del.addEventListener("click", removeLetter);

    actionRow.appendChild(enter);
    actionRow.appendChild(del);
    kbEl.appendChild(actionRow);
}

const tileAt = (r, c) => boardEl.querySelector(`.tile[data-row="${r}"][data-col="${c}"]`);
const keyOf = (ch) => kbEl.querySelector(`.key[data-key="${ch}"]`);

const STATE_CLASS = { CORRECT: "correct", PRESENT: "present", ABSENT: "absent" };
const RANK = { correct: 3, present: 2, absent: 1, "": 0 };

function drawActiveRow() {
    for (let c = 0; c < wordLength; c++) {
        const tile = tileAt(currentRow, c);
        if (!tile) continue;
        const ch = currentGuess[c] || "";
        const hint = status === "IN_PROGRESS" ? hints.find(h => h.position === c) : null;
        if (ch === "" && hint) {
            tile.textContent = hint.letter;
            tile.classList.add("hint-ghost");
        } else {
            tile.textContent = ch;
            tile.classList.remove("hint-ghost");
        }
        tile.classList.toggle("filled", ch !== "");
    }
}

function applyGuessRow(r, guessWord, feedback) {
    for (let c = 0; c < wordLength; c++) {
        const tile = tileAt(r, c);
        if (!tile) continue;
        tile.textContent = guessWord[c] || "";
        const cls = STATE_CLASS[feedback[c]] || "";
        tile.classList.remove("filled");
        if (cls) tile.classList.add(cls);

        const key = keyOf(guessWord[c]);
        if (key && cls) {
            const cur = key.dataset.state || "";
            if (RANK[cls] > RANK[cur]) {
                key.classList.remove("correct", "present", "absent");
                key.classList.add(cls);
                key.dataset.state = cls;
            }
        }
    }
}

function resetKeyboardStates() {
    kbEl.querySelectorAll(".key").forEach(k => {
        k.classList.remove("correct", "present", "absent");
        delete k.dataset.state;
    });
}

function refreshHintButton() {
    if (!hintBtn) return;
    const noneLeft = knownPositions.size >= wordLength;
    hintBtn.disabled = busy || status !== "IN_PROGRESS" || noneLeft || pointsBalance < HINT_COST;
    hintBtn.textContent = noneLeft ? "ყველა ასო ცნობილია" : `მინიშნება (−${HINT_COST})`;
}

function setMessage(text, kind = "") {
    msgEl.textContent = text || "";
    msgEl.className = "msg" + (kind ? " " + kind : "");
}

function render(state) {
    puzzleId = state.puzzleId;
    wordLength = state.wordLength || 5;
    maxGuesses = state.maxGuesses || 6;
    status = state.status;
    const guesses = state.guesses || [];
    currentRow = guesses.length;
    currentGuess = "";
    hints = state.hints || [];
    pointsBalance = state.pointsBalance || 0;

    buildBoard();
    resetKeyboardStates();
    knownPositions = new Set(hints.map(h => h.position));
    guesses.forEach((g, i) => {
        applyGuessRow(i, g.guessWord, g.feedback);
        g.feedback.forEach((f, c) => { if (f === "CORRECT") knownPositions.add(c); });
    });
    drawActiveRow();

    if (pointsEl) pointsEl.textContent = `ქულები: ${pointsBalance}`;
    refreshHintButton();

    if (status === "WON") {
        setMessage("გამოიცანი!", "win");
    } else if (status === "LOST") {
        setMessage("სიტყვა იყო: " + (state.answerWord || "—"), "lose");
    } else {
        setMessage("");
    }
}

function addLetter(ch) {
    if (status !== "IN_PROGRESS" || busy) return;
    if (!ANBANI_SET.has(ch) || currentGuess.length >= wordLength) return;
    currentGuess += ch;
    drawActiveRow();
}

function removeLetter() {
    if (status !== "IN_PROGRESS" || busy || currentGuess.length === 0) return;
    currentGuess = currentGuess.slice(0, -1);
    drawActiveRow();
}

function shakeRow() {
    const row = boardEl.querySelector(`.board-row[data-row="${currentRow}"]`);
    if (!row) return;
    row.classList.add("shake");
    setTimeout(() => row.classList.remove("shake"), 420);
}

async function submitGuess() {
    if (status !== "IN_PROGRESS" || busy) return;
    if (currentGuess.length !== wordLength) {
        setMessage("არ არის საკმარისი ასოები");
        shakeRow();
        return;
    }
    const myToken = ++loadToken;
    setBusy(true);
    try {
        const res = await fetch(`${API}/${puzzleId}/guess`, {
            method: "POST",
            headers: authHeaders(true),
            body: JSON.stringify({ guess: currentGuess })
        });
        if (myToken !== loadToken) return;
        if (res.status === 401 || res.status === 403) {
            setMessage("გთხოვთ გაიაროთ ავტორიზაცია", "lose");
            return;
        }
        if (res.status === 422) {
            setMessage("სიტყვა არ არსებობს");
            shakeRow();
            return;
        }
        if (!res.ok) {
            setMessage("ასეთი სიტყვა ვერ მოიძებნა");
            shakeRow();
            return;
        }
        const state = await res.json();
        render(state);
        if (state.status === "WON" && window.sendTimeAnalytics) {
            window.sendTimeAnalytics(puzzleId, "Wordle", "WORD_PUZZLE", 0);
        }
        if (window.showAchievementToasts && state.newAchievements && state.newAchievements.length) {
            window.showAchievementToasts(state.newAchievements);
        }
        if (state.status === "WON") {
            const guessCount = (state.guesses || []).length;
            if (activeGameChallengeId && window.submitChallengeAttempt) {
                window.submitChallengeAttempt(activeGameChallengeId, guessCount);
            } else if (window.offerGameChallenge) {
                window.offerGameChallenge("WORDLE", puzzleId, guessCount);
            }
        }
    } catch (err) {
        if (myToken !== loadToken) return;
        console.error("guess failed:", err);
        setMessage("კავშირის შეცდომა");
        shakeRow();
    } finally {
        if (myToken === loadToken) setBusy(false);
    }
}

async function useHint() {
    if (status !== "IN_PROGRESS" || busy) return;
    const myToken = ++loadToken;
    setBusy(true);
    try {
        const res = await fetch(`${API}/${puzzleId}/hint`, {
            method: "POST",
            headers: authHeaders(true)
        });
        if (myToken !== loadToken) return;
        if (res.status === 401 || res.status === 403) {
            setMessage("გთხოვთ გაიაროთ ავტორიზაცია", "lose");
            return;
        }
        if (res.status === 402) {
            setMessage("არასაკმარისი ქულები მინიშნებისთვის", "lose");
            return;
        }
        if (res.status === 422) {
            setMessage("ყველა ასო უკვე ცნობილია");
            return;
        }
        if (!res.ok) {
            setMessage("მინიშნება ვერ მოხერხდა");
            return;
        }
        const state = await res.json();
        const typedSoFar = currentGuess;
        render(state);
        currentGuess = typedSoFar;
        drawActiveRow();
        const newHint = state.hints && state.hints[state.hints.length - 1];
        if (newHint) {
            setMessage(`მინიშნება: მე-${newHint.position + 1} პოზიციაზე — „${newHint.letter}“`, "hint");
        }
    } catch (err) {
        if (myToken !== loadToken) return;
        console.error("hint failed:", err);
        setMessage("კავშირის შეცდომა");
    } finally {
        if (myToken === loadToken) setBusy(false);
    }
}

document.addEventListener("keydown", (e) => {
    if (e.ctrlKey || e.metaKey || e.altKey) return;
    if (e.key === "Enter") { submitGuess(); return; }
    if (e.key === "Backspace") { e.preventDefault(); removeLetter(); return; }
    if (e.key.length === 1 && ANBANI_SET.has(e.key)) addLetter(e.key);
});

async function load(endpoint, method) {
    const myToken = ++loadToken;
    setBusy(true);
    setMessage("");
    try {
        const res = await fetch(endpoint, { method, headers: authHeaders(method === "POST") });
        if (myToken !== loadToken) return;
        if (res.status === 401 || res.status === 403) {
            setMessage("გთხოვთ გაიაროთ ავტორიზაცია", "lose");
            return;
        }
        if (!res.ok) throw new Error("load failed: " + res.status);
        render(await res.json());
    } catch (err) {
        if (myToken !== loadToken) return;
        console.error("load failed:", err);
        setMessage("თამაში ვერ ჩაიტვირთა", "lose");
    } finally {
        if (myToken === loadToken) setBusy(false);
    }
}

function loadDaily() {
    subEl.textContent = "დღის სიტყვა";
    load(`${API}/daily`, "GET");
}

function loadChallenge(challengePuzzleId) {
    subEl.textContent = "\uD83C\uDFC6 მეგობრის გამოწვევა";
    load(`${API}/${challengePuzzleId}`, "GET");
}

function loadPractice() {
    if (localStorage.getItem('role') === 'GUEST') {
        showInfoToast('You have to log in');
        return;
    }
    subEl.textContent = "ვარჯიში";
    load(`${API}/practice`, "POST");
}

document.addEventListener("DOMContentLoaded", () => {
    boardEl = document.getElementById("board");
    kbEl = document.getElementById("keyboard");
    msgEl = document.getElementById("msg");
    subEl = document.getElementById("sub");
    dailyBtn = document.getElementById("daily-btn");
    practiceBtn = document.getElementById("practice-btn");
    hintBtn = document.getElementById("hint-btn");
    pointsEl = document.getElementById("points-balance");

    buildBoard();
    buildKeyboard();

    const params = new URLSearchParams(location.search);
    const mode = params.get("mode");
    const challengePuzzleId = params.get("challengeId");
    activeGameChallengeId = params.get("gcid");

    if (challengePuzzleId) loadChallenge(challengePuzzleId);
    else if (mode === "practice") loadPractice();
    else loadDaily();
});