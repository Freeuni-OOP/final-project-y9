const API_BASE=""

let MyUserId = null;
let MyUsername = null;
let roomId = null;
let pollTimer = null;
let selectedMemberIds = [];
let renderedMessageIds = new Set();

document.addEventListener("DOMContentLoaded", loadMyIdentity);

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
    try {
        const res = await fetch(`${API_BASE}/chat/find-user/${encodeURIComponent(username)}`);
        if (!res.ok) {
            const text = await res.text();
            showError(`This user does not exist: ${username} womp womp`);
            return null;
        }
        const data = await res.json();
        return data.id;
    } catch (err) {
        showError("Could not connect to the server. Please try again.");
    }

}

async function openFriendChat(){
    hideError();
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

    matches = matches.filter(function (u){return u.username !== MyUsername});

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
            showError( "You are not friends with the user womp womp");
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

    if(m.flagged){
        div.style.color = "red";
    }

    div.appendChild(document.createTextNode(m.message));
    box.appendChild(div);
}

async function loadMessages(){
    if(!roomId){
        return;
    }
    const res = await fetch(`${API_BASE}/chat/${roomId}`);
    if(!res.ok) return;

    const messages = await res.json();
    const box = document.getElementById("messages");

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
            if(saved.flagged){
                tempDiv.textContent = "";
                tempDiv.style.color = "red";
                tempDiv.appendChild(document.createTextNode(saved.message));
            }
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

async function toggleFriendDropdown(){
    const dropdown = document.getElementById("friendDropdown");

    if(dropdown.style.display === "block"){
        dropdown.style.display = "none";
        return;
    }

    dropdown.innerHTML = "<div class='chat-search-empty'>Loading friends...</div>";
    dropdown.style.display = "block";

    const token = localStorage.getItem("token");
    if(!token || !MyUserId)return;

    try {
        const res = await fetch(`${API_BASE}/friends/accepted/${MyUserId}`, {
            headers: {"Authorization": `Bearer ${token}`}
        });

        const friendships = await res.json();

        const availableFriendships = friendships.filter(function (f) {
            if (f.senderId === MyUserId) {
                return !selectedMemberIds.includes(f.receiverId);
            } else {
                return !selectedMemberIds.includes(f.senderId);
            }
        });

        if(availableFriendships.length === 0){
            dropdown.innerHTML = "<div class='chat-search-empty'>No friends available to add</div>";
            return;
        }

        dropdown.innerHTML = "";

        for(let i=0; i<availableFriendships.length; i++) {
            const f = availableFriendships[i];

            let friendId;
            if (f.senderId === MyUserId) {
                friendId = f.receiverId;
            } else {
                friendId = f.senderId;
            }

            const userRes = await fetch(`${API_BASE}/api/users/${friendId}`, {
                headers: {"Authorization": `Bearer ${token}`}
            });

            if (userRes.ok) {
                const friend = await userRes.json();

                const item = document.createElement("div");
                item.className = "chat-search-item";
                item.style.display = "flex";
                item.style.alignItems = "center";
                item.style.gap = "10px";

                item.onclick = function () {
                    addMemberToGroupList(friend.id, friend.username);
                    dropdown.style.display = "none";
                };

                const img = document.createElement("img");
                img.src = friend.avatarUrl || "/img/avatars/default.png";
                img.style.width = "24px";
                img.style.height = "24px";
                img.style.borderRadius = "50%";

                const name = document.createElement("span");
                name.textContent = friend.username;

                item.appendChild(img);
                item.appendChild(name);
                dropdown.appendChild(item);
            }
        }
    }catch (err){
        console.error(err);
        dropdown.innerHTML = "<div class='chat-search-empty'>Connection error</div>";
    }
}

function addMemberToGroupList(id, username){
    if(selectedMemberIds.includes(id)){
        return;
    }

    selectedMemberIds.push(id);

    const group = document.getElementById("selectedMembersGroup");

    const badge = document.createElement("div");
    badge.id = `member-badge-${id}`;
    badge.style.background = "rgba(179, 39, 201, 0.25)";
    badge.style.border = "1px solid #b327c9";
    badge.style.borderRadius = "20px";
    badge.style.padding = "5px 12px";
    badge.style.fontSize = "13px";
    badge.style.display = "flex";
    badge.style.alignItems = "center";
    badge.style.gap = "8px";
    badge.style.color = "#fff";

    badge.innerHTML = `<span>${escapeHtml(username)}</span>
                       <span onclick="removeMemberFromGroupList(${id})" style="cursor:pointer; color:#ff6b9d; font-weight:bold:">✖️</span>`;

    group.appendChild(badge);
}

function removeMemberFromGroupList(id){
    for(let i=0; i<selectedMemberIds.length; i++){
        if(selectedMemberIds[i] === id){
            selectedMemberIds.splice(i, 1);
            break;
        }
    }
    const badge = document.getElementById(`member-badge-${id}`);
    if(badge){
        badge.remove();
    }
}

async function createGroup(){
    hideError();
    if(!MyUserId){
        alert("Reload page");
        return;
    }

    const groupName = document.getElementById("groupName").value.trim() || "Group Chat";

    if(selectedMemberIds.length === 0){
        alert("Please add at least one member to the group");
        return;
    }

    const allMemberIds = [MyUserId, ...selectedMemberIds];

    const token = localStorage.getItem("token");
    const url = `/chat/create-group?name=${encodeURIComponent(groupName)}&type=GROUP`;
    await openRoomRequest(url,{
        method: "POST",
        headers: {"Authorization": "Bearer " + token,
            "Content-Type": "application/json"},
        body: JSON.stringify(allMemberIds)
    });

    document.getElementById("groupName").value="";
    document.getElementById("selectedMembersGroup").innerHTML = "";
    selectedMemberIds = [];
}

function showError(message) {
    document.getElementById('error-text').textContent = message;
    document.getElementById('error-banner').style.display = 'block';
}

function hideError() {
    document.getElementById('error-banner').style.display = 'none';
}


document.addEventListener("click", function(e){
    const selectionWrapper = document.querySelector(".group-members-selection");
    if(selectionWrapper && !selectionWrapper.contains(e.target)){
        const dropdown = document.getElementById("friendDropdown");
        if(dropdown){
            dropdown.style.display = "none";
        }
    }
});