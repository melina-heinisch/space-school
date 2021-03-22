$(document).ready(function () {
    $.post("/main")
        .done(function (data, status) {
            if (status === "success") {
                sessionStorage.setItem("userlist", data);
                allUsers = JSON.parse(sessionStorage.getItem("userlist"));
            }
        })
        .fail(function () {
            window.alert("An unexpected Error occured. Please chack your connection.")
        });
});

function openFriends() {
    window.location.href = "/friends";
}

(function () {
    let offsetX = 10, offsetY = 10;
    let icon = $("#wrapper").find("a");

    icon.hover(function (e) {
        var title = $(this).attr("dir");
        $('<img src="' + title + '" alt="toolTip" id="tooltip" />').css({
            left: e.pageX + offsetX,
            top: e.pageY + offsetY
        }).appendTo('body');
    }, function () {
        $("#tooltip").remove();
    });

    icon.on("mousemove", function (e) {
        $("#tooltip").css({left: e.pageX + offsetX, top: e.pageY + offsetY});
    });
})();