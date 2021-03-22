let levelData = new LevelData(currentLevel);
let bubbleCounter = 0;
let pieceCounter = 0;

let quizBtnModal = document.getElementById("quiz_btn_modal");
let quizBtnNoModal = document.getElementById("quiz_btn_noModal");

let names, sizeFactor, loadLeft, loadOver, loadUnder, silhouette_src;
if (currentLevel === 1) {
    sizeFactor = 0.88;
    names = ["power", "housing", "antenna", "thermal_control", "guidance_control", "command_datahandling", "transponder"];
    loadLeft = ["housing"];
    loadOver = ["antenna"];
    loadUnder = ["transponder"];
    silhouette_src = "assets/images/puzzleSilhouettes/satellite.png";
} else if (currentLevel === 2) {
    sizeFactor = 0.83;
    loadLeft = ["helmet", "helmet_inside", "torso", "shoes"];
    loadOver = [];
    loadUnder = [];
    silhouette_src = "assets/images/puzzleSilhouettes/suit.png";
    names = ["shoes", "backpack", "pants", "torso", "arm", "glove", "helmet_inside", "helmet"]; //has to be in this order!
} else if (currentLevel === 3) {
    sizeFactor = 0.396;
    names = ["structuralSystem", "payloadFairing", "launchAbortSystem", "serviceModule", "crewCargoModule",
        "guidanceSystem", "nozzle", "rocketEngine", "liquidFuelOxygen"];
    loadOver = [];
    loadUnder = [];
    loadLeft = ["nozzle", "crewCargoModule", "guidanceSystem", "structuralSystem", "payloadFairing"];
    silhouette_src = "assets/images/puzzleSilhouettes/spaceLauncher.png";
}

$(document).ready(function () {
    let zIndex = 400;
    document.getElementById("silhouette").src = silhouette_src;

    // load placeholders for the info speech bubbles
    names.forEach(function (name) {
        setTextPlaceholders(name);
    });

    // load the images of puzzle pieces
    names.forEach(function (name) {
        let thisImg = createImage(name, zIndex, sizeFactor, "", "images", levelData.typeMap);
        loadRandomly(thisImg);
        zIndex++;
    });

    // load info speech bubbles
    names.forEach(function (name) {
        let thisImg = createImage(name, zIndex, 0.5, "_text", "infoText", levelData.bubbleMap);
        loadRandomly(thisImg);
        zIndex++;
    });
});

/*
    fills the invisible bubble target divs with the placeholder images for the info bubbles
    there are 4 different placeholder images
 */
function setTextPlaceholders(name) {
    let placeholder = document.createElement("img");
    if (loadLeft.includes(name)) {
        placeholder.src = "assets/images/bubbles/ph_left.png";
    } else if (loadOver.includes(name)) {
        placeholder.src = "assets/images/bubbles/ph_over.png"
    } else if (loadUnder.includes(name)) {
        placeholder.src = "assets/images/bubbles/ph_under.png"
    } else {
        placeholder.src = "assets/images/bubbles/ph_right.png";
    }
    placeholder.style.height = "100%"; //to fit the parent div
    placeholder.style.opacity = "0.8";
    document.getElementById(name + "_bubble_target").appendChild(placeholder);
}

/*
    creates the images from the given array
 */
function createImage(name, zIndex, sizeFactor, idExtra, className, imgMap) {
    const img = document.createElement("img");
    imgMap.forEach(function (values, key) {
        if (name === values) {
            img.src = key;
        }
    });
    img.className = className;
    img.id = name + idExtra;
    img.height *= sizeFactor;
    img.width *= sizeFactor;
    // the "upper" parts (helmet) are overlapping the lower parts (shoes)
    // zIndex is incremented after each function call
    img.style.zIndex = zIndex;
    img.style.position = "absolute";
    return img;
}

function loadRandomly(img) {
    let random12 = Math.floor(Math.random() * 2) + 1;
    if (random12 === 1) { //let the parts appear randomly on the left and right of the silhouette
        let sideWidth = document.getElementById("right").offsetWidth;
        let sideHeight = document.getElementById("right").offsetHeight;
        let sideLeft = document.getElementById("right").offsetLeft;
        let sideTop = document.getElementById("right").offsetTop;
        img.style.top = (sideTop + (Math.random() * (sideHeight - img.height))).toString() + "px";
        img.style.left = (sideLeft + (Math.random() * (sideWidth - img.width))).toString() + "px";
        document.getElementById("right").appendChild(img);
    } else {
        let sideWidth = document.getElementById("left").offsetWidth;
        let sideHeight = document.getElementById("left").offsetHeight;
        let sideLeft = document.getElementById("left").offsetLeft;
        let sideTop = document.getElementById("left").offsetTop;
        img.style.top = (sideTop + (Math.random() * (sideHeight - img.height))).toString() + "px";
        img.style.left = (sideLeft + (Math.random() * (sideWidth - img.width))).toString() + "px";
        document.getElementById("left").appendChild(img);
    }
}

$(function () { //make things draggable as soon as they are loaded
    /*
    https://www.elated.com/res/File/articles/development/javascript/jquery/drag-and-drop-with-jquery-your-essential-guide/card-game.html
    was really useful!
     */

    $(".infoText").draggable({
        containment: "#container"
    })

    $(".bubble_target").droppable({
        drop: function (event, ui) {
            if (ui.draggable.attr('id').includes("text")) {
                let dropName = $(this).attr('id').replace(/_bubble_target/, "");
                let dragName = ui.draggable.attr('id').replace(/_text/, "");
                if (dropName === dragName) { // if text and placeholder are for the same part, let the text snap in on mouse up
                    $(this).droppable('disable');
                    ui.draggable.draggable('disable'); // once snapped into the right place, it's not draggable anymore
                    ui.draggable.position({of: $(this), my: 'left top', at: 'left top'});
                    ui.draggable.css('cursor', 'default');
                    bubbleCounter++;

                    if (bubbleCounter === names.length && pieceCounter === names.length) {
                        setUserProgress();
                    }
                }
            }
        }
    });

    $(".images").draggable({
        containment: "#container"
    });

    $(".parts_target").droppable({
        drop: function (event, ui) {
            if (!ui.draggable.attr('id').includes("text")) {
                let dropName = $(this).attr('id').replace(/_target/, "");
                let dragName = $(ui.draggable).attr('id');
                if (dropName === dragName) {
                    ui.draggable.draggable('disable');
                    $(this).droppable('disable');
                    ui.draggable.position({of: $(this), my: 'left top', at: 'left top'});
                    ui.draggable.css('cursor', 'default');
                    pieceCounter++;

                    if (bubbleCounter === names.length && pieceCounter === names.length) {
                        setUserProgress();
                    }
                }
            }
        }
    });
})

function setUserProgress() {
    $.post("/puzzleProgress", {gameLevel: currentLevel})
        .done(function (data) {
            if (data === "true") {
                quizBtnModal.classList.remove("quiz_btn-hidden");
            } else {
                document.getElementById("quiz_btn_noModal").setAttribute('onclick', 'openLevel(' + currentLevel + ')');
                quizBtnNoModal.classList.remove("quiz_btn-hidden");
            }
        });
}