let searchTime = null;

function onSearch(search){
    clearTimeout(searchTime);

    const result = document.getElementById("searchResults");

    if(search.trim().length<2){
        result.style.display = "none";
        result.innerHTML = "";
        return;
    }

    searchTime = setTimeout(function (){
        runSearch(search);
    }, 300);
}

async function runSearch(search){
    const result = document.getElementById("searchResults");
    result.innerHTML = "";
    result.style.display = "block";

    const gameMatches = []
    const games = window.cachedGamesList || []

    for(let i=0; i<games.length; i++){
        const game = games[i];

        if(game.title.toLowerCase().includes(search.toLowerCase())){
            gameMatches.push(game);
        }
        if(gameMatches.length === 5){
            break;
        }
    }

    let userMatches = [];
    const token = localStorage.getItem("token");
    try {
        const res = await fetch("/api/users/search?query=" + encodeURIComponent(search), {
            headers: {"Authorization": "Bearer " + token}
        });
        if (res.ok) {
            userMatches = await res.json();
        }
    }catch (e){
        console.log(e);
    }

    if(gameMatches.length === 0 && userMatches.length === 0){
        result.innerHTML = "<div style= 'padding:16px; color:#aaa; text-align:center;'>no result</div>";
        return;
    }

    if(gameMatches.length > 0){
        const gameTitle = document.createElement("div");
        gameTitle.className = "search-section-title";
        gameTitle.textContent = "games";
        result.appendChild(gameTitle);

        for(let i=0; i < gameMatches.length; i++){
            const game = gameMatches[i];
            const item = document.createElement("div");
            item.className = "search-item";
            item.onclick = function (){
                result.style.display = "none";
                document.getElementById("search-input").value = "";
                window.handlePlayGame(game.id, game.gameType, game.sourceUrl, game.title);
            };

            const img = document.createElement("img");
            img.src = game.iconUrl;
            img.onerror = function (){
                this.src = "/img/games/default.png";
            };

            const name = document.createElement("span");
            name.textContent = game.title;

            item.appendChild(img);
            item.appendChild(name);
            result.appendChild(item);
        }
    }

    if(userMatches.length > 0){
        const userTitle = document.createElement("div");
        userTitle.className = "search-section-title";
        userTitle.textContent = "user";
        result.appendChild(userTitle);

        for(let i=0; i<userMatches.length; i++){
            const user = userMatches[i];

            const item = document.createElement("a");
            item.className = "search-item";
            item.href = "/profile/" + user.id;

            const img = document.createElement("img");
            img.src = user.avatarUrl || "/img/avatars/default.png";
            img.onerror = function (){
                this.src = "/img/avatars/default.png";
            };
            img.style.borderRadius = "50%";

            const name = document.createElement("span");
            name.textContent = user.username;

            item.appendChild(img);
            item.appendChild(name);
            result.appendChild(item);
        }
    }
}

document.addEventListener("click", function (e){
    const wrapper = document.querySelector(".search-wrapper");
    if(wrapper && !wrapper.contains(e.target)){
        document.getElementById("searchResults").style.display = "none";
    }
});