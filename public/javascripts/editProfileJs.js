let currentPicSrc = findCurrentPicSrc();
let allPics = JSON.parse(sessionStorage.getItem("profilePictures"));
let currImg = findImgBySrc();
let chosenPictureID;
let oldUsername;

$(document).ready(function () {
    oldUsername = document.getElementById("newName").value;
    currentPicSrc = findCurrentPicSrc();
    setAllPictures();
    currImg = findImgBySrc();
    setChosenID(currImg.id);
    setBorder("select");
    $(".profilePicture").click(function () {
        setBorder("unselect");
        chosePicture(this.id);
    });
});

function chosePicture(pic_id) {
    currImg = document.getElementById(pic_id);
    setChosenID(pic_id);
    setBorder("select");
}

function saveChanges() {
    let username = document.getElementById("newName").value; //neuer Name wird eingelesen
    if (username !== "") {
        updateAllPics();
        $.post("/edit", {username: username, newPic: chosenPictureID.toString(), oldName: oldUsername})
            .done(function (data, status) {
                if (status === "success") {
                    sessionStorage.setItem("profilePictures", JSON.stringify(allPics));

                    $("#changedProfile").modal("show");
                    $('#changedProfile').delay(1500).fadeOut(450);
                    setTimeout(function () {
                        $('#changedProfile').modal("hide");
                        window.location.href = "/profile";
                    }, 1950);
                }
            })
            .fail(function () {
                window.alert("Der Nutzername ist bereits vergeben. Bitte wähle einen anderen.");
            });
    } else {
        window.alert("Der Nutzername ist leer. Bitte gib einen Nutzernamen ein!");
    }
}

function findImgBySrc() {
    let images = document.getElementsByTagName('img');
    for (let i = 0; i < images.length; ++i) {
        if (images[i].getAttribute("src") === currentPicSrc)
            return images[i];
    }
}

function setBorder(type) {
    switch (type) {
        case "select":
            currImg.setAttribute("style", "border: 3px solid #FECD1A");
            currImg.setAttribute("style", "background-color: #FECD1A");
            break;
        case "unselect":
            currImg.setAttribute("style", "border: none");
            currImg.setAttribute("style", "background-color: transparent");
    }
}

function setAllPictures() {

    allPics.forEach((jObj) => {
        let image = document.getElementById("pic_" + jObj.id.toString());
        if (image !== null)
            $(image).attr("src", "data:image/png;base64," + jObj["profilePic"]);

    });
}

function findCurrentPicSrc() {
    let allPics = JSON.parse(sessionStorage.getItem("profilePictures"));
    let result = "";
    allPics.forEach(jObj => {
        let isCurrent = jObj["isCurrPic"];
        if (isCurrent === true) {
            result = "data:image/png;base64," + jObj["profilePic"];
        }
    });
    return result;
}

/** geht über alle möglichen Profilbilder in der Session und setzt für das ausgewählte die info auf true,
 *  d.h. kennzeichnet es als aktuelles Profilbild**/
function updateAllPics() {
    allPics.forEach(jObj => {
        jObj["isCurrPic"] = jObj.id.toString() === chosenPictureID.toString();
    })
}

function setChosenID(pic_id) {
    let id_Array = pic_id.split("_");
    chosenPictureID = id_Array[1];
}
