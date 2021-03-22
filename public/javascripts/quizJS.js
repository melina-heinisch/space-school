window.onload = function () {
    getQuestions();
    quiz_div.classList.add("quiz_div-fadeIn")
};

let currentlevel = sessionStorage.getItem("currentlevel");

const quiz_div = document.getElementById("quiz_div");
const gameovermenu = document.getElementById("gameovermenu");
const gamewonmenu = document.getElementById("gamewonmenu");
const totalPointsSpan = document.getElementById("totalPointsSpan");
const endPointsSpan = document.getElementById("endPointsSpan");

let answerIDs = ["answer1", "answer2", "answer3", "answer4"];
let questions = [];
let rightAnswers = 0;
let currentQuestion;
let questionLength;

function getQuestions() {
    $.post("/quiz", {currentlevel: currentlevel})
        .done(function (data, status) {
            fillQuestions(data);
        });
}

function fillQuestions(questionsString) {
    let questionArray = JSON.parse(questionsString);

    questionArray.forEach((object) => {
        let currentQuestion = new Map();
        currentQuestion.set("question", object.question);
        currentQuestion.set("rightAnswer", object.rightAnswer);
        currentQuestion.set("wrongAnswer1", object.wrongAnswer1);
        currentQuestion.set("wrongAnswer2", object.wrongAnswer2);
        currentQuestion.set("wrongAnswer3", object.wrongAnswer3);

        questions.push(currentQuestion);
    });
    questionLength = questions.length;

    reloadQuestions();
}

function reloadQuestions() {
    if (questions.length === 0) {
        gameover();
    } else {
        let currentId = getRandomInt(0, questions.length - 1);

        currentQuestion = questions[currentId];

        answerIDs = shuffle(answerIDs);

        document.getElementById("question").innerText = currentQuestion.get("question");
        document.getElementById(answerIDs[0]).innerText = currentQuestion.get("rightAnswer");
        document.getElementById(answerIDs[1]).innerText = currentQuestion.get("wrongAnswer1");
        document.getElementById(answerIDs[2]).innerText = currentQuestion.get("wrongAnswer2");
        document.getElementById(answerIDs[3]).innerText = currentQuestion.get("wrongAnswer3");

        questions.splice(currentId, 1)
    }
}

function gameover() {
    if (rightAnswers === questionLength) {
        quiz_div.classList.remove("quiz_div-fadeIn");
        gamewonmenu.classList.add("gamewonmenu-fadeIn");
        document.getElementById("lvlBackLink").classList.add("hidden");
        setUserProgress();
    } else {
        endPointsSpan.innerText = String(rightAnswers);
        totalPointsSpan.innerText = String(questionLength);
        quiz_div.classList.remove("quiz_div-fadeIn");
        gameovermenu.classList.add("gameovermenu-fadeIn");
        document.getElementById("lvlBackLink").classList.add("hidden");
    }
}

function checkAnswer(answer) {
    if (document.getElementById(answer).value === currentQuestion.get("rightAnswer")) {
        showRightAnswerModal();
        rightAnswers++;
    } else {
        showWrongAnswerModal();
    }
}

// floor rundet auf nächst kleinere Ganzzahl ab, ceil rundet auf
function getRandomInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function showRightAnswerModal() {
    $("#rightAnswer").modal("show");
    $('#rightAnswer').delay(1200).fadeOut(450);
    setTimeout(function () {
        $('#rightAnswer').modal("hide");
        reloadQuestions();
    }, 1650);
}

function showWrongAnswerModal() {
    $("#wrongAnswer .modal-body").text(' Das war leider falsch. Die richtige Antwort ist "' + currentQuestion.get("rightAnswer") + '".');
    $("#wrongAnswer").modal("show");
}

//Fisher-Yates (aka Knuth) Shuffle
//https://bost.ocks.org/mike/shuffle/
function shuffle(array) {
    let currentIndex = array.length;
    let randomIndex = array.length;
    let temporaryValue = array.length;

    // While there remain elements to shuffle...
    while (0 !== currentIndex) {

        // Pick a remaining element...
        randomIndex = Math.floor(Math.random() * currentIndex);
        currentIndex -= 1;

        // And swap it with the current element.
        temporaryValue = array[currentIndex];
        array[currentIndex] = array[randomIndex];
        array[randomIndex] = temporaryValue;
    }
    return array;
}

function openNextLevel() {
    if (currentlevel === "1") {
        sessionStorage.setItem("currentlevel", "2");

        $.post("/mainGame", {currentlevel: 2})
            .done(function (data, status) {
                sessionStorage.setItem("data", data);
                if (status === "success")
                    location.href = "/mainGame";
            });
    } else if (currentlevel === "2") {
        sessionStorage.setItem("currentlevel", "3");

        $.post("/mainGame", {currentlevel: 3})
            .done(function (data, status) {
                sessionStorage.setItem("data", data);
                if (status === "success")
                    location.href = "/mainGame";
            });
    } else {
        alert("This level is not implemented yet!");
    }
}

function setUserProgress() {
    $.post("/quizProgress", {gameLevel: currentlevel})
        .done(function (data, status) {
            if (data === "true")
                $("#handbuchHint").modal("show");
        });
}