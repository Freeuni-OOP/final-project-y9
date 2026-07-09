let timerInterval = null;
let secondsElapsed = 0;
let activePuzzleId = null;
let currentSolution = "";

let hintsUsed = 0;
const MAX_HINTS = 3;
let autoCheckEnabled = false;

let mistakesCommitted = 0;
const MAX_MISTAKES = 3;
let isGameOver = false;

document.addEventListener("DOMContentLoaded", () => {
    determineInitialRouting();
    initControlControls();

    document.addEventListener('click', (e) => {
        if (!e.target.classList.contains('sudoku-cell') && e.target.id !== 'hint-btn') {
            clearAllHighlights();
        }
    });
});

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `custom-toast toast-${type}`;

    let prefix = "ℹ️ ";
    if (type === 'success') prefix = "🏆 ";
    if (type === 'error') prefix = "❌ ";
    if (type === 'warn') prefix = "⚠️ ";

    toast.innerText = prefix + message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 4800);
}

function initControlControls() {
    const autoCheckCheckbox = document.getElementById('auto-check-toggle');
    if (autoCheckCheckbox) {
        autoCheckEnabled = autoCheckCheckbox.checked;
        updateMistakesUIVisibility();
        autoCheckCheckbox.addEventListener('change', (e) => {
            autoCheckEnabled = e.target.checked;
            updateMistakesUIVisibility();
            runBoardAutoCheckScan();
        });
    }
}

function updateMistakesUIVisibility() {
    const counterElement = document.getElementById('mistakes-counter');
    if (counterElement) {
        counterElement.style.display = autoCheckEnabled ? 'block' : 'none';
    }
}

async function determineInitialRouting() {
    const urlParams = new URLSearchParams(window.location.search);
    const challengeId = urlParams.get('challengeId');

    if (challengeId) {
        fetchPuzzleData(`/api/sudoku/board?challengeId=${challengeId}`, "🏆 Friend Challenge Mode");
    } else {
        fetchPuzzleData("/api/sudoku/daily", "📅 Daily Puzzle Challenge");
    }
}

function openDifficultyModal() {
    document.getElementById('difficulty-modal').classList.add('modal-open');
}

function closeDifficultyModal() {
    document.getElementById('difficulty-modal').classList.remove('modal-open');
}

function selectDifficulty(level) {
    closeDifficultyModal();
    window.history.pushState({}, document.title, window.location.pathname);
    fetchPuzzleData(`/api/sudoku/board?difficulty=${level}`, `🕹️ Solo Practice (${level})`);
    showToast(`Loading fresh ${level.toLowerCase()} game grid. Good luck!`, 'info');
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

        hintsUsed = 0;
        mistakesCommitted = 0;
        isGameOver = false;
        updateHintButtonLabel();
        updateMistakesUIVisibility();
        updateMistakesUI();

        document.getElementById('mode-title').innerText = "SUDOKU";
        assembleGridElements(puzzle.definition);
        initTimerClock();
    } catch (err) {
        console.error("Sudoku engine failure initialization:", err);
        document.getElementById('sudoku-grid').innerHTML =
            `<p style="color:#ff6b9d; padding: 20px; grid-column: 1/-1; text-align:center; font-weight:bold;">❌ Failed to load board layout.</p>`;
        showToast("Failed to initialize game map coordinates.", "error");
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
        cell.dataset.index = i;

        cell.oninput = function() {
            if (isGameOver) {
                this.value = "";
                return;
            }

            const previousValue = this.dataset.lastValidValue || "";
            this.value = this.value.replace(/[^1-9]/g, '');

            if (this.value === previousValue) return;

            if (autoCheckEnabled) {
                evaluateCellAccuracyWithStrikes(this, previousValue);
            } else {
                this.classList.remove('check-error', 'check-correct');
                this.dataset.lastValidValue = this.value;
            }

            highlightMatchingNumbers(this.value);
        };

        cell.addEventListener('focus', () => {
            if (isGameOver) return;
            highlightMatchingNumbers(cell.value);
            document.querySelectorAll('.sudoku-cell').forEach(c => c.classList.remove('focused-cell'));
            cell.classList.add('focused-cell');
        });

        cell.addEventListener('click', (e) => {
            e.stopPropagation();
            if (isGameOver) return;
            highlightMatchingNumbers(cell.value);
        });

        if (char !== '0') {
            cell.value = char;
            cell.readOnly = true;
            cell.classList.add('is-clue');
            cell.dataset.lastValidValue = char;
        } else {
            cell.dataset.lastValidValue = "";
        }

        container.appendChild(cell);
    }
}

function evaluateCellAccuracy(cell) {
    const idx = parseInt(cell.dataset.index, 10);
    const val = cell.value.trim();

    if (val === "") {
        cell.classList.remove('check-error', 'check-correct');
        return;
    }

    if (!currentSolution || cell.classList.contains('is-clue')) return;

    if (val === currentSolution[idx]) {
        cell.classList.remove('check-error');
        cell.classList.add('check-correct');
    } else {
        cell.classList.remove('check-correct');
        cell.classList.add('check-error');
    }
}

function evaluateCellAccuracyWithStrikes(cell, previousValue) {
    const idx = parseInt(cell.dataset.index, 10);
    const val = cell.value.trim();

    if (val === "") {
        cell.classList.remove('check-error', 'check-correct');
        cell.dataset.lastValidValue = "";
        return;
    }

    if (!currentSolution || cell.classList.contains('is-clue')) return;

    if (val === currentSolution[idx]) {
        cell.classList.remove('check-error');
        cell.classList.add('check-correct');
        cell.dataset.lastValidValue = val;
    } else {
        cell.classList.remove('check-correct');
        cell.classList.add('check-error');

        mistakesCommitted++;
        updateMistakesUI();
        showToast(`Mistake detected! (${mistakesCommitted}/${MAX_MISTAKES} strikes used)`, 'error');
        cell.dataset.lastValidValue = val;

        if (mistakesCommitted >= MAX_MISTAKES) {
            triggerSudokuLoss();
        }
    }
}

function updateMistakesUI() {
    const counterElement = document.getElementById('mistakes-counter');
    if (!counterElement) return;

    if (isGameOver && mistakesCommitted >= MAX_MISTAKES) {
        counterElement.innerText = "☠️ GAME OVER";
        return;
    }

    let hearts = "";
    for (let i = 0; i < MAX_MISTAKES; i++) {
        hearts += (i < mistakesCommitted) ? "💔 " : "❤️ ";
    }
    counterElement.innerText = hearts.trim();
}

function triggerSudokuLoss() {
    isGameOver = true;
    clearInterval(timerInterval);

    showToast("Too many mistakes! Game Over. Revealing the solution...", "error");

    const cells = document.querySelectorAll('.sudoku-cell');
    cells.forEach((cell, index) => {
        cell.readOnly = true;
        cell.blur();
        if (!cell.classList.contains('is-clue')) {
            cell.value = currentSolution[index];
            cell.classList.remove('check-error');
            cell.classList.add('check-correct');
            cell.style.color = '#ff6b9d';
        }
    });

    updateMistakesUI();
}

function runBoardAutoCheckScan() {
    if (isGameOver) return;

    const cells = document.querySelectorAll('.sudoku-cell');
    cells.forEach(cell => {
        if (autoCheckEnabled) {
            const idx = parseInt(cell.dataset.index, 10);
            const val = cell.value.trim();
            if (val !== "" && !cell.classList.contains('is-clue')) {
                if (val === currentSolution[idx]) {
                    cell.classList.add('check-correct');
                } else {
                    cell.classList.add('check-error');
                }
            }
        } else {
            cell.classList.remove('check-error', 'check-correct');
        }
    });
}

function triggerBoardCellHint() {
    if (isGameOver || !currentSolution) return;
    if (hintsUsed >= MAX_HINTS) {
        showToast("You have already exhausted your 3 available hints for this puzzle challenge session!", "warn");
        return;
    }

    let targetCell = document.querySelector('.sudoku-cell.focused-cell');

    if (!targetCell || targetCell.classList.contains('is-clue') || targetCell.value === currentSolution[parseInt(targetCell.dataset.index, 10)]) {
        const cells = Array.from(document.querySelectorAll('.sudoku-cell'));
        targetCell = cells.find(cell => {
            if (cell.classList.contains('is-clue')) return false;
            const idx = parseInt(cell.dataset.index, 10);
            return cell.value.trim() !== currentSolution[idx];
        });
    }

    if (!targetCell) {
        showToast("Excellent progress! Every element currently matches perfectly.", "success");
        return;
    }

    const cellIdx = parseInt(targetCell.dataset.index, 10);
    targetCell.value = currentSolution[cellIdx];
    targetCell.dataset.lastValidValue = currentSolution[cellIdx];
    targetCell.classList.add('is-hint-provided');

    if (autoCheckEnabled) {
        targetCell.classList.remove('check-error');
        targetCell.classList.add('check-correct');
    }

    hintsUsed++;
    updateHintButtonLabel();
    highlightMatchingNumbers(targetCell.value);
}

function updateHintButtonLabel() {
    const hintBtn = document.getElementById('hint-btn');
    if (hintBtn) {
        hintBtn.innerText = `💡 Get Hint (${MAX_HINTS - hintsUsed}/${MAX_HINTS} left)`;
        if (hintsUsed >= MAX_HINTS) {
            hintBtn.classList.add('button-exhausted');
        } else {
            hintBtn.classList.remove('button-exhausted');
        }
    }
}

function highlightMatchingNumbers(targetValue) {
    clearAllHighlights();

    const valueToMatch = targetValue ? targetValue.trim() : "";
    if (valueToMatch === "") return;

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
    if (isGameOver) return;

    const cells = document.querySelectorAll('.sudoku-cell');
    let submissionString = "";
    cells.forEach(cell => submissionString += (cell.value.trim() === "") ? "0" : cell.value.trim());

    if (submissionString.includes("0")) {
        showToast("The board is incomplete!", "warn");
        return;
    }

    if (currentSolution === submissionString) {
        clearInterval(timerInterval);
        showToast(`Perfect! You solved the puzzle in ${document.getElementById('sudoku-timer').innerText}!`, "success");
        if (window.sendTimeAnalytics) {
            window.sendTimeAnalytics(activePuzzleId, "Sudoku", "BOARD_PUZZLE", secondsElapsed);
        }
        reportSolveForAchievements();
    } else {
        showToast("There are some mistakes on your board. Keep tracking!", "error");
    }
}

async function reportSolveForAchievements() {
    try {
        const token = localStorage.getItem('token');
        const headers = { "Content-Type": "application/json" };
        if (token) headers["Authorization"] = `Bearer ${token}`;

        const res = await fetch(`/api/sudoku/${activePuzzleId}/solve`, {
            method: "POST",
            headers: headers,
            body: JSON.stringify({ secondsTaken: secondsElapsed })
        });
        const result = await res.json();
        if (window.showAchievementToasts && result.newAchievements && result.newAchievements.length) {
            window.showAchievementToasts(result.newAchievements);
        }
    } catch (err) {
        console.error("Failed to report solve for achievements : ", err);
    }
}

function giveUpAndReveal() {
    if (!currentSolution) return;

    const confirmSurrender = confirm("Are you sure you want to give up and reveal the full solution?");
    if (!confirmSurrender) return;

    isGameOver = true;
    clearInterval(timerInterval);
    const cells = document.querySelectorAll('.sudoku-cell');

    cells.forEach((cell, index) => {
        cell.value = currentSolution[index];
        cell.readOnly = true;
        cell.classList.remove('check-error');
        cell.classList.add('check-correct');
        if (!cell.classList.contains('is-clue')) {
            cell.style.color = '#e194d5';
        }
    });

    showToast("Game Over. Solution grid values filled out completely.", "info");
}

function generateChallengeLink() {
    if (!activePuzzleId) return;
    const challengeUrl = `${window.location.origin}/sudoku?challengeId=${activePuzzleId}`;
    navigator.clipboard.writeText(challengeUrl).then(() => {
        showToast("Challenge Link copied to clipboard!", "success");
    });
}