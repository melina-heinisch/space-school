/* ---------------------------- Canvas ------------------------------- */

function selectID(elem) {
    return document.getElementById(elem);
}

//Canvas ist die "Fläche" zum zeichnen von Objekten, CanvasContext das "Werkzeug" um darauf zu malen
const canvas = selectID("myCanvas");
const canvasContext = canvas.getContext("2d");
const startGameBtn = selectID("startGameButton");
const mainmenu = selectID("mainmenu");
const gameovermenu = selectID("gameovermenu");
const gamewonmenu = selectID("gamewonmenu");
const ingame = selectID("ingame");
const sideBarDiv = selectID("puzzle_list");
const pointsSpan = selectID("pointsSpan");

const endPointsSpan = selectID("endPointsSpan");
const textPuzzle = selectID("textPuzzle");
const crashSound = new Sound('/assets/sounds/hitSound.wav');
const collectSound = new Sound('/assets/sounds/collectSound.wav');

const restartBtn = selectID("restartBtn");
const levelBtn = selectID("levelBtn");
const levelBtnWon = selectID("levelBtnWon");

const puzzlelist = new LevelData(currentLevel);
let images = puzzlelist.getImageArray();
let numberOfPieces = images.length;
let win = false;
selectID("currLvl").innerText = currentLevel;

/* ---------------------------- Sound effects ------------------------------- */
function Sound(src) {
    this.sound = document.createElement("audio");
    this.sound.src = src;
    this.sound.setAttribute("preload", "auto");
    this.sound.setAttribute("controls", "none");
    this.sound.style.display = "none";
    document.body.appendChild(this.sound);
    this.play = function () {
        this.sound.play();
    };
    this.stop = function () {
        this.sound.pause();
    };
}

/* ---------------------------- Vollbild etc. ------------------------------- */
/**
 * blockiert Rechtsklick (Context menu)
 */

 document.oncontextmenu = function () {
    return false;
};

/**
 * passt Canvas der aktuellen Fenstergröße an und berechnet alle möglichen Startpositionen am Bildschirmrand für Hindernisse
 */
function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    game.createStartingPoints();
}

/* ---------------------------- Sidebar-Setup ------------------------------- */

for (let i = 0; i < numberOfPieces; i++) {
    let container = document.createElement("div");
    let im = document.createElement("img");
    $(im).attr("src", images[i]).attr("class", "puzzle_piece").attr("id", puzzlelist.getID(images[i]));

    if (numberOfPieces > 10) {
        $(container).css("height", "5rem").css("width", "5rem");
        $(im).css("width", "5rem");
    }

    let imHeight = im.height;
    let imWidth = im.width;
    console.log(imHeight + " " + imWidth);

    if (imWidth > imHeight) {
        $(im).css({
            'max-width': "100%",
            'height': 'auto'
        });
    }

    if (imHeight > imWidth) {
        $(im).css({
            'max-height': "100%",
            'width': 'auto'
        });
    }

    container.appendChild(im);
    sideBarDiv.appendChild(container);
}

/* ---------------------------- mathematische Formeln ------------------------------- */

//Ganzzahlen
function getRandomInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

//Kommazahlen
function getRandomArbitrary(min, max) {
    return Math.random() * (max - min) + min;
}

//Winkel zwischen zwei Punkten
function winkelBerechnen(p1, p2) {
    return Math.atan2(p2.y - p1.y, p2.x - p1.x);
}

//Abstand zweier Punkte mittels Hypotenuse berechnen
function abstandBerechnen(p1, p2) {
    return Math.hypot(p2.x - p1.x, p2.y - p1.y);
}

/* ---------------------------- Klassen ------------------------------- */

//region Position and Size
class Position {
    constructor(xPos, yPos) {
        this.x = xPos;
        this.y = yPos;
    }
}

class Size {
    constructor(w, h) {
        this.width = w;
        this.height = h;
    }

    sizeSetWidth(w) {
        this.width = w;
    }

    sizeSetHeight(h) {
        this.height = h;
    }
}

/* ---------------------------- Maus ------------------------------- */

let mousePosition = new Position(0, 0);
let mousePressedDown = false;

//Wird bei jeder Mausbewegung aufgerufen
function setMousePosition(event) {
    mousePosition.x = event.clientX;
    mousePosition.y = event.clientY;
}

function mouseDown(event) {
    mousePressedDown = true;
    mousePosition.x = event.clientX;
    mousePosition.y = event.clientY;
}

function mouseUp() {
    mousePressedDown = false;
}

/* ---------------------------- Game ------------------------------- */

class Game {
    constructor() {
        this.ball = null; //Rakete;
        this.obstacles = [];
        this.startingPoints = []; // Startpunkte von Obstacles und Puzzelteilen
        this.lives = 3;
        this.collectedImages = new Set;
        this.obstacleTime = new obstacleTime();
        this.canCreateObstacles = false;
        this.running = false; //um requestAnimationFrame zu stoppen (+ cancel...)
        this.buttonFree = false; //mehrmaliges Drücken eines Buttons wird verhindert
        this.points = new Points();
        this.reset = false;
    }

    load() {
        resizeCanvas();
    }

    start(ort) {
        if (this.running === false) {
            this.buttonFree = true; //neuStart Visibility-"Bug" beim Laden
            this.points.points = 0;
            this.running = true;
            images = puzzlelist.getImageArray();
            win = false;
            this.ball = new Ball(0.4, 0.25);

            if (ort === "mainM" || this.reset) {
                this.reset = false;
            }
            this.canCreateObstacles = true;


            this.loop();
            mainmenu.classList.add("mainmenu-fadeOut");
            ingame.classList.add("ingame-fadeIn");
        }
    }


    die() { //wird über Hinderniss Kollision aufgerufen
        selectID("live3").setAttribute("style", "opacity: 1.0");
        selectID("live2").setAttribute("style", "opacity: 1.0");
        selectID("live1").setAttribute("style", "opacity: 1.0");
        puzzlelist.resetPuzzlePieces();
        game.collectedImages = new Set;
        game.lives = 3;
        pointsSpan.innerText = "0";

        this.gameOver();
    }

    collectPuzzlePiece() {
        this.points.countPoints();
    }

    gameOver() {
        this.running = false;
        cancelAnimationFrame(this.frame); //AnimationFrame aktualisiert sich nicht mehr fortlaufend
        canvasContext.clearRect(0, 0, canvas.width, canvas.height);
        this.obstacles.splice(0, this.obstacles.length);
        this.canCreateObstacles = false;
        this.obstacleTime = new obstacleTime();
        this.buttonFree = true;

        if (win) {
            textPuzzle.classList.add("textPuzzle-fadeIn");
        }

        endPointsSpan.innerText = String(Math.round(this.points.points));
        ingame.classList.remove("ingame-fadeIn");

        if (win) {
            gamewonmenu.classList.add("gamewonmenu-fadeIn");
        } else {
            gameovermenu.classList.add("gameovermenu-fadeIn");
        }
    }

    restart() {
        if (this.buttonFree) {
            this.buttonFree = false;
            this.start("overM");
            this.classAddRemove();
        }
    }

    back() {
        if (this.buttonFree) {
            this.buttonFree = false;
            this.classAddRemove();
            mainmenu.classList.remove("hauptmenu-fadeOut");
        }
    }

    classAddRemove() {
        if (win) {
            gamewonmenu.classList.remove("gamewonmenu-fadeIn");
            textPuzzle.classList.remove("textPuzzle-fadeIn");
            textPuzzle.classList.remove("textPuzzle-bottom")
        } else {
            gameovermenu.classList.remove("gameovermenu-fadeIn");
        }
    }

    /* Legt alle möglichen Positionen am Rand des Spielfelds fest, an denen Hindernisse erzeugt werden können */
    createStartingPoints() {
        this.startingPoints.splice(0, this.startingPoints.length);
        let a = 100; //sorgt dafür dass Objekte sicher außerhalb des Fensters erzeugt werden
        for (let i = 0; i < canvas.width; i++) {
            this.startingPoints.push(new Position(i, 0 - a));
            this.startingPoints.push(new Position(i, canvas.height + 0.5 * a));
        }
        for (let i = 0; i < canvas.height; i++) {
            this.startingPoints.push(new Position(0 - a, i));
            this.startingPoints.push(new Position(canvas.width + 0.5 * a, i));
        }
    }

    createObstacles() { //wird über obstacleTime getimet
        this.obstacles.push(new Obstacles());
    }

    loop() {
        canvasContext.clearRect(0, 0, canvas.width, canvas.height);

        //Update für Position der Rakete
        this.ball.moveBall();
        this.ball.collisionRand();
        this.ball.drawBall();

        //Updated Position der Hindernisse & Puzzleteile
        for (let i = 0; i < this.obstacles.length; i++) {
            this.obstacles[i].moveObstacle();
            this.obstacles[i].draw();
            this.obstacles[i].counterForDeleting();
            this.obstacles[i].collisionDetection();

            //löscht Obstacle wenn es außerhalb des Bildschirms ist
            if (this.obstacles[i] && this.obstacles[i].deleteObstacle) {
                this.obstacles.splice(i, 1);
                i -= 1; //da es in der Schleife noch drin ist
            }
        }

        if (this.canCreateObstacles) {
            this.obstacleTime.countObstacles();
        }

        // Solange das spiel läuft, wird der Loop immer wieder ausgeführt
        if (this.running) {
            this.frame = requestAnimationFrame(() => {
                this.loop()
            });
        }
    }
}

//endregion
/* ---------------------------- Punkte ------------------------------- */

//region Punkte
class Points {

    constructor() {
        this.points = 0;
    }

    countPoints() {
        this.points += 1;
        this.renderPoints();
    }

    renderPoints() {
        pointsSpan.innerText = this.points; //innerText oder textContent (für reinen Text) - innerHTML .. brauche ich hier nicht
    }
}

//endregion

//region Hinderniss & Zeit
/* ---------------------------- ObstacleTime ------------------------------- */

class obstacleTime {
    constructor() {
        this.timer = 0;
        this.timeBetween = 60;
        this.currentValue = 0;
    }

    //Nach 60 Aufrufen der Methode wird Abstand in dem Objekte erscheinen verkürzt
    countObstacles() {
        this.timer += 1;
        if (this.timer >= 60) {
            this.timer = 0;
            //hier kommt rein was einmal pro sekunde aufgerufen werden soll
            this.shortenTimeBetween();
        }

        this.currentValue += 1; // geht immer wieder hoch zu timeBetween

        //Erstellt nach "Ablauf" der Zeit zwischen den Objekten ein neues
        if (this.currentValue >= this.timeBetween) {
            this.currentValue = 0;
            game.createObstacles();
        }
    }

    shortenTimeBetween() { //
        if (this.timeBetween > 7) {
            this.timeBetween -= 0.6;
        }
    }
}

/* ---------------------------- Hindernisse ------------------------------- */

class Obstacles {
    constructor() {
        let randomPosition = this.randomPosition();
        this.position = new Position(game.startingPoints[randomPosition].x, game.startingPoints[randomPosition].y);
        this.size = this.setObstacleSizeAndType();
        this.speed = getRandomArbitrary(1, 4);
        this.direction = this.setDirection();
        this.counter = 0;
        this.deleteAt = 6000 / this.speed;
        this.deleteObstacle = false;
    }

    counterForDeleting() {
        this.counter += 1;
        if (this.counter > this.deleteAt) {
            this.deleteObstacle = true;
        }
    }

    collisionDetection() { //Kreis - Rechteck
        let ballPosition = new Position(game.ball.position.x, game.ball.position.y);

        if (game.ball.position.x < this.position.x) {
            ballPosition.x = this.position.x;
        } else if (game.ball.position.x > this.position.x + this.size.width) {
            ballPosition.x = this.position.x + this.size.width;
        }
        if (game.ball.position.y < this.position.y) {
            ballPosition.y = this.position.y;
        } else if (game.ball.position.y > this.position.y + this.size.height) {
            ballPosition.y = this.position.y + this.size.height;
        }

        let distance = abstandBerechnen(game.ball.position, ballPosition);

        if (distance <= game.ball.xSizeHalf || distance <= game.ball.ySizeHalf) {
            if (this.type === "deadly") {
                crashSound.stop();
                crashSound.play();
                game.lives -= 1;
                this.deleteObstacle = true;
                if (game.lives === 2) {
                    selectID("live3").setAttribute("style", "opacity: 0.0");
                } else if (game.lives === 1) {
                    selectID("live2").setAttribute("style", "opacity: 0.0");
                } else if (game.lives === 0) {
                    selectID("live1").setAttribute("style", "opacity: 0.0");
                    game.die();
                }
            } else if (this.type === "rocketPart") {
                if (!game.collectedImages.has(this.currentPiece)) {
                    $('.info').text(puzzlelist.infoMap.get(this.pieceType)).fadeIn(400).delay(3000).fadeOut(400);
                    collectSound.stop();
                    collectSound.play();
                    this.deleteObstacle = true;
                    game.collectPuzzlePiece();
                    puzzlelist.setFullOpacity(this.image);
                    game.collectedImages.add(this.currentPiece);
                    const imageIndex = images.indexOf(this.currentPiece);
                    if (imageIndex > -1) images[imageIndex] = "";
                }

                if (game.collectedImages.size === numberOfPieces) {
                    win = true;
                    setUserProgress();
                    game.die();
                }
            }
        }
    }

    randomPosition() {
        return getRandomInt(0, game.startingPoints.length - 1);
    }

    setObstacleSizeAndType() {
        let randomInt = getRandomInt(1, 5);
        this.currentPiece = images[getRandomInt(0, numberOfPieces - 1)];

        while (this.currentPiece === "")
            this.currentPiece = images[getRandomInt(0, numberOfPieces - 1)];

        if (randomInt === 1) {
            this.image = this.currentPiece;
            this.pieceType = puzzlelist.typeMap.get(this.currentPiece);
            this.type = "rocketPart";
            let size1 = getRandomInt(60, 100);
            return new Size(size1, size1);
        } else {
            this.image = "/assets/images/asteroid.png";
            this.type = "deadly";
            let size2 = getRandomInt(55, 95);
            return new Size(size2, size2);
        }
    }

    setDirection() {
        let lowestNumber = 50;
        let x = getRandomInt(lowestNumber, canvas.width - lowestNumber);
        let y = getRandomInt(lowestNumber, canvas.height - lowestNumber);
        let position2 = new Position(x, y);
        return winkelBerechnen(this.position, position2);
    }

    moveObstacle() {
        this.position.x += this.speed * Math.cos(this.direction);
        this.position.y += this.speed * Math.sin(this.direction);
    }

    draw() {
        let floatingParts = new Image();
        floatingParts.src = this.image;
        let currentWidth = this.size.width;
        let currentHeight = this.size.height;

        if (this.type === "rocketPart") {
            if (floatingParts.width > floatingParts.height) {
                let factorH = currentWidth / floatingParts.width;
                currentHeight = floatingParts.height * factorH;
            } else {
                let factorW = currentHeight / floatingParts.height;
                currentWidth = floatingParts.width * factorW;
            }
        }

        canvasContext.beginPath();
        canvasContext.drawImage(floatingParts, this.position.x, this.position.y, currentWidth, currentHeight);
        canvasContext.closePath();
    }
}

//endregion
/* ---------------------------- Spielball ------------------------------- */

class Ball {
    constructor(x, y) {
        this.position = new Position(x * canvas.width, y * canvas.height);
        this.xSizeHalf = 30;
        this.ySizeHalf = 50;
        this.speed = 6;
        this.angle = -0.2 * Math.PI;
        this.currentSpeed = this.speed;

        this.collision = false; //ab hier Abstoß untereinander
        this.angleH = 0;
        this.sHilfs = 25;
        this.sHilfs2 = this.sHilfs;
        this.abstossHilfs = 6;
        this.abstoss = this.abstossHilfs;
    }

    moveBall() {
        this.currentSpeed = this.speed;
        let distance = abstandBerechnen(this.position, mousePosition);
        this.angle = winkelBerechnen(this.position, mousePosition);

        let slowDown = 100;
        let factor = slowDown / this.speed;
        if (distance <= slowDown) {
            this.currentSpeed = distance / factor;
        }

        if (this.collision) {
            this.position.x -= this.abstoss * Math.cos(this.angleH);
            this.position.y -= this.abstoss * Math.sin(this.angleH);
        }

        if (this.collision && this.abstoss <= this.currentSpeed) {
            let g = this.currentSpeed - this.abstoss;
            this.position.x += g * Math.cos(this.angle);
            this.position.y += g * Math.sin(this.angle);
        }

        if (this.collision === false) { //normal
            this.position.x += this.currentSpeed * Math.cos(this.angle);
            this.position.y += this.currentSpeed * Math.sin(this.angle);
        }
    }

    collisionRand() {
        let sidebar = selectID("sidebar-wrapper");
        let style = getComputedStyle(sidebar);
        let sidebarWidth = parseInt(style.width) + 2;

        //stellt sicher dass cursor nicht unter der sidebar verschwindet
        if (this.position.x >= canvas.width - sidebarWidth) {
            this.position.x = canvas.width - sidebarWidth;
        }

        //stellt sicher, dass rakete nicht am unteren Bildschirmrand verschwindet
        if (this.position.y + this.ySizeHalf >= canvas.height) {
            this.position.y = canvas.height - this.ySizeHalf;
        }
    }

    drawBall() {
        let xPos = this.position.x * 0.94;
        let yPos = this.position.y * 0.94;
        let imgRocket = new Image();
        imgRocket.src = "/assets/images/rocket.png";
        canvasContext.beginPath();
        canvasContext.drawImage(imgRocket, xPos, yPos, 63, 100);
        canvasContext.closePath();
    }
}

/* ---------------------- Weiterleitung zu Puzzle ------------------------- */

function openPuzzle() {
    if (currentLevel === 1) {
        sessionStorage.setItem("currentlevel", 1);

        //Speichern des Levels in Session
        $.post("/mainGame", {currentlevel: 1})
            .done(function (data, status) {
                sessionStorage.setItem("data", data);
                if (status === "success")
                    location.href = "/puzzle";
            });
    } else if (currentLevel === 2) {
        sessionStorage.setItem("currentlevel", 2);

        //Speichern des Levels in Session
        $.post("/mainGame", {currentlevel: 2})
            .done(function (data, status) {
                sessionStorage.setItem("data", data);
                if (status === "success")
                    location.href = "/puzzle";
            });
    } else if (currentLevel === 3) {
        sessionStorage.setItem("currentlevel", 3);

        //Speichern des Levels in Session
        $.post("/mainGame", {currentlevel: 3})
            .done(function (data, status) {
                sessionStorage.setItem("data", data);
                if (status === "success")
                    location.href = "/puzzle";
            });
    } else {
        alert("This level is not implemented yet!");
    }
}

/* ---------------------------- User Progress ------------------------------- */

function setUserProgress() {
    $.post("/collectProgress", {gameLevel: currentLevel});
}


/* ---------------------------- Erstellen ------------------------------- */

let game = new Game();
game.load();

/* ---------------------------- Event Listener ------------------------------- */

window.addEventListener("resize", resizeCanvas, false);

document.addEventListener("mousemove", setMousePosition, false);
document.addEventListener("mousedown", mouseDown, false);
document.addEventListener("mouseup", mouseUp, false);

startGameBtn.addEventListener("mousedown", () => {
    game.start("mainM")
}, false);

levelBtn.addEventListener("mousedown", () => window.location.href = "/levels", false);
levelBtnWon.addEventListener("mousedown", () => window.location.href = "/levels", false);
restartBtn.addEventListener("mousedown", () => {
    game.restart()
}, false);