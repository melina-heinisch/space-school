let allUsers = JSON.parse(sessionStorage.getItem("userlist"));
let selectedUser = sessionStorage.getItem("selectedFriendID");

let nameSpans = document.getElementsByClassName("friend_name");

$(document).ready(function () {
    let currentUser = "";

    allUsers.forEach(jObj => {
        if (jObj["userID"] === selectedUser) {
            currentUser = jObj;
        }
    });

    nameSpans.item(0).innerText = currentUser["username"];
    nameSpans.item(1).innerText = currentUser["username"];
    $("#profilePicture").attr("src", "data:image/png;base64," + currentUser["friendPic"]);
    document.getElementById("friend_rank").innerText = currentUser["friendRank"];

    getRewardIcons();
});

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
            document.getElementById("rewardIcon1").src = "data:image/png;base64," + object["rewardImage"];
        } else if (object["level"] === 2) {
            document.getElementById("rewardIcon2").src = "data:image/png;base64," + object["rewardImage"];
        } else if (object["level"] === 3) {
            document.getElementById("rewardIcon3").src = "data:image/png;base64," + object["rewardImage"];
        }
    });
}