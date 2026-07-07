let timerInterval = null;
let secondsElapsed = 0;
let activePuzzleId = null;
let currentSolution = "";

document.addEventListener("DOMContentLoaded", () => {
    determineInitialRouting();
});

async function determineInitialRouting() {
    const urlParams = new URLSearchParams(window.location.search);
    const challengeId = urlParams.get('challengeId');

    if (challengeId) {
        fetchPuzzleData(`/api/sudoku/board?challengeId=${challengeId}`, "🏆 Friend Challenge Mode");
    } else {
        fetchPuzzleData("/api/sudoku/daily", "📅 Daily Puzzle Challenge");
    }
}

async function loadSoloPracticeGame() {
    window.history.pushState({}, document.title, window.location.pathname);
    fetchPuzzleData("/api/sudoku/board?difficulty=MEDIUM", "🕹️ Solo Practice Game");
}

async function fetchPuzzleData(endpoint, titleLabelText) {
    try {
        const token = localStorage.getItem('token');
        const headers = { "Content-Type": "application/json" };
        if (token) headers["Authorization"] = `Bearer ${token}`;

        const res = await fetch(endpoint, { method: "GET", headers: headers });
        if (!res.ok) throw new Error("Could not load puzzle data mapping properties.");

        const puzzle = await res.json();
        activePuzzleId = puzzle.id;
        currentSolution = puzzle.solution;

        document.getElementById('mode-title').innerText = "SUDOKU";
        assembleGridElements(puzzle.definition);
        initTimerClock();
    } catch (err) {
        console.error("Sudoku engine failure initialization:", err);
        document.getElementById('sudoku-grid').innerHTML =
            `<p style="color:#ff6b9d; padding: 20px; grid-column: 1/-1; text-align:center; font-weight:bold;">❌ Failed to load board layout.</p>`;
    }
}

function assembleGridElements(boardDefinition) {
    const container = document.getElementById('sudoku-grid');
    container.innerHTML = '';

    for (let i = 0; i < 81; i++) {
        const char = boardDefinition[i];
        const cell = document.createElement('input');

        cell.type = 'text';
        cell.maxLength = 1;
        cell.className = 'sudoku-cell';

        cell.oninput = function() {
            this.value = this.value.replace(/[^1-9]/g, '');
            highlightMatchingNumbers(this.value); // Re-trigger match glow if user changes a value
        };

        cell.addEventListener('focus', () => highlightMatchingNumbers(cell.value));
        cell.addEventListener('click', () => highlightMatchingNumbers(cell.value));

        cell.addEventListener('blur', clearAllHighlights);

        if (char !== '0') {
            cell.value = char;
            cell.readOnly = true; // Still interactive, but value locked down
            cell.classList.add('is-clue');
        }

        container.appendChild(cell);
    }
}


function highlightMatchingNumbers(targetValue) {
    clearAllHighlights();

    const valueToMatch = targetValue ? targetValue.trim() : "";
    if (valueToMatch === "") return; // Skip calculation if cell is completely empty

    const cells = document.querySelectorAll('.sudoku-cell');
    cells.forEach(cell => {
        if (cell.value && cell.value.trim() === valueToMatch) {
            cell.classList.add('highlight-number-match');
        }
    });
}

function clearAllHighlights() {
    const cells = document.querySelectorAll('.sudoku-cell');
    cells.forEach(cell => cell.classList.remove('highlight-number-match'));
}

function initTimerClock() {
    clearInterval(timerInterval);
    secondsElapsed = 0;
    const timerElement = document.getElementById('sudoku-timer');

    timerInterval = setInterval(() => {
        secondsElapsed++;
        const displayMins = String(Math.floor(secondsElapsed / 60)).padStart(2, '0');
        const displaySecs = String(secondsElapsed % 60).padStart(2, '0');
        timerElement.innerText = `${displayMins}:${displaySecs}`;
    }, 1000);
}

async function submitSolutionCheck() {
    const cells = document.querySelectorAll('.sudoku-cell');
    let submissionString = "";
    cells.forEach(cell => submissionString += (cell.value.trim() === "") ? "0" : cell.value.trim());

    if (submissionString.includes("0")) {
        alert("⚠️ The board is incomplete!");
        return;
    }

    if (currentSolution === submissionString) {
        clearInterval(timerInterval);
        alert(`🏆 Perfect! You solved the puzzle in ${document.getElementById('sudoku-timer').innerText}!`);
        if (window.sendTimeAnalytics) {
            window.sendTimeAnalytics(activePuzzleId, "Sudoku", "BOARD_PUZZLE", secondsElapsed);
        }
    } else {
        alert("❌ There are some mistakes on your board. Keep tracking!");
    }
}

function giveUpAndReveal() {
    if (!currentSolution) return;

    const confirmSurrender = confirm("Are you sure you want to give up and reveal the full solution?");
    if (!confirmSurrender) return;

    clearInterval(timerInterval);
    const cells = document.querySelectorAll('.sudoku-cell');

    cells.forEach((cell, index) => {
        cell.value = currentSolution[index];
        cell.readOnly = true;
        if (!cell.classList.contains('is-clue')) {
            cell.style.color = '#e194d5';
        }
    });

    alert("🏳️ Game Over. Solution grid values filled out completely.");
}

function generateChallengeLink() {
    if (!activePuzzleId) return;
    const challengeUrl = `${window.location.origin}/sudoku?challengeId=${activePuzzleId}`;
    navigator.clipboard.writeText(challengeUrl).then(() => {
        alert("🔗 Challenge Link copied to clipboard!");
    });
}