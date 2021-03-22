function toEdit() {
    window.location.href = "/edit"
}

$(document).ready(function () {
    let currPicSrc = findCurrentPic();
    $("#profilePicture").attr("src", "data:image/png;base64," + currPicSrc.valueOf());
    getRewardIcons();
});

function findCurrentPic() {
    let allPics = JSON.parse(sessionStorage.getItem("profilePictures"));
    let result = "";
    allPics.forEach(jObj => {
        let isCurrent = jObj["isCurrPic"];
        if (isCurrent === true) {
            result = jObj["profilePic"];
        }
    });
    return result;
}

function getRewardIcons() {
    let name = document.getElementById("presentedName").innerText;
    $.post("/rewardIcon", {username: name})
        .done(function (data, status) {
            loadRewardIcons(JSON.parse(data));
        });
}

function loadRewardIcons(jsonArray) {
    jsonArray.forEach((object) => {
        if (object["level"] === 1) {
            document.getElementById("rewardIcon1").src = "data:image/png;base64," + object.rewardImage;
        } else if (object["level"] === 2) {
            document.getElementById("rewardIcon2").src = "data:image/png;base64," + object.rewardImage;
        } else if (object["level"] === 3) {
            document.getElementById("rewardIcon3").src = "data:image/png;base64," + object.rewardImage;
        }
    });
}