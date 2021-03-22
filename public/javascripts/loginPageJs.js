function login() {
    let username = document.getElementById("userName").value;
    let password = document.getElementById("password").value;
    let loggedIn = "true";

    $.post("/login", {username: username, password: password, loggedIn: loggedIn})
        .done(function (data, status) {
            if (status === "success")
                sessionStorage.setItem("profilePictures", data);
            sessionStorage.setItem("introShown", "false");
            sessionStorage.setItem("tutorialShown", "false");
            window.location.href = "/main";
        })
        .fail(function () {
            window.alert("Benutzername oder Passwort sind falsch. Bitte versuche es noch einmal.");
        });
}

document.addEventListener("keypress", function (event) {
    if (event.key === 'Enter')
        login()
});