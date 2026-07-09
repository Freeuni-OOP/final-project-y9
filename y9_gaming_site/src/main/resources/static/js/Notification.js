let currentUserId = null;

async function initNotifications(){
    const token = localStorage.getItem("token");
    if(!token){
        return;
    }

    try {
        const res = await fetch("/api/users/me", {
            headers: {"Authorization": "Bearer " + token}
        });
        if (!res.ok) {
            return;
        }

        const me = await res.json();
        currentUserId = me.id;

        await checkUnreadCount();
        setInterval(checkUnreadCount, 30000);
    }catch (e){
        console.log("Notification init error:", e);
    }
}

async function checkUnreadCount(){
    if(!currentUserId){
        return;
    }

    try {
        const res = await fetch("/notifications/unread-count/" + currentUserId);
        if (!res.ok) {
            return;
        }

        const count = await res.json();
        const badge = document.getElementById("bell-badge");

        if (count > 0) {
            badge.textContent = count;
            badge.style.display = "block";
        } else {
            badge.style.display = "none";
        }
    }catch (e){
        console.log("Error checking unread count:", e);
    }
}

async function toggleNotifications(){
    const dropdown = document.getElementById("notification-dropdown");
    if(!dropdown){
        return;
    }

    if(dropdown.style.display === "block"){
        dropdown.style.display = "none";
        return;
    }

    dropdown.style.display = "block";
    await loadNotifications();

    if(currentUserId) {
        await fetch("/notifications/mark-read/" + currentUserId, {
            method: "POST"
        });
        document.getElementById("bell-badge").style.display = "none";
    }
}

async function loadNotifications(){
    const dropdown = document.getElementById("notification-dropdown");
    if(!dropdown || !currentUserId){
        return;
    }

    dropdown.innerHTML = `<div class = 'notification-dropdown-title'>🔔 notifications</div>`;

    try {
        const res = await fetch("/notifications/" + currentUserId);
        if (!res.ok) {
            return;
        }

        const list = await res.json();

        if (list.length === 0) {
            dropdown.innerHTML += `<div class = 'notification-empty'>no notification</div>`;
            return;
        }

        for (let i = 0; i < list.length; i++) {
            const n = list[i];

            const item = document.createElement("div");
            item.className = "notification-item";

            const msg = document.createElement("p");
            msg.textContent = n.message;
            item.appendChild(msg);

            if (n.type === "FRIEND_REQUEST") {
                const acceptBtn = document.createElement("button");
                acceptBtn.className = "notif-accept-btn";
                acceptBtn.textContent = "Accept";
                acceptBtn.onclick = function () {
                    acceptFriend(n.id, item);
                };

                const declineBtn = document.createElement("button");
                declineBtn.className = "notif-decline-btn";
                declineBtn.textContent = "Decline";
                declineBtn.onclick = function () {
                    declineFriend(n.id, item);
                };

                const actions = document.createElement("div");
                actions.className = "notif-actions";
                actions.appendChild(acceptBtn);
                actions.appendChild(declineBtn);
                item.appendChild(actions);
            }

            dropdown.appendChild(item);
        }
    }catch (e){
        console.log("Error loading notifications:", e);
        dropdown.innerHTML += `<div class="notification-empty">Error loading</div>`;
    }
}

async function acceptFriend(notificationId, item){
    try {
        const res = await fetch("/notifications/accept/" + notificationId, {
            method: "POST"
        });
        if (res.ok) {
            item.innerHTML = `<p style = 'color:#9a4eab;'>✅ friendship accepted</p>`;
        }
    }catch (e){
        console.log("Error accepting friend request:", e);
    }
}

async function declineFriend(notificationId, item){
    try {
        const res = await fetch("/notifications/decline/" + notificationId, {
            method: "POST"
        });
        if (res.ok) {
            item.remove();
            checkUnreadCount();
        }
    }catch (e){
        console.log("Error declining friend request:", e);
    }
}

document.addEventListener("click", function (e){
    const bell = document.querySelector(".navbar__bell");
    const dropdown = document.getElementById("notification-dropdown");

    if(!bell || !dropdown){
        return;
    }

    if(!bell.contains(e.target) && !dropdown.contains(e.target)) {
        dropdown.style.display = "none";
    }
});

document.addEventListener("DOMContentLoaded", initNotifications);