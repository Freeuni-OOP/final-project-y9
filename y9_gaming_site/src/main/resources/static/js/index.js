const API = 'http://localhost:8080/api/users';

const loginView = document.getElementById('loginView');
const registerView = document.getElementById('registerView');

// View Toggle Controls
document.getElementById('toRegisterLink').addEventListener('click', () => {
    loginView.classList.add('hidden');
    registerView.classList.remove('hidden');
});

document.getElementById('toLoginLink').addEventListener('click', () => {
    registerView.classList.add('hidden');
    loginView.classList.remove('hidden');
});

// Shared Session Save Handler
function saveSession(data) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
    localStorage.setItem('role', data.role);
    alert("Success! Authenticated as: " + data.username);
}

// Form Submission Actions
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const body = {
        username: document.getElementById('loginUser').value,
        password: document.getElementById('loginPass').value
    };
    try {
        const res = await fetch(`${API}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (res.ok) {
            saveSession(await res.json());
        } else {
            alert("Invalid username or password.");
        }
    } catch (err) {
        alert("Server connection failed.");
    }
});

document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const body = {
        username: document.getElementById('regUser').value,
        email: document.getElementById('regEmail').value,
        birthDate: document.getElementById('regBirth').value,
        password: document.getElementById('regPass').value
    };
    try {
        const res = await fetch(API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (res.ok) {
            alert("Registered successfully!");
            registerView.classList.add('hidden');
            loginView.classList.remove('hidden');
        } else {
            const errMsg = await res.text();
            alert(errMsg || "Registration failed.");
        }
    } catch (err) {
        alert("Server connection failed.");
    }
});

document.getElementById('guestBtn').addEventListener('click', async () => {
    try {
        const res = await fetch(`${API}/guest`, { method: 'POST' });
        if (res.ok) {
            saveSession(await res.json());
        } else {
            alert("Could not initialize guest session.");
        }
    } catch (err) {
        alert("Server connection failed.");
    }
});