let userList = JSON.parse(sessionStorage.getItem("userlist"));
let table = document.getElementById('friendshipTable').getElementsByTagName('tbody')[0];
let onlyFriendsVisible = false;
let searchResults = "";
let row;

/** Liste wird beim Öffnen der Seite in die Tabelle geladen **/
$(document).ready(function () {
    fillTable();
});

/** Zentrale Steurung für den Inhalt der Tabelle. Wird jedes mal aufgerufen wenn sich etwas verändert (Freundschaft, Filterung, Suche...) **/
function fillTable() {
    table.innerHTML = '';
    let fillWith = userList;

    if (onlyFriendsVisible) {
        fillWith = getFriends();

        if (searchResults !== "") {
            fillWith = searchResults;
        }
    }

    if (searchResults !== "") {
        fillWith = searchResults;
    }

    fillWith.forEach(jObj => {
        row = table.insertRow(-1);
        setUserData(jObj);
        setFriendshipStatus(jObj);
        setButtonCell(jObj);
        setOpenProfileButtons(jObj);
    });

    //legt für alle "Hinzufügen" und "Entfernen" Buttons Funktion fest, die die Freundschaft entsprechend mit der DB abgleicht
    $(".updateFriendBtn").click(function () {
        let i = this.getAttribute("id").split("_");
        if (this.innerText === "Entfernen") {
            let a = confirm("Bist du dir sicher, dass du diese Freundschaft beenden willst?");
            if (a) {
                updateFriendship(parseInt(i[1]));
            }
        } else {
            updateFriendship(parseInt(i[1]));
        }
    });

    //Funktion die fuer alle "OpenProfileIcons" Funktion festlegt, die die Seite eines Freundes oeffnet
    $(".openProfileBtn").click(function () {
        let thisFriendID = (this.getAttribute("id").split("_"))[1];
        userList.forEach(jObj => {
            if (jObj["userID"] === thisFriendID) {
                sessionStorage.setItem("selectedFriendID", thisFriendID);
                window.location.href = "/friendProfile";
            }
        });
    });
}

function setUserData(jObj) {
    let idCell = row.insertCell();
    let imgCell = row.insertCell();
    let nameCell = row.insertCell();

    idCell.innerText = jObj["userID"];
    nameCell.innerHTML = jObj["username"];

    let profilePic = document.createElement("img");
    profilePic.src = "data:image/png;base64," + jObj["friendPic"];

    imgCell.appendChild(profilePic);
}

function setFriendshipStatus(jObj) {
    let statusCell = row.insertCell();

    if (jObj["isFriend"]) {
        let friends = document.createElement("h6");
        friends.innerText = "Freunde";
        statusCell.appendChild(friends);
        statusCell.appendChild(createDateObj(jObj["friendSince"]));

        row.setAttribute("style", "background-color: #d9f7f7");
    } else {
        statusCell.innerText = "Unbekannt";
    }
}

function createDateObj(date) {
    let dateSpan = document.createElement("h7");

    if (date === undefined) {
        const dateNow = new Date();
        const dd = String(dateNow.getDate()).padStart(2, '0');
        const mm = String(dateNow.getMonth() + 1).padStart(2, '0');
        const yyyy = String(dateNow.getFullYear());
        date = dd + "." + mm + "." + yyyy;
    }
    dateSpan.innerText = "seit " + date;
    return dateSpan;
}

function setButtonCell(jObj) {
    let updateCell = row.insertCell();
    let b = document.createElement("button");
    b.setAttribute("id", "updateFriend_" + jObj["userID"]);
    b.classList.add("updateFriendBtn");

    if (jObj["isFriend"]) {
        b.classList.add("secondary_btn");
        b.innerText = "Entfernen";
    } else {
        b.classList.add("primary_btn");
        b.innerText = "Hinzufügen";
    }
    b.classList.add("basic_btn");
    updateCell.appendChild(b);
}

function setOpenProfileButtons(jObj) {
    let openPageCell = row.insertCell();

    let openPageIcon = document.createElement("img");
    openPageIcon.classList.add("openProfileBtn");
    openPageIcon.setAttribute("id", "friendID_" + jObj["userID"]);

    if (jObj["isFriend"]) {
        $(openPageIcon).attr("src", "/assets/images/userProfile.png");

        openPageCell.appendChild(openPageIcon);
    } else {
        openPageCell.innerText = "Du kannst nur die Profile deiner Freunde besuchen!";
        openPageCell.setAttribute("style", "font-size: 11px; color: indianred; font-style: italic;");
    }
}

/** setzt die userlist local so, dass neue und entfernte Freundschaften unter "isFriend" als boolean neu gespeichert werden
 * Danach wird POST für /friends aufgerufen, wodurch die Veränderungen an die DB weitergegeben werden **/
function updateFriendship(id) {
    userList.forEach(jObj => {
        if (id === parseInt(jObj["userID"])) {
            jObj["isFriend"] = (!jObj["isFriend"]);
            fillTable();
        }
    });

    $.post("/friends", {friendIDToUpdate: id})
        .done(function (data, status) {
            if (status === "success") {
                sessionStorage.setItem("userlist", data);
                userList = JSON.parse(sessionStorage.getItem("userlist"));
            }
        })
        .fail(function () {
            window.alert("An unexpected Error occurred. Please check your connection.")
        });

    fillTable();
}

/** Gibt die Liste aller aktuellen Freunde zurück **/
function getFriends() {
    let list = [];
    userList.forEach(jObj => {
        if (jObj["isFriend"]) {
            list.push(jObj);
        }
    });
    return list;
}

/** Wird ausgelösst, sobald ein Element im Dropdown verändert wird (Filterung nach alle User oder Meine Freunde) **/
function changeTable() {
    let filterBox = document.getElementById("user-filter");
    onlyFriendsVisible = filterBox.selectedIndex === 1; //index 1 bedeutet "Meine Freunde" ist ausgewählt
    searchUsername();
}

//wird jedes mal aufgerufen, wenn etwas in die Suchleiste eingegeben wird und lädt die entsprechenden Ergebnisse in die Tabelle
function searchUsername() {
    let currVisibleList = userList;
    if (onlyFriendsVisible) currVisibleList = getFriends();

    let input = document.getElementById("searchFriends").value.toLowerCase();
    let foundFriends = [];
    currVisibleList.forEach((jObj) => {
        let name = jObj["username"].toLowerCase();
        if (name.includes(input)) {
            foundFriends.push(jObj);
        }
    });

    searchResults = foundFriends;

    //setzt die Ergebnisse zurück wenn Suchleiste gelöscht wird
    if (input === "") {
        searchResults = "";
        fillTable();
    }
    fillTable();
}