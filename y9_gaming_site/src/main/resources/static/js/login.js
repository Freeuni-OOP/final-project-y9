async function login() {
    const email = document.getElementById('password').value;
    const password = document.getElementById('password').value;

    if (!email || !password) {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').innerText = 'Please fill in all fields';
        return;
    }

    const response = await fetch('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    const result = await response.text();

    if (result === 'ok') {
        window.location.href = '/home';
    } else {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').innerText = 'Invalid email or password';
    }
}