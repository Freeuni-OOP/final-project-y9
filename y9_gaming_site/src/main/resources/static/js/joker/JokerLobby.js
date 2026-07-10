const API = "/api/joker";

// --- State Variables ---
let selectedPlayerCount = null;
let selectedRoundOption = null;
let selectedJokerAmount = null;
let selectedAllowRandoms = null;

// --- Wizard State ---
let currentStep = 1;
const totalSteps = 4;

// --- Sub-View Switcher Logic ---
function switchView(viewId, clickedButton = null) {
    setMsg("", "");

    if (viewId === 'view-main') {
        document.querySelectorAll('.joker-menu-btn').forEach(b => b.classList.remove('active'));
    }

    if (clickedButton) {
        document.querySelectorAll('.joker-menu-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        clickedButton.classList.add('active');
    }

    document.querySelectorAll('.view-panel').forEach(panel => {
        panel.classList.remove('active');
    });

    const activePanel = document.getElementById(viewId);
    if (activePanel) {
        activePanel.classList.add('active');
    }

    const subtitle = document.getElementById('panel-subtitle');
    if (viewId === 'view-create') {
        subtitle.textContent = "ახალი თამაშის პარამეტრები";
        resetWizard();
    }
    else if (viewId === 'view-browse') {
        subtitle.textContent = "აქტიური საჯარო ოთახები";
        loadLobbies();
    }
    else if (viewId === 'view-join') {
        subtitle.textContent = "შეუერთდით მეგობრის პარტიას";
    }
    else {
        subtitle.textContent = "აირჩიეთ მოქმედება გასაგრძელებლად";
    }
}

// --- Wizard ნავიგაციის ფუნქციები ---
function updateWizardUI() {
    document.querySelectorAll(".wizard-step").forEach(step => {
        step.classList.remove("active");
        if (parseInt(step.dataset.step) === currentStep) {
            step.classList.add("active");
        }
    });

    const progressFill = document.getElementById("wizardProgress");
    if (progressFill) {
        const percent = (currentStep / totalSteps) * 100;
        progressFill.style.width = `${percent}%`;
    }

    const backBtn = document.getElementById("wizardBackBtn");
    const nextBtn = document.getElementById("wizardNextBtn");

    if (currentStep === 1) {
        backBtn.textContent = "← მთავარი";
    } else {
        backBtn.textContent = "← უკან";
    }

    if (currentStep === totalSteps) {
        nextBtn.textContent = "ოთახის შექმნა 🃏";
        nextBtn.className = "joker-btn joker-btn--primary";
    } else {
        nextBtn.textContent = "შემდეგი →";
        nextBtn.className = "joker-btn joker-btn--primary";
    }
}

function handleWizardNext() {
    if (currentStep === 1 && !selectedPlayerCount) { setMsg(" გთხოვთ აირჩიოთ მოთამაშეების რაოდენობა", "error"); return; }
    if (currentStep === 2 && !selectedRoundOption) { setMsg(" გთხოვთ აირჩიოთ რაუნდების რაოდენობა", "error"); return; }
    if (currentStep === 3 && !selectedJokerAmount) { setMsg(" გთხოვთ აირჩიოთ ჯოკერების რაოდენობა", "error"); return; }
    if (currentStep === 4 && selectedAllowRandoms === null) { setMsg(" გთხოვთ აირჩიოთ ოთახის ხილვადობა", "error"); return; }

    setMsg("", "");

    if (currentStep < totalSteps) {
        currentStep++;
        updateWizardUI();
    } else {
        createGame();
    }
}

function handleWizardBack() {
    if (currentStep > 1) {
        currentStep--;
        updateWizardUI();
    } else {
        switchView('view-main');
        document.querySelectorAll('.joker-menu-btn').forEach(btn => {
            btn.classList.remove('active');
        });
    }
}

function resetWizard() {
    currentStep = 1;
    selectedPlayerCount = null;
    selectedRoundOption = null;
    selectedJokerAmount = null;
    selectedAllowRandoms = null;
    document.querySelectorAll(".joker-toggle").forEach(b => b.classList.remove("active"));
    updateWizardUI();
}

// --- Toggle configurations ---
document.querySelectorAll(".wizard-step .joker-toggle-group").forEach(group => {
    const stepNum = parseInt(group.closest(".wizard-step").dataset.step);

    group.querySelectorAll(".joker-toggle").forEach(btn => {
        btn.addEventListener("click", () => {
            group.querySelectorAll(".joker-toggle").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            const val = btn.dataset.value;

            if (stepNum === 1) selectedPlayerCount  = val;
            if (stepNum === 2) selectedRoundOption  = val;
            if (stepNum === 3) selectedJokerAmount  = parseInt(val);
            if (stepNum === 4) selectedAllowRandoms = val === "true";
        });
    });
});

// --- Action Logic: Create Game Room ---
async function createGame() {
    const token = localStorage.getItem("token");
    setMsg("", "");
    try {
        const res = await fetch(`${API}/create`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                playerCount:  selectedPlayerCount,
                roundOption:  selectedRoundOption,
                jokerAmount:  selectedJokerAmount,
                allowRandoms: selectedAllowRandoms
            })
        });
        if (!res.ok) {
            const err = await res.text();
            setMsg(err || "შეცდომა", "error");
            return;
        }
        const state = await res.json();
        const roomCode = state.room.roomId;
        window.location.href = `/joker/${roomCode}`;
    } catch (err) {
        console.error(err);
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- Action Logic: Join Room manually via Explicit Code ---
async function joinByCode() {
    const token = localStorage.getItem("token");
    const code = document.getElementById("roomCodeInput").value.trim().toUpperCase();
    if (!code) { setMsg("შეიყვანე ოთახის კოდი", "error"); return; }

    try {
        const res = await fetch(`${API}/${code}/join`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            const err = await res.text();
            setMsg(err || "შეცდომა", "error");
            return;
        }
        window.location.href = `/joker/${code}`;
    } catch (err) {
        console.error(err);
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- Action Logic: Load Open Public Lobbies ---
async function loadLobbies() {
    const token = localStorage.getItem("token");
    const list = document.getElementById("lobbiesList");
    list.innerHTML = `<p style="color:#888; text-align:center; font-size:0.85rem;">იტვირთება...</p>`;

    try {
        const res = await fetch(`${API}/lobbies`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("failed");
        const lobbies = await res.json();

        if (!lobbies || lobbies.length === 0) {
            list.innerHTML = `<p style="color:#888; text-align:center; font-size:0.85rem;">ღია ლობი არ არის</p>`;
            return;
        }

        list.innerHTML = "";
        lobbies.forEach(lobby => {
            const players = lobby.players || [];
            const config  = lobby.config || {};
            const roomCode = lobby.room?.roomId || "—";
            const filled   = players.length;
            const required = config.players || "?";
            const rounds   = config.totalRounds || "?";

            const item = document.createElement("div");
            item.className = "joker-lobby-item";
            item.setAttribute("data-code", roomCode.toUpperCase());
            item.innerHTML = `
                <div class="joker-lobby-info">
                    <div class="joker-lobby-code">🃏 ${roomCode}</div>
                    <div class="joker-lobby-meta">
                        ${filled}/${required} მოთამაშე &nbsp;·&nbsp; ${rounds} რაუნდი
                    </div>
                </div>
                <button class="joker-lobby-join" onclick="joinLobby('${roomCode}')">
                    შესვლა
                </button>
            `;
            list.appendChild(item);
        });
    } catch (err) {
        list.innerHTML = `<p style="color:var(--pink); text-align:center; font-size:0.85rem;">ვერ ჩაიტვირთა</p>`;
    }
}

async function joinLobby(roomCode) {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API}/${roomCode}/join`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            const err = await res.text();
            setMsg(err || "შეცდომა", "error");
            if (res.status === 404) {
                loadLobbies();
            }
            return;
        }
        window.location.href = `/joker/${roomCode}`;
    } catch (err) {
        setMsg("კავშირის შეცდომა", "error");
    }
}

// --- UI Notification Helpers ---
function setMsg(text, type) {
    const el = document.getElementById("lobbyMsg");
    if(el) {
        el.textContent = text;
        el.className = "joker-msg" + (type ? " " + type : "");
    }
}

// --- Rules Component Object ---
const JokerRules = {
    // ქმნის უნიკალურ გასაღებს კონკრეტული იუზერისთვის
    getStorageKey() {
        const token = localStorage.getItem("token");
        const isGuest = localStorage.getItem("isGuest") === "true";
        if (isGuest) return "jokerRulesAccepted_guest";
        if (token) {
            // უსაფრთხოებისთვის ვიღებთ ტოკენის ბოლო 10 სიმბოლოს, როგორც იუზერის ID-ს
            return "jokerRulesAccepted_" + token.slice(-10);
        }
        return "jokerRulesAccepted_unknown";
    },

    getModalHTML(isMandatory = true) {
        const checkboxSection = isMandatory ? `
            <div class="rules-checkbox-container">
                <input type="checkbox" id="acceptRulesCheck">
                <label for="acceptRulesCheck">წავიკითხე წესები და ვეთანხმები</label>
            </div>
            <button id="closeRulesBtn" class="joker-btn joker-btn--primary" disabled>გაგრძელება</button>
        ` : `
            <button id="closeRulesBtn" class="joker-btn joker-btn--primary" style="width: auto; padding: 10px 30px;">დახურვა</button>
        `;

        return `
        <div id="jokerRulesModal" class="rules-modal-overlay">
            <div class="rules-modal-content" style="max-width: 750px;"> 
                <div class="rules-modal-header">
                    <h2> თამაშის წესები </h2>
                </div>
                <div class="rules-modal-body">
                    <h3>1. ოთახის შექმნა და ნავიგაცია</h3>
                    <p>თამაშის დაწყება შესაძლებელია სამი ძირითადი გზით:</p>
                    <ul>
                        <li><strong>ოთახის შექმნა:</strong> თქვენ ირჩევთ მოთამაშეების რაოდენობას (3 ან 4), რაუნდების, ჯოკერების რაოდენობას და წვდომას. საჯარო ოთახი ჩნდება ლობიში, ხოლო კერძო ოთახში შესვლა მხოლოდ უნიკალური კოდითაა შესაძლებელი.</li>
                        <li><strong>ღია ოთახები:</strong> ლობიში დინამიურად იტვირთება აქტიური საჯარო თამაშები, სადაც ნაჩვენებია მოთამაშეების მიმდინარე რაოდენობა და რაუნდების ტიპი.</li>
                        <li><strong>კოდით შესვლა:</strong> კერძო ოთახში მოსახვედრად იყენებთ <code>JOKER-XXXXXXXX</code> ფორმატის უნიკალურ იდენტიფიკატორს.</li>
                    </ul>

                    <h3>2. ბანქოს დასტა და ნორმალიზაცია</h3>
                    <p>დასტის სტრუქტურა ავტომატურად ადაპტირდება მოთამაშეებისა და ჯოკრების რაოდენობაზე:</p>
                    <ul>
                        <li><strong>4 მოთამაშე:</strong> თამაში იწყება 6-იანებიდან (სულ 36 კარტი). 
                            <br>• <em>2 ჯოკერის შემთხვევაში:</em> დასტას აკლდება <strong>ჯვრის 6</strong> და <strong>ყვავის 6</strong>, მათ ნაცვლად ემატება 2 ჯოკერი.
                            <br>• <em>1 ჯოკერის შემთხვევაში:</em> აკლდება მხოლოდ <strong>ყვავის 6</strong> და ემატება 1 ჯოკერი.
                        </li>
                        <li><strong>3 მოთამაშე:</strong> თამაში იწყება 8-იანებიდან (სულ 28 კარტი). 
                            <br>• <em>2 ჯოკერის შემთხვევაში:</em> დასტას აკლდება <strong>ჯვრის 8</strong> და <strong>ყვავის 8</strong>, მათ ნაცვლად ემატება 2 ჯოკერი.
                            <br>• <em>1 ჯოკერის შემთხვევაში:</em> აკლდება მხოლოდ <strong>ყვავის 8</strong> და ემატება 1 ჯოკერი.
                        </li>
                    </ul>

                    <h3>3. თამაშის რეჟიმები და რაუნდები</h3>
                    <ul>
                        <li><strong>QUICK_4:</strong> სულ 4 რაუნდი. ყოველ რაუნდში რიგდება მაქსიმალური — <strong>9 კარტი</strong>.</li>
                        <li><strong>SHORT_8:</strong> სულ 8 რაუნდი. კარტების რაოდენობა იზრდება პროგრესიულად 1-დან 8-მდე.</li>
                        <li><strong>FULL_24:</strong> კლასიკური 24 რაუნდი, რომელიც მოიცავს მზარდ, ფიქსირებულ (9 კარტი) და კლებად ფაზებს.</li>
                    </ul>

                    <h3>4. კოზირი და სვლების რიგითობა (Bidding)</h3>
                    <p>კარტების დარიგების შემდეგ ვლინდება კოზირი (Trump). თუ დასტაში მორჩენილია კარტი, პირველივე კარტი ტრიალდება, წინააღმდეგ შემთხვევაში კოზირს განსაზღვრავს დამრიგებლის (Dealer) ბოლო კარტი. თუ ეს კარტი ჯოკერია, რაუნდი მიმდინარეობს უკოზიროდ.</p>
                    <ul>
                        <li><strong>პრედიქციის (Bidding) ფაზა:</strong> პირველ პროგნოზს (პრედიქციას) აკეთებს <strong>დამრიგებლის შემდეგ შემოსული მოთამაშე</strong>, რის შემდეგაც რიგრიგობით ყველა აცხადებს სასურველ პრედიქციას.</li>
                        <li><strong>გათამაშების ფაზა:</strong> რაუნდის პირველ სვლას (პირველი კარტის ჩამოსვლას) ასევე ასრულებს <strong>დამრიგებლის შემდეგ შემოსული მოთამაშე</strong>. ყოველ მომდევნო ხელს კი იწყებს წინა ხელის მომგები.</li>
                    </ul>

                    <h3>5. კარტის დადების და ჯოკერის მართვის წესები</h3>
                    <ul>
                        <li><strong>ცვეტი:</strong> მოთამაშე ვალდებულია ჩამოვიდეს იმავე ცვეტის კარტს, რომლითაც რაუნდის დამწყები ჩამოვიდა. თუ ცვეტი არ აქვს, ვალდებულია გაჭრას კოზირით (თუ ჰყავს). თუ არც ცვეტი აქვს და არც კოზირი, შეუძლია ითამაშოს  ნებისმიერი კარტით.</li>
                        <li><strong>ჯოკერის თამაში ნებისმიერ დროს:</strong> ჯოკერის გამოყენება შესაძლებელია ნებისმიერ სვლაზე, იმის მიუხედავად, გაქვთ თუ არა ხელში მოთხოვნილი ცვეტი ან კოზირი. ჯოკერის ჩამოსვლისას მოთამაშე აცხადებს მის სტატუსს:</li>
                        
                        <li style="list-style-type: none; padding-left: 15px;">
                            <strong>• ჯოკერი როგორც მაღალი ("წაიღოს"):</strong> 
                            <br>– თუ მოთამაშე ჯოკერით იწყებს ხელს, იგი <strong>ასახელებს ცვეტს</strong>, რომელიც სხვებმა უნდა დადონ. ამ დროს, ყველა სხვა მოთამაშე ვალდებულია ჩამოვიდეს დასახელებული ცვეტის <strong>თავის ყველაზე მაღალ კარტს</strong> (ან მეორე ჯოკერს). 
                            <br>– თუ დასახელებული ფერი კოზირია და მოთამაშეს ის არ ჰყავს, შეუძლია დადოს ნებისმიერი კარტი. თუ დასახელებული ცვეტი ჩვეულებრივია (არა-კოზირი), და მოთამაშეს ის არ ჰყავს, იგი <strong>ვალდებულია გაჭრას კოზირით</strong> (ნებისმიერით, არაა სავალდებულო უმაღლესი კოზირი).
                            <br>– <em>გამონაკლისები, როცა მაღალი ჯოკერი აგებს:</em> 
                            <ol>
                                <li>თუ იმავე ხელში მომდევნო მოთამაშეც დადებს მეორე ჯოკერს "მაღლად", ხელს იგებს <strong>მეორე (რიგით ბოლო) ჯოკერი</strong>.</li>
                                <li>თუ ხელის დამწყები ჩამოვიდა ჯოკერს "მაღლად", მოითხოვა არა-კოზირის ფერი, და რომელიმე მოთამაშეს ეს ფერი არ აღმოაჩნდა და <strong>გაჭრა კოზირით</strong>, ხელს იგებს ეს კოზირი და არა ჯოკერი!</li>
                            </ol>
                        </li>
                        
                        <li style="list-style-type: none; padding-left: 15px; margin-top: 8px;">
                            <strong>• ჯოკერი როგორც დაბალი ("დაიკიდოს"):</strong> 
                            <br>– თუ მოთამაშე ჯოკერით იწყებს ხელს, იგი ასახელებს ცვეტს, ხოლო სხვა მოთამაშეები უბრალოდ ვალდებულები არიან მიყვნენ ამ ცვეტს (არაა აუცილებელი უმცირესი ან უდიდესი კარტის დადება). ცვეტის არქონისას – ჭრიან კოზირით. დაბალი ჯოკერი ავტომატურად აგებს ხელს (თუ მეორე მოთამაშემაც არ დადო დაბალი ჯოკერი, რა დროსაც პირველი იგებს).
                        </li>
                    </ul>

                    <h3>6. ქულების მათემატიკური სისტემა</h3>
                    <table>
                        <thead style="text-align: left; color: var(--orchid);">
                            <tr><th style="padding: 8px;">სიტუაცია</th><th style="padding: 8px;">ფორმულა / ქულა</th></tr>
                        </thead>
                        <tbody>
                            <tr><td style="padding: 6px 8px;"><strong>ზუსტი მიზანი (Prophecy == Actual)</strong></td><td style="padding: 6px 8px;"><code>(ბიდი × 50) + 50</code></td></tr>
                            <tr><td style="padding: 6px 8px;"><strong>სუფთა ნული (0 თქვა და 0 წაიღო)</strong></td><td style="padding: 6px 8px;"><code>+50 ქულა</code></td></tr>
                            <tr><td style="padding: 6px 8px;"><strong>გრანდ სლემი (ყველა ხელი წაიღო)</strong></td><td style="padding: 6px 8px;"><code>ბიდი × 100</code></td></tr>
                            <tr><td style="padding: 6px 8px;"><strong>პასივში წაყვანა (0 თქვა, მაგრამ წაიღო)</strong></td><td style="padding: 6px 8px;"><code>+10 × წაყვანილი</code></td></tr>
                            <tr><td style="padding: 6px 8px;"><strong>გაუმართლა (არაზუსტი, მაგრამ > 0)</strong></td><td style="padding: 6px 8px;"><code>წაყვანილი × 10</code></td></tr>
                            <tr><td style="padding: 6px 8px;"><strong>გაფუჭებული ბიდი (ბიდი > 0, წაყვანილი == 0)</strong></td><td style="padding: 6px 8px; color: var(--pink);"><code>-200 ქულა</code></td></tr>
                            <tr><td style="padding: 6px 8px;"><strong>⚠️ ჯარიმა FULL_24-ის 9-12 და 21-24 რაუნდებში</strong></td><td style="padding: 6px 8px; color: #ff4444; font-weight: bold;"><code>-500 ქულა</code></td></tr>
                        </tbody>
                    </table>
                </div>
                <div class="rules-modal-footer">
                    ${checkboxSection}
                </div>
            </div>
        </div>
        `;
    },

    show(isMandatory = true) {
        const existing = document.getElementById('jokerRulesModal');
        if (existing) existing.remove();

        document.body.insertAdjacentHTML('beforeend', this.getModalHTML(isMandatory));

        const modal = document.getElementById('jokerRulesModal');
        const checkbox = document.getElementById('acceptRulesCheck');
        const btn = document.getElementById('closeRulesBtn');

        if (isMandatory) {
            document.body.classList.add('rules-locked');
        }

        if (checkbox && btn) {
            checkbox.addEventListener('change', (e) => {
                btn.disabled = !e.target.checked;
            });
        }

        btn.addEventListener('click', () => {
            if (isMandatory) {
                const key = this.getStorageKey();
                localStorage.setItem(key, 'true');
                document.body.classList.remove('rules-locked');
            }
            modal.remove();
        });
    },

    initInLobby() {
        const key = this.getStorageKey();
        const isAccepted = localStorage.getItem(key);
        // თუ ამ იუზერს/სტუმარს ჯერ არ წაუკითხავს, მხოლოდ მაშინ ამოუგდებს იძულებით
        if (!isAccepted) {
            this.show(true);
        }
    }
};

// --- გვერდის ერთიანი ჩატვირთვის ლოგიკა (DOMContentLoaded) ---
document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    const isGuest = localStorage.getItem("isGuest") === "true";

    // 1. უსაფრთხოების ფილტრი: თუ არც ავტორიზებულია და არც სტუმარი, მიდის ლოგინზე
    if (!token && !isGuest) {
        window.location.href = "/login";
        return;
    }

    // 2. ჩაუშვი წესების შემოწმება (ამოხტება მხოლოდ ახალ იუზერებზე!)
    JokerRules.initInLobby();

    // 3. სათაურზე დაჭერისას ყოველთვის ამოაგდოს წესები
    const mainTitle = document.querySelector(".joker-title") || document.querySelector("h1");
    if (mainTitle) {
        mainTitle.style.cursor = "pointer";
        mainTitle.style.userSelect = "none";
        mainTitle.addEventListener("click", () => {
            JokerRules.show(true); // იძულებითი ჩვენება დათანხმებით
        });
    }
});