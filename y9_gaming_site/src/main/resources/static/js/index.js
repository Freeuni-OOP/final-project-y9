const API = 'http://localhost:8081/api/users';

// Helper function to save JWT details and cleanly move to the homepage
function saveSession(data) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
    localStorage.setItem('role', data.role);

    // Smooth transition over to our secure controller route
    window.location.href = '/home';
}

// Handle the Login Submission
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

// Handle the Guest Session Option
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