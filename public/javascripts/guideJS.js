window.onload = function () {
    getImagesAndText();
    getProgress();
};

let levelTags = ["satellite", "suit", "launcher"];
let images = new Map();
let texts = new Map();
let progress = [false, false, false, false, false, false];

let satelliteImg = selectID("satelliteImg");
let suitImg = selectID("suitImg");
let launcherImg = selectID("launcherImg");

let satelliteText = selectID("satelliteText");
let suitText = selectID("suitText");
let launcherText = selectID("launcherText");
let pageNumber = selectID("pageNumber");

let startingPage = selectID("startingPage");
let satelliteImagePage = selectID("satelliteImagePage");
let satelliteTextPage = selectID("satelliteTextPage");
let suitImagePage = selectID("suitImagePage");
let suitTextPage = selectID("suitTextPage");
let launcherImagePage = selectID("launcherImagePage");
let launcherTextPage = selectID("launcherTextPage");
let lockedPage = selectID("locked");

let pages = [startingPage, satelliteImagePage, satelliteTextPage, suitImagePage, suitTextPage, launcherImagePage, launcherTextPage];

let currentPage = 0;

function forward() {
    pages[currentPage].classList.add("hidden");
    lockedPage.classList.add("hidden");
    if (currentPage === 6) {
        currentPage = 0;
    } else {
        currentPage++;
    }
    pageNumber.innerText = currentPage + 1 + "/7";
    if (currentPage !== 0 && !progress[currentPage - 1]) {
        lockedPage.classList.remove("hidden");
    } else {
        pages[currentPage].classList.remove("hidden");
    }
}

function backwards() {
    pages[currentPage].classList.add("hidden");
    lockedPage.classList.add("hidden");

    if (currentPage === 0) {
        currentPage = 6;
    } else {
        currentPage--;
    }
    pageNumber.innerText = currentPage + 1 + "/7";

    if (currentPage !== 0 && !progress[currentPage - 1]) {
        lockedPage.classList.remove("hidden");
    } else {
        pages[currentPage].classList.remove("hidden");
    }
}

function getImagesAndText() {
    $.post("/guide")
        .done(function (data, status) {
            loadImagesAndText(JSON.parse(data))
        });
}

function loadImagesAndText(jsonArray) {
    jsonArray.forEach((object) => {
        texts.set(levelTags[object.id - 1] + "Text", object.info);
        images.set(levelTags[object.id - 1] + "Image", object.image);
    });

    satelliteText.innerText = texts.get("satelliteText");
    suitText.innerText = texts.get("suitText");
    launcherText.innerText = texts.get("launcherText");

    satelliteImg.src = "data:image/png;base64," + images.get("satelliteImage");
    suitImg.src = "data:image/png;base64," + images.get("suitImage");
    launcherImg.src = "data:image/png;base64," + images.get("launcherImage");
}

function getProgress() {
    $.post("/progress")
        .done(function (data, status) {
            saveProgress(JSON.parse(data));
        });
}

function saveProgress(jsonArray) {
    jsonArray.forEach((object) => {
        if (object.id === "1") {
            progress[0] = object.puzzle;
            progress[1] = object.quiz;
        } else if (object.id === "2") {
            progress[2] = object.puzzle;
            progress[3] = object.quiz;
        } else if (object.id === "3") {
            progress[4] = object.puzzle;
            progress[5] = object.quiz;
        }
    });
}

function selectID(elem) {
    return document.getElementById(elem);
}