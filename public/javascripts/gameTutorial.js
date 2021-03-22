/* ----------------- Zeigt Modal auf /mainGame wenn erstes Mal spielen ----------------------- */

$(document).ready(function () {
    prep_modal();
    if (currentLevel === 1) {
        $.post("/currentProgress")
            .done(function (data, status) {
                let obj = JSON.parse(data);

                if (obj["collect"] === false) {
                    let tutorialShown = sessionStorage.getItem("tutorialShown");
                    if (tutorialShown === "false") {
                        $("#tutorial_Modal").modal("show");
                        sessionStorage.setItem("tutorialShown", "true");
                    }
                }
            });
    }
});

const closeBtn = document.getElementById("closeModal");
closeBtn.addEventListener("click", () => $('#tutorial_Modal').modal("hide"));

