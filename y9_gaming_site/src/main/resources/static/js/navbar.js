function showInfoToast(message) {
    let style = document.getElementById('info-toast-style');
    if (!style) {
        style = document.createElement('style');
        style.id = 'info-toast-style';
        style.textContent = `
#info-toast-container { position: fixed; top: 20px; right: 20px; z-index: 9999; display: flex; flex-direction: column; gap: 10px; pointer-events: none; }
.info-toast { min-width: 220px; max-width: 320px; padding: 14px 16px; border-radius: 12px; background: rgba(41, 20, 48, 0.92); backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px); border: 1px solid rgba(192, 38, 211, 0.4); box-shadow: 0 0 20px rgba(192, 38, 211, 0.35), 0 8px 24px rgba(0, 0, 0, 0.35); transform: translateX(120%); opacity: 0; transition: transform 0.35s ease, opacity 0.35s ease; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 0.9rem; font-weight: 600; color: #fff; }
.info-toast.show { transform: translateX(0); opacity: 1; }
.info-toast.hide { transform: translateX(120%); opacity: 0; }
@media (max-width: 480px) { #info-toast-container { left: 12px; right: 12px; top: 12px; } .info-toast { max-width: none; } }`;
        document.head.appendChild(style);
    }

    let container = document.getElementById('info-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'info-toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'info-toast';
    toast.textContent = message;
    container.appendChild(toast);

    requestAnimationFrame(() => toast.classList.add('show'));
    setTimeout(() => {
        toast.classList.remove('show');
        toast.classList.add('hide');
        setTimeout(() => toast.remove(), 400);
    }, 3200);
}

function updateNavbar(user){
    const navUser = document.getElementById('nav-username');
    const navAvatar = document.getElementById('nav-avatar');
    const navProfileLink = document.getElementById('nav-profile-link')

    if (navUser) {
        navUser.textContent = user.username;
    }

    if (navAvatar) {
        navAvatar.src = user.avatarUrl || '/img/avatars/default.png'
        navAvatar.onerror = function () {
            this.onerror = null;
            this.src = '/img//avatars/default.png';
        };
    }
    if (navProfileLink) {
        navProfileLink.href = '/profile/' + user.id;
        navProfileLink.onclick = function (e) {
            if (user.role === 'GUEST') {
                e.preventDefault();
                showInfoToast('You have to log in');
            }
        };
    }
    if (adminLink && user.role === 'ADMIN') {
        adminLink.style.display = 'block';
    }

    // NEW: load streak once we know the user's id
    if (user.id && typeof loadStreak === 'function') {
        loadStreak(user.id);
    }
}

async function loadNavProfile() {
    const token = localStorage.getItem('token');
    if(!token){
        return;
    }

    try {
        const res = await fetch('/api/users/me', {method: 'GET',
            headers: {'Authorization': `Bearer ${token}`, 'Content-type': 'application/json'}});

        if(res.ok){
            const user = await res.json();
            updateNavbar(user);
        }else if (res.status === 401|| res.status === 403){
            localStorage.removeItem('token');
        }
    }catch (err){
        console.error('Nav profile sync failed: ', err);
    }
}

async function logout() {
    try {
        await fetch("/api/users/logout", { method: "POST" });
    } catch (e) {
        console.error("Backend logout call skipped.");
    }
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    window.location.href = "/login";
}


document.addEventListener('DOMContentLoaded', function (){
    loadNavProfile();
});