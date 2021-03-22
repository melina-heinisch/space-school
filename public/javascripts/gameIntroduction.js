/* ----------------- Zeigt Modal auf /main wenn noch nichts gespielt ----------------------- */

$(document).ready(function () {
    prep_modal();
    $.post("/currentProgress")
        .done(function (data, status) {
            let obj = JSON.parse(data);
            if (obj.id === 1 && obj["collect"] === false) {
                let introShown = sessionStorage.getItem("introShown");
                if (introShown === "false") {
                    $("#intro_Modal").modal("show");
                    sessionStorage.setItem("introShown", "true");
                }
            }
        });
});

const closeBtn = document.getElementById("closeModal");
closeBtn.addEventListener("click", () => $('#intro_Modal').modal("hide"));