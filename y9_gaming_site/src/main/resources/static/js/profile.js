const API_BASE = "/api/users";

const token = localStorage.getItem('token')
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
    fetch(`${API_BASE}/${userId}`)
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Could not load this profile.");
            }
            return response.json();
        })
        .then(function (profile) {
            usernameDisplay.textContent = profile.username;
            if (profile.avatarUrl) {
                avatarImg.src = profile.avatarUrl;
            }

            // Only the profile's own owner sees the upload control.
            const loggedInUsername = localStorage.getItem("username");
            if (loggedInUsername && loggedInUsername === profile.username) {
                uploadSection.hidden = false;
            }
        })
        .catch(function (err) {
            showError(err.message);
        });
}


avatarInput.addEventListener("change", function () {
    if (avatarInput.files[0]) {
        uploadStatus.textContent = avatarInput.files[0].name;
    }
});

avatarForm.addEventListener("submit", function (event) {
    event.preventDefault();

    const file = avatarInput.files[0];
    if (!file) {
        uploadStatus.textContent = "Choose a photo first.";
        return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
        uploadStatus.textContent = "You need to be logged in to do this.";
        return;
    }

    const formData = new FormData();
    formData.append("avatar", file);

    uploadStatus.textContent = "Uploading...";

    fetch(`${API_BASE}/avatar`, {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + token
        },
        body: formData
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (text) {
                    throw new Error(text || "Upload failed.");
                });
            }
            return response.json();
        })
        .then(function (result) {
            avatarImg.src = result.avatarUrl;
            uploadStatus.textContent = result.message;
        })
        .catch(function (err) {
            uploadStatus.textContent = err.message;
        });
});

loadProfile();