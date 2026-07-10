const API_BASE=""

let MyUserId = null;
let MyUsername = null;
let roomId = null;
let pollTimer = null;

let renderedMessageIds = new Set();

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
    await openFriendChatWithId(otherId);
}

async function openFriendChatWithId(otherId){
    if(!MyUserId){
        alert("reload page");
        return;
    }
    await openRoomRequest(`/chat/open-private/${MyUserId}/${otherId}`, {method: "POST"});
}

let friendSearchTime = null;

function onFriendSearch(value){
    clearTimeout(friendSearchTime);

    const result = document.getElementById("friendSearchResults");

    if(value.trim().length < 2){
        result.style.display = "none";
        result.innerHTML = "";
        return;
    }

    friendSearchTime = setTimeout(function (){
        runFriendSearch(value.trim());
    }, 300);
}

async function runFriendSearch(query){
    const result = document.getElementById("friendSearchResults");
    result.innerHTML = "";
    result.style.display = "block";

    if(!MyUserId){
        result.innerHTML = "<div class='chat-search-empty'>reload page</div>";
        return;
    }

    const token = localStorage.getItem("token");
    let matches = [];
    try {
        const url = `${API_BASE}/friends/search?myId=${MyUserId}&query=` + encodeURIComponent(query);
        const res = await fetch(url, {
            headers: {"Authorization": "Bearer " + token}
        });
        if(res.ok){
            matches = await res.json();
        }
    }catch (e){
        console.log(e);
    }

    matches = matches.filter(u => u.username !== MyUsername);

    if(matches.length === 0){
        result.innerHTML = "<div class='chat-search-empty'>no friends match</div>";
        return;
    }

    for(let i=0; i<matches.length; i++){
        const user = matches[i];

        const item = document.createElement("div");
        item.className = "chat-search-item";
        item.onclick = function (){
            result.style.display = "none";
            document.getElementById("othersUsername").value = user.username;
            openFriendChatWithId(user.id);
        };

        const img = document.createElement("img");
        img.src = user.avatarUrl || "/img/avatars/default.png";
        img.onerror = function (){
            this.src = "/img/avatars/default.png";
        };

        const name = document.createElement("span");
        name.textContent = user.username;

        item.appendChild(img);
        item.appendChild(name);
        result.appendChild(item);
    }
}

document.addEventListener("click", function (e){
    const wrapper = document.querySelector(".user-search-wrapper");
    if(wrapper && !wrapper.contains(e.target)){
        const result = document.getElementById("friendSearchResults");
        if(result) result.style.display = "none";
    }
});

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

    const token = localStorage.getItem("token");
    const url = `/chat/create-group?name=${encodeURIComponent(groupName)}&type=GROUP`;
    await openRoomRequest(url,{
        method: "POST",
        headers: {"Authorization": "Bearer " + token,
            "Content-Type": "application/json"},
        body: JSON.stringify(memberIds)
    });

    document.getElementById("groupName").value="";
    document.getElementById("groupUserNames").value="";
}

async function openRoomRequest(url, options){
    try{
        if(!options.headers){
            options.headers = {};
        }

        const token = localStorage.getItem("token");
        if(token && !options.headers["Authorization"]){
            options.headers["Authorization"] = "Bearer " + token;
        }

        const res = await fetch(`${API_BASE}${url}`, options);
        if(!res.ok){
            const text = await res.text();
            alert(text || "Unable to open chat");
            return;
        }

        const room = await res.json();
        if (room.id !== roomId) {
            renderedMessageIds = new Set();
            const box = document.getElementById("messages");
            if (box) box.innerHTML = "";
        }
        roomId = room.id;

        document.getElementById("roomIdLabel").textContent = roomId;
        document.getElementById("chatBox").classList.add("visible");


        loadMessages();
        if(pollTimer) clearInterval(pollTimer);
        pollTimer = setInterval(loadMessages, 3000);
    }catch (err){
        console.error(err);
        alert("Unable to connect to the server");
    }
}


function renderMessage(m, box){
    const isMine = String(m.senderId) === String(MyUserId);

    const div = document.createElement("div");
    div.className = "msg " + (isMine ? "mine" : "theirs");

    if(!isMine){
        const nameElem = document.createElement("div");
        nameElem.className = "senderName";
        nameElem.textContent = m.senderUsername;
        div.appendChild(nameElem);
    }

    div.appendChild(document.createTextNode(m.message));
    box.appendChild(div);
}

async function loadMessages(){
    const res = await fetch(`${API_BASE}/chat/${roomId}`);
    if(!res.ok) return;

    const messages = await res.json();
    const box = document.getElementById("messages");

    //only if not already rendered, KACHOW
    let appendedAny = false;
    for(let i = 0; i < messages.length; i++){
        const m = messages[i];
        if (renderedMessageIds.has(m.id)) {
            continue;
        }
        renderMessage(m, box);
        renderedMessageIds.add(m.id);
        appendedAny = true;
    }

    if (appendedAny) {
        box.scrollTop = box.scrollHeight;
    }
}

async function sendMessage(){
    const input = document.getElementById("messageInput");
    const text = input.value.trim();
    if(!text) return;
    input.value = "";
    const box = document.getElementById("messages");
    const tempDiv = document.createElement("div");
    tempDiv.className = "msg mine";
    tempDiv.appendChild(document.createTextNode(text));
    tempDiv.style.opacity = "0.6";
    box.appendChild(tempDiv);
    box.scrollTop = box.scrollHeight;

    const token = localStorage.getItem("token");

    try {
        const res = await fetch(`${API_BASE}/chat/send`, {
            method: "POST",
            headers: {"Authorization": "Bearer " + token,
                "Content-Type":"application/json"},
            body: JSON.stringify({
                senderId: MyUserId,
                roomId: roomId,
                message: text
            })
        });

        if (res.ok) {
            const saved = await res.json();
            renderedMessageIds.add(saved.id);
            tempDiv.style.opacity = "1";
        } else {
            tempDiv.style.opacity = "1";
            tempDiv.style.color = "red";
            tempDiv.appendChild(document.createTextNode(" (failed to send)"));
        }
    } catch (e) {
        tempDiv.style.opacity = "1";
        tempDiv.style.color = "red";
        tempDiv.appendChild(document.createTextNode(" (failed to send)"));
    }
}

document.addEventListener("DOMContentLoaded", function (){
    const input = document.getElementById("messageInput");
    if(input){
        input.addEventListener("keydown", function (e){
            if(e.key === "Enter"){
                sendMessage();
            }
        });
    }
});

async function openChatById(id){
    if(!MyUserId){
        console.error("User identity not loaded yet.")
    }

    if (id !== roomId) {
        renderedMessageIds = new Set();
        const box = document.getElementById("messages");
        if (box) box.innerHTML = "";
    }
    roomId = id;

    document.getElementById("roomIdLabel").textContent=roomId;
    document.getElementById("chatBox").classList.add("visible");

    loadMessages();

    if(pollTimer) clearInterval(pollTimer);
    pollTimer = setInterval(loadMessages, 3000);
}