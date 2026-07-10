const API_BASE=""

let MyUserId = null;
let MyUsername = null;
let roomId = null;
let pollTimer = null;

document.addEventListener("DOMContentLoaded", loadMyIdentity);

async function loadMyIdentity(){
    const token = localStorage.getItem('token');
    if(!token){
        window.location.href = "/login";
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/users/me`,
            {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });
        if(!res.ok){
            localStorage.removeItem('token');
            window.location.href = "/login";
            return;
        }

        const me = await res.json();
        MyUserId = me.id;
        MyUsername = me.username;
        document.getElementById("myUsernameLabel").textContent = MyUsername;
    }catch (err){
        window.location.href = "/login";
    }
}

async function resolveUsername(username){
    const res = await fetch(`${API_BASE}/chat/find-user/${encodeURIComponent(username)}`);
    if(!res.ok){
        const text = await res.text();
        alert(text || `This user does not exist: ${username}`);
        return null;
    }
    const data = await res.json();
    return data.id;
}

async function openFriendChat(){
    if(!MyUserId){
        alert("reload page");
        return;
    }

    const otherUsername = document.getElementById("othersUsername").value.trim();
    if(!otherUsername){
        alert("put other username");
        return;
    }

    const otherId = await resolveUsername(otherUsername);
    if(!otherId){
        return;
    }
    await openRoomRequest(`/chat/open-private/${MyUserId}/${otherId}`, {method: "POST"});
}

async function createGroup(){
    if(!MyUserId){
        alert("Reload page");
        return;
    }
    const groupName = document.getElementById("groupName").value.trim() || "group";
    const raw = document.getElementById("groupUserNames").value.trim();
    if(!raw){
        alert("write list one username");
        return;
    }
    const parts = raw.split(",");
    const usernames = [];
    for(let i=0; i< parts.length; i++){
        const trimmed = parts[i].trim();
        if(trimmed.length>0){
            usernames.push(trimmed);
        }
    }
    const memberIds = [MyUserId];

    for(const username of usernames){
        const id = await resolveUsername(username);
        if(!id){
            return;
        }
        memberIds.push(id);
    }

    const url = `/chat/create-group?name=${encodeURIComponent(groupName)}&type=GROUP`;
    await openRoomRequest(url,{
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(memberIds)
    });
}

async function openRoomRequest(url, options){
    try{
        const res = await fetch(`${API_BASE}${url}`, options);
        if(!res.ok){
            const text = await res.text();
            alert(text || "Unable to open chat");
            return;
        }

        const room = await res.json();
        roomId = room.id;

        document.getElementById("roomIdLabel").textContent = roomId;
        document.getElementById("chatBox").classList.add("visible");


        loadMessages();
        if(pollTimer) clearInterval(pollTimer);
        pollTimer = setInterval(loadMessages, 3000);
    }catch (err){
        alert("Unable to connect to the server");
    }
}


async function loadMessages(){
    const res = await fetch(`${API_BASE}/chat/${roomId}`);
    if(!res.ok) return;

    const messages = await res.json();
    const box = document.getElementById("messages");
    box.innerHTML = "";

    for(let i = 0; i< messages.length; i++){
        const m = messages[i];
        const isMine = String(m.senderId)  === String(MyUserId);

        const div = document.createElement("div");
        let cssClass;
        if(isMine){
            cssClass = "mine";
        }else {
            cssClass = "theirs";
        }
        div.className = "msg " + cssClass;

        if(!isMine){
            const nameElem = document.createElement("div");
            nameElem.className = "senderName";
            nameElem.textContent = m.senderUsername;
            div.appendChild(nameElem);
        }

        div.appendChild(document.createTextNode(m.message));
        box.appendChild(div);
    }
    box.scrollTop = box.scrollHeight;
}

async function sendMessage(){
    const input = document.getElementById("messageInput");
    const text = input.value.trim();
    if(!text) return;

    await fetch(`${API_BASE}/chat/send`, {
        method: "POST",
        headers: {"Content-Type":"application/json"},
        body: JSON.stringify({
            senderId: MyUserId,
            roomId: roomId,
            message: text
        })
    });
    input.value = "";
    loadMessages();
}
