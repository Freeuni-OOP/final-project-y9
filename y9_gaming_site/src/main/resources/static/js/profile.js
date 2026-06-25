const API_BASE = "/api/users";

const avatarImg = document.getElementById("avatar-img");
const usernameDisplay = document.getElementById("username-display");
const uploadSection = document.getElementById("avatar-upload-section");
const avatarForm = document.getElementById("avatar-form");
const avatarInput = document.getElementById("avatar-input");
const uploadStatus = document.getElementById("upload-status");
const errorMessage = document.getElementById("error-message");

function showError(message) {
    errorMessage.textContent = message;
    errorMessage.hidden = false;
}

function loadProfile() {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "/login";
        return;
    }

    fetch(`${API_BASE}/${userId}`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Could not load this profile.");
            }
            return response.json();
        })
        .then(function (profile) {
            // 1. Populate Profile Card Information
            usernameDisplay.textContent = profile.username;
            if (profile.avatarUrl) {
                avatarImg.src = profile.avatarUrl;
            }

            // 2. Set Navbar Details for the current session safely inside the .then block
            const loggedInUsername = localStorage.getItem("username");
            if (document.getElementById("nav-username")) {
                document.getElementById("nav-username").textContent = loggedInUsername || "...";
            }

            if (document.getElementById("nav-avatar")) {
                // If viewing your own profile, sync the navbar avatar image to the profile's image
                if (loggedInUsername && loggedInUsername === profile.username && profile.avatarUrl) {
                    document.getElementById("nav-avatar").src = profile.avatarUrl;
                }
            }

            // 3. Show upload controls if you are viewing your own dashboard
            if (loggedInUsername && loggedInUsername === profile.username) {
                uploadSection.hidden = false;
            }
        })
        .catch(function (err) {
            showError(err.message);
        });
}

// Show selected filename text
avatarInput.addEventListener("change", function () {
    if (avatarInput.files[0]) {
        uploadStatus.textContent = avatarInput.files[0].name;
    }
});

// Handle image submission upload
avatarForm.addEventListener("submit", function (event) {
    event.preventDefault();
    const file = avatarInput.files[0];
    if (!file) {
        uploadStatus.textContent = "Choose a photo first.";
        return;
    }

    const token = localStorage.getItem("token");
    const formData = new FormData();
    formData.append("avatar", file);

    uploadStatus.textContent = "Uploading...";

    fetch(`${API_BASE}/avatar`, {
        method: "POST",
        headers: { "Authorization": "Bearer " + token },
        body: formData
    })
        .then(response => {
            if (!response.ok) return response.text().then(text => { throw new Error(text || "Upload failed."); });
            return response.json();
        })
        .then(result => {
            avatarImg.src = result.avatarUrl;
            // Also update the navbar avatar simultaneously
            if (document.getElementById("nav-avatar")) {
                document.getElementById("nav-avatar").src = result.avatarUrl;
            }
            uploadStatus.textContent = "Avatar updated successfully!";
        })
        .catch(err => {
            uploadStatus.textContent = err.message;
        });
});

async function logout() {
    try {
        await fetch("/api/users/logout", { method: "POST" });
    } catch (e) {
        console.error("Backend logout clean call skipped.");
    }
    localStorage.clear();
    window.location.href = "/login";
}

// Initialize execution
loadProfile();
