function createUser() {
    let username = document.getElementById("userName").value;
    let password1 = document.getElementById("password1").value;
    let password2 = document.getElementById("password2").value;

    if (password1 !== password2) {
        window.alert("Die Passwörter müssen gleich sein.");
    } else {
        let user
            = {username: username, password: password1};

        $.post("/register", user)
            .done(function (data, status) {
                if (status === "success")
                    sessionStorage.setItem("profilePictures", data);
                sessionStorage.setItem("introShown", "false");
                sessionStorage.setItem("tutorialShown", "false");
                window.location.href = "/main";
            })
            .fail(function () {
                window.alert("Der Nutzername ist bereits vergeben. Bitte wähle einen anderen.")
            });
    }
}