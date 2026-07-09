let slideCount = 0;


document.addEventListener('DOMContentLoaded', function () {
    const userRole = localStorage.getItem('role');
    const categoryContainer = document.getElementById('categoryContainer');
    const categorySelect = document.getElementById('categorySelect');

    // Handle Category display and values cleanly in one place
    if (categorySelect) {
        if (userRole === 'USER') {
            // 1. Force the single valid category option
            categorySelect.value = 'ENTERTAINMENT';

            // 2. Hide the entire container visually so users can't see it
            if (categoryContainer) {
                categoryContainer.style.setProperty('display', 'none', 'important');
            }
        }
        else if (userRole === 'ADMIN') {
            // Ensure admins see it completely normally
            if (categoryContainer) {
                categoryContainer.style.setProperty('display', 'block', 'important');
            }
        }
    }
});


function addQuestionBlock() {
    slideCount++;
    const id = 'slide-' + slideCount;

    const qClone = document.getElementById('questionTemplate').content.cloneNode(true);
    const block = qClone.querySelector('.question-block');
    block.dataset.slide = id;
    document.getElementById('questionsContainer').appendChild(qClone);

    const railClone = document.getElementById('railThumbTemplate').content.cloneNode(true);
    const thumb = railClone.querySelector('.rail-thumb');
    thumb.dataset.slide = id;
    document.getElementById('slideRail').insertBefore(railClone, document.getElementById('addSlideBtn'));

    renumber();
    activateSlideById(id);
}

function removeQuestion(btn) {
    const block = btn.closest('.question-block');
    const id = block.dataset.slide;
    block.remove();
    document.querySelector(`.rail-thumb[data-slide="${id}"]`)?.remove();
    renumber();
    const remaining = document.querySelector('.question-block');
    if (remaining) activateSlideById(remaining.dataset.slide);
}

function renumber() {
    document.querySelectorAll('#questionsContainer .question-block').forEach((b, i) => {
        b.querySelector('.q-number').textContent = 'QUESTION ' + (i + 1);
    });
    document.querySelectorAll('#slideRail .rail-thumb').forEach((t, i) => {
        t.querySelector('.rail-number').textContent = i + 1;
    });
}

function activateSlide(thumbEl) {
    activateSlideById(thumbEl.dataset.slide);
}

function activateSlideById(id) {
    document.querySelectorAll('.question-block').forEach(b => b.classList.toggle('active', b.dataset.slide === id));
    document.querySelectorAll('.rail-thumb').forEach(t => t.classList.toggle('active', t.dataset.slide === id));
}

function selectCorrect(tile) {
    const grid = tile.closest('.answer-grid');
    grid.querySelectorAll('.answer-tile').forEach(t => t.classList.remove('selected'));
    tile.classList.add('selected');
    syncRailDots(tile.closest('.question-block'));
}

function syncRailDots(block) {
    const id = block.dataset.slide;
    const thumb = document.querySelector(`.rail-thumb[data-slide="${id}"] .rail-dots`);
    if (!thumb) return;
    thumb.innerHTML = '';
    block.querySelectorAll('.answer-tile').forEach(tile => {
        const dot = document.createElement('span');
        const filled = tile.querySelector('.q-option').value.trim() !== '';
        dot.style.background = filled ? tile.dataset.color : 'rgba(255,255,255,0.1)';
        if (tile.classList.contains('selected')) dot.style.outline = '1px solid white';
        thumb.appendChild(dot);
    });
}

function previewImage(input) {
    const block = input.closest('.question-block');
    const preview = block.querySelector('.q-image-preview');
    const label = block.querySelector('.img-label');
    if (input.files && input.files[0]) {
        preview.src = URL.createObjectURL(input.files[0]);
        preview.style.display = 'block';
        label.textContent = input.files[0].name;
    }
}

function toggleQuizForm(e) {
    if (e) e.preventDefault();
    const container = document.getElementById('quizFormContainer');
    container.style.display = (container.style.display === 'none' || container.style.display === '') ? 'block' : 'none';
    if (container.style.display === 'block') {
        container.scrollIntoView({ behavior: 'smooth' });
        if (!document.querySelector('.question-block')) addQuestionBlock();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('questionsContainer').addEventListener('input', e => {
        if (e.target.classList.contains('q-option')) {
            syncRailDots(e.target.closest('.question-block'));
        }
    });
    document.getElementById('questionsContainer').addEventListener('change', e => {
        if (e.target.classList.contains('q-type-select')) {
            toggleQuestionType(e.target);
        }
    });
    document.getElementById('quizForm').addEventListener('submit', async function (e) {
        e.preventDefault();

        // 1. Remove any old hidden fields from previous failed submit attempts to prevent accumulation
        this.querySelectorAll('input[type="hidden"][name="correctAnswer"], input[type="hidden"][name="wrongAnswers"]').forEach(el => el.remove());

        // 2. Loop through every question block on the UI
        document.querySelectorAll('#questionsContainer .question-block').forEach((block) => {
            const typeSelect = block.querySelector('.q-type-select');
            const isWritten = typeSelect && typeSelect.value === 'WRITTEN';

            const questionTextInput = block.querySelector('.q-text');

            // Prepare hidden inputs
            const correctInput = document.createElement('input');
            correctInput.type = 'hidden';
            correctInput.name = 'correctAnswer'; // Must match your controller's expected request param name

            const wrongInput = document.createElement('input');
            wrongInput.type = 'hidden';
            wrongInput.name = 'wrongAnswers'; // Must match your controller's expected request param name

            if (isWritten) {
                const writtenVal = block.querySelector('.q-written-answer').value.trim();

                // Leave the question text exactly as inputted by the user
                // Set the inputs so Java constructs "Question Text (CoreAnswer|)"
                correctInput.value = writtenVal;
                wrongInput.value = ""; // Submits as empty string so Java loops cleanly without crashing
            } else {
                // Processing standard MCQ
                const tiles = [...block.querySelectorAll('.answer-tile')];
                const correctTile = tiles.find(t => t.classList.contains('selected')) || tiles[0];
                const correctVal = correctTile.querySelector('.q-option').value.trim();
                const wrongVals = tiles.filter(t => t !== correctTile)
                    .map(t => t.querySelector('.q-option').value.trim())
                    .join('|');

                correctInput.value = correctVal;
                wrongInput.value = wrongVals;
            }

            // Always append the hidden elements inside the active block container
            block.appendChild(correctInput);
            block.appendChild(wrongInput);
        });

        // 3. Remove names from the raw option text inputs so they aren't parsed out of alignment
        document.querySelectorAll('#questionsContainer .q-option, #questionsContainer .q-written-answer').forEach(o => {
            o.removeAttribute('name');
        });

        const formData = new FormData(this);
        const token = localStorage.getItem('token');

        try {
            const response = await fetch(this.action, {
                method: 'POST',
                headers: {'Authorization': 'Bearer ' + token},
                body: formData
            });

            if (response.ok) {
                window.location.href = '/home';
            } else {
                alert('Failed to publish quiz (status ' + response.status + ')');
            }
        } catch (err) {
            alert('Error publishing quiz: ' + err.message);
        }
    });
        document.addEventListener('DOMContentLoaded', function () {
        const userRole = localStorage.getItem('role');
        const categorySelect = document.getElementById('categorySelect');

        if (categorySelect) {
        // If the user is NOT an admin, lock the dropdown to Entertainment
        if (userRole !== 'ADMIN') {
        categorySelect.value = 'ENTERTAINMENT'; // Enforce default
        categorySelect.disabled = true;        // Prevent changing

        // Optional styling to visually show it is locked
        categorySelect.style.opacity = '0.6';
        categorySelect.style.cursor = 'not-allowed';
    }
    }
    });

    function toggleQuestionType(selectEl) {
        const block = selectEl.closest('.question-block');
        const mcqSection = block.querySelector('.mcq-section');
        const writtenSection = block.querySelector('.written-section');
        const isWritten = selectEl.value === 'WRITTEN';

        if (isWritten) {
            mcqSection.style.display = 'none';
            writtenSection.style.display = 'block';

            // Disable required validation on the hidden MCQ options
            block.querySelectorAll('.q-option').forEach(opt => opt.required = false);
            // Make the single written input required
            block.querySelector('.q-written-answer').required = true;
        } else {
            mcqSection.style.display = 'grid';
            writtenSection.style.display = 'none';

            // Re-enable required validation on MCQ options
            block.querySelectorAll('.q-option').forEach(opt => opt.required = true);
            // Turn off required validation on the hidden written field
            block.querySelector('.q-written-answer').required = false;
        }

        // Update side thumbnail dot system if applicable
        if (typeof syncRailDots === 'function') {
            syncRailDots(block);
        }
    }
});