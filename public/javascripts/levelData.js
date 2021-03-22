const currLvlString = sessionStorage.getItem("currentlevel");
const currentLevel = parseInt(currLvlString);
let jArr = JSON.parse(sessionStorage.getItem("data"));


// database-ids and name-ids are hardcoded in a map
const level1idNames = new Map();
level1idNames.set("11", "antenna").set("12", "command_datahandling").set("13", "guidance_control").set("14", "housing")
    .set("15", "power").set("16", "thermal_control").set("17", "transponder");

const level2idNames = new Map();
level2idNames.set("21", "helmet_inside").set("22", "helmet").set("23", "torso").set("24", "arm")
    .set("25", "glove").set("26", "pants").set("27", "shoes").set("28", "backpack");

const level3idNames = new Map();
level3idNames.set("31", "launchAbortSystem").set("32", "payloadFairing").set("33", "crewCargoModule").set("34", "guidanceSystem")
    .set("35", "serviceModule").set("36", "liquidFuelOxygen").set("37", "structuralSystem")
    .set("38", "rocketEngine").set("39", "nozzle");

let nameInfo = new Map();
let imgName = new Map();
let bubbleName = new Map();

// update the maps depending on the current level
if (currentLevel === 1) {
    nameInfo = createNameInfo(level1idNames);
    imgName = createImgName(level1idNames, "image");
    bubbleName = createImgName(level1idNames, "bubble");
} else if (currentLevel === 2) {
    nameInfo = createNameInfo(level2idNames);
    imgName = createImgName(level2idNames, "image");
    bubbleName = createImgName(level2idNames, "bubble");
} else if (currentLevel === 3) {
    nameInfo = createNameInfo(level3idNames);
    imgName = createImgName(level3idNames, "image");
    bubbleName = createImgName(level3idNames, "bubble");
}

// creates a map in which the keys = name-ids, values = small info texts (for the collecting game)
function createNameInfo(idNames) {
    let returnMap = new Map();
    idNames.forEach((nameId, numberId) => {
        jArr.forEach((jObj) => {
            if (jObj.id.toString() === numberId) { // compares the id from database to number-id in the idNames-map
                returnMap.set(nameId, jObj.info);
            }
        })
    })
    return returnMap;
}

/*
 creates a map in which the keys = src-Strings für the images, values = name-Ids
 can be used for creating the parts-image-map or info-bubble-map
 */
function createImgName(idNames, partsImagesOrBubbles) {
    let returnMap = new Map();
    idNames.forEach((nameId, numberId) => {
        jArr.forEach((jObj) => {
            if (jObj.id.toString() === numberId) {
                let newImage = document.createElement('img');
                if (partsImagesOrBubbles === "image") {
                    newImage.src = "data:image/png;base64," + jObj.pImage; //for converting from base64 to png image
                } else if (partsImagesOrBubbles === "bubble") {
                    newImage.src = "data:image/png;base64," + jObj.bubble;
                }
                returnMap.set(newImage.src, nameId);
            }
        })
    })
    return returnMap;
}

class LevelData {
    constructor(level) {
        this.currentlevel = level;
        this.typeMap = imgName;
        this.infoMap = nameInfo;
        this.bubbleMap = bubbleName;
    }

    getImageArray() {
        return Array.from(this.typeMap.keys());
    }

    setFullOpacity(image) {
        let element = selectID(this.typeMap.get(image));
        $(element).css({opacity: 1});
    }

    resetPuzzlePieces() {
        function reset() {
            return function (value) {
                $(selectID(value)).css({opacity: .4});
            };
        }
        this.typeMap.forEach(reset());
    }

    getID(image) {
        return this.typeMap.get(image);
    }
}