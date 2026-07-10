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
    }
    if (adminLink && user.role === 'ADMIN') {
        adminLink.style.display = 'block';
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