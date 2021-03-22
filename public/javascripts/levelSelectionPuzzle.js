window.onload = function () {
    getProgress();
};

function openLevel(number) {
    if (number >= 1 && number <= 3) {
        sessionStorage.setItem("currentlevel", number);

        //Speichern des Levels in Session
        $.post("/mainGame", {currentlevel: number})
            .done(function (data, status) {
                sessionStorage.setItem("data", data);
                if (status === "success")
                    location.href = "/puzzle";
            });
    } else {
        window.alert("This level is not implemented yet.");
    }
}

function getProgress() {
    $.post("/currentProgress")
        .done(function (data) {
            showLevelForProgress(JSON.parse(data));
        });
}

function showLevelForProgress(object) {
    let highestLevel = object.id;
    let collect = object.collect;

    if (highestLevel === 3) {
        $("#lvl1Icon").attr("style", "opacity:1");
        $("#lvl2Icon").attr("style", "opacity:1");

        if (collect === true) {
            $("#lvl3Icon").attr("style", "opacity:1");
        } else {
            $("#lvl3Icon").css("pointer-events", "none");
        }


    } else if (highestLevel === 2) {
        $("#lvl1Icon").attr("style", "opacity:1");
        $("#lvl3Icon").css("pointer-events", "none");

        if (collect === true) {
            $("#lvl2Icon").attr("style", "opacity:1");
        } else {
            $("#lvl2Icon").css("pointer-events", "none");
        }

    } else if (highestLevel === 1) {
        $("#lvl2Icon").css("pointer-events", "none");
        $("#lvl3Icon").css("pointer-events", "none");

        if (collect === true) {
            $("#lvl1Icon").attr("style", "opacity:1");
        } else {
            $("#lvl1Icon").css("pointer-events", "none");
        }
    }
    setImages(highestLevel, collect)
}

function setImages(highestLvl, collect) {
    let img1Open = "/assets/images/levelSelection/level1/satelliteOpen.png";
    let img2Open = "/assets/images/levelSelection/level2/spaceSuitOpen.png";
    let img3Open = "/assets/images/levelSelection/level3/launcherOpen.png";

    if (highestLvl >= 1 && collect) {
        $("#lvl1Pic").attr("src", img1Open);
    }
    if (highestLvl >= 2 && collect) {
        $("#lvl2Pic").attr("src", img2Open);
    }
    if (highestLvl === 3 && collect) {
        $("#lvl3Pic").attr("src", img3Open);
    }
}