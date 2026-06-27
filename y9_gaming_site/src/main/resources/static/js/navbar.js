async function loadNavProfile() {
    const token = localStorage.getItem('token');
    const navUser = document.getElementById('nav-username');
    const navAvatar = document.getElementById('nav-avatar');
    const navProfileLink = document.getElementById('nav-profile-link')

    if(!token){
        return;
    }

    try {
        const res = await fetch('/api/users/me', {method: 'GET',
        headers: {'Authorization': `Bearer ${token}`, 'Content-type': 'application/json'}});

        if(res.ok){
            const user = await res.json();
            if(navUser){
                navUser.textContent = user.username;
            }
            if(navAvatar){
                navAvatar.src = user.avatarUrl || '/img/avatars/default.png';
                navAvatar.onerror = function (){
                    this.onerror = null;
                    this.src = '/img/avatars/default.png';
                };
            }
            if (navProfileLink){
                navProfileLink.href = '/profile/'+user.id;
            }
        }else if (res.status === 401|| res.status === 403){
            localStorage.removeItem('token');
        }
    }catch (err){
        console.error('Nav profile sync failed: ', err);
    }
}

document.addEventListener('DOMContentLoaded', loadNavProfile);