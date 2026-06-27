const emojiMap = { 'GEOGRAPHY': '🌍', 'SCIENCE': '🔬', 'ENTERTAINMENT': '🎮' };

let currentQuiz = null;
let currentQuestionIndex = 0;
let score = 0;
let timerInterval = null;
let timeLeft = 0;
let currentCorrectAnswer = "";

document.addEventListener("DOMContentLoaded", () => {
    fetchAndRenderQuizzes('');

    const role = localStorage.getItem('role');
    if (role === 'ADMIN') {
        const adminLink = document.getElementById('adminLink');
        if (adminLink) adminLink.style.display = 'block';
    }

    const savedUser = localStorage.getItem('username');
    if (savedUser) {
        const navUsername = document.getElementById('nav-username');
        if (navUsername) navUsername.textContent = savedUser;
    }
});

function filterCategory(btn, category) {
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    fetchAndRenderQuizzes(category);
}

async function fetchAndRenderQuizzes(category) {
    const grid = document.getElementById('quizGrid');
    let url = category ? `/api/quizzes/category/${category}` : '/api/quizzes';

    try {
        const token = localStorage.getItem('token');
        const res = await fetch(url, {
            method: 'GET',
            headers: { 'Authorization': token ? `Bearer ${token}` : '', 'Content-Type': 'application/json' }
        });
        if (!res.ok) throw new Error();
        const quizzes = await res.json();
        grid.innerHTML = '';

        if (quizzes.length === 0) {
            grid.innerHTML = '<p style="text-align: center; width: 100%; color: #aaa;">No active quiz packages found.</p>';
            return;
        }

        quizzes.forEach(quiz => {
            const card = document.createElement('div');
            card.className = 'quiz-card';
            const emoji = emojiMap[quiz.category] || '❓';

            card.innerHTML = `
                <div>
                    <div class="quiz-icon">${emoji}</div>
                    <div class="quiz-title">${quiz.title}</div>
                    <div class="quiz-meta">${quiz.category.replace('_', ' ')} • ${quiz.timeLimitSeconds}s</div>
                    <div class="quiz-desc">${quiz.description || 'No description provided.'}</div>
                </div>
                <button class="play-btn" onclick="startQuiz(${quiz.id})">Start Quiz</button>
            `;
            grid.appendChild(card);
        });
    } catch (err) {
        grid.innerHTML = `<p style="color:#ff6b9d; text-align:center; width: 100%;">Error connecting to core database streams.</p>`;
    }
}

async function startQuiz(id) {
    try {
        const categoryActive = document.querySelector('.filter-btn.active').getAttribute('onclick').match(/'([^']*)'/)[1];
        let url = categoryActive ? `/api/quizzes/category/${categoryActive}` : '/api/quizzes';

        const res = await fetch(url);
        const quizzes = await res.json();
        currentQuiz = quizzes.find(q => q.id === id);

        if (!currentQuiz || !currentQuiz.questions || currentQuiz.questions.length === 0) {
            alert("This quiz doesn't have any question nodes loaded.");
            return;
        }

        document.getElementById('filterBar').classList.add('hidden');
        document.getElementById('quizGrid').classList.add('hidden');
        document.getElementById('quizPlayer').classList.remove('hidden');
        document.getElementById('gameActiveArea').classList.remove('hidden');
        document.getElementById('gameScoreArea').classList.add('hidden');

        currentQuestionIndex = 0;
        score = 0;
        timeLeft = currentQuiz.timeLimitSeconds;

        document.getElementById('activeQuizTitle').textContent = currentQuiz.title;
        showQuestion();
        startTimer();

    } catch (e) {
        alert("Error running initialization script context map.");
    }
}

function showQuestion() {
    const rawQuestion = currentQuiz.questions[currentQuestionIndex];
    const openParenIdx = rawQuestion.lastIndexOf('(');
    const closeParenIdx = rawQuestion.lastIndexOf(')');
    if (openParenIdx === -1 || closeParenIdx === -1) return;

    const textPrompt = rawQuestion.substring(0, openParenIdx).trim();
    document.getElementById('questionPrompt').textContent = textPrompt;
    document.getElementById('questionProgress').textContent = `Question ${currentQuestionIndex + 1} of ${currentQuiz.questions.length}`;

    const innerData = rawQuestion.substring(openParenIdx + 1, closeParenIdx);

    if (innerData.includes('|')) {
        document.getElementById('textInputContainer').classList.add('hidden');
        document.getElementById('mcqOptionsContainer').classList.remove('hidden');

        const options = innerData.split('|');
        currentCorrectAnswer = options[0];

        const scrambled = [...options].sort(() => Math.random() - 0.5);
        const mcqContainer = document.getElementById('mcqOptionsContainer');
        mcqContainer.innerHTML = '';

        scrambled.forEach(choice => {
            const btn = document.createElement('button');
            btn.className = 'mcq-btn';
            btn.textContent = choice.trim();
            btn.onclick = () => checkMCQAnswer(choice);
            mcqContainer.appendChild(btn);
        });
    } else {
        document.getElementById('mcqOptionsContainer').classList.add('hidden');
        document.getElementById('textInputContainer').classList.remove('hidden');
        currentCorrectAnswer = innerData;

        const inputField = document.getElementById('answerInput');
        inputField.value = '';
        setTimeout(() => inputField.focus(), 40);
    }
}

function handleKeyDown(event) {
    if (event.key === "Enter") checkAnswer();
}

function checkAnswer() {
    const inputElement = document.getElementById('answerInput');
    const cleanUserAnswer = inputElement.value.trim().toLowerCase().replace(/[\r\n\t]/g, "");
    const cleanCorrectAnswer = currentCorrectAnswer.trim().toLowerCase().replace(/[\r\n\t]/g, "");

    if (cleanUserAnswer === cleanCorrectAnswer) {
        score++;
        advanceQuiz();
    } else {
        inputElement.style.borderColor = "#ff007f";
        setTimeout(() => inputElement.style.borderColor = "rgba(255,255,255,0.2)", 350);
        inputElement.value = '';
    }
}

function checkMCQAnswer(selectedText) {
    const cleanUserAnswer = selectedText.trim().toLowerCase().replace(/[\r\n\t]/g, "");
    const cleanCorrectAnswer = currentCorrectAnswer.trim().toLowerCase().replace(/[\r\n\t]/g, "");

    if (cleanUserAnswer === cleanCorrectAnswer) {
        score++;
        advanceQuiz();
    } else {
        const mcqContainer = document.getElementById('mcqOptionsContainer');
        mcqContainer.style.opacity = "0.4";
        setTimeout(() => {
            mcqContainer.style.opacity = "1";
            advanceQuiz();
        }, 300);
    }
}

function skipQuestion() {
    const currentQuestion = currentQuiz.questions[currentQuestionIndex];
    currentQuiz.questions.splice(currentQuestionIndex, 1);
    currentQuiz.questions.push(currentQuestion);
    showQuestion();
}

function advanceQuiz() {
    currentQuestionIndex++;
    if (currentQuestionIndex < currentQuiz.questions.length) {
        showQuestion();
    } else {
        endQuiz();
    }
}

function startTimer() {
    updateTimerDisplay();
    clearInterval(timerInterval);
    timerInterval = setInterval(() => {
        timeLeft--;
        updateTimerDisplay();
        if (timeLeft <= 0) endQuiz();
    }, 1000);
}

function updateTimerDisplay() {
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    document.getElementById('quizTimer').textContent =
        `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
}

function endQuiz() {
    clearInterval(timerInterval);
    document.getElementById('gameActiveArea').classList.add('hidden');
    document.getElementById('gameScoreArea').classList.remove('hidden');
    document.getElementById('finalScoreText').textContent = `You scored ${score} out of ${currentQuiz.questions.length}!`;
}

function quitQuiz() {
    if (confirm("Are you sure you want to surrender this attempt?")) endQuiz();
}

function exitPlayer() {
    clearInterval(timerInterval);
    document.getElementById('filterBar').classList.remove('hidden');
    document.getElementById('quizGrid').classList.remove('hidden');
    document.getElementById('quizPlayer').classList.add('hidden');
    const activeCatBtn = document.querySelector('.filter-btn.active');
    activeCatBtn.click();
}