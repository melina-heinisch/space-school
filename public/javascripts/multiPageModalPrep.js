/** Funktion bereitet ein Modal mit mehreren Seiten vor. Länge hängt von Anzahl der Objekte mit class = "modal-split" ab**/

function prep_modal() {
    $("#back_button").hide();
    $("#next_button").text("Weiter");

    $(".modal").each(function () {
        const allModalPages = $(this).find('.modal-split');

        if (allModalPages.length !== 0) {
            let page_track = 0;

            allModalPages.hide();
            allModalPages.eq(0).show();

            //aktuell angezeigte Seite
            document.getElementById("pageNr").innerText = page_track + 1;

            $("#next_button").click(function () {

                this.blur();

                //sobald die 1.Seite (Index 0) verlassen wird, wird der Back-Button angezeigt
                if (page_track === 0) {
                    $("#back_button").show();
                }

                //sobald die vorletzte Seite verlassen wird, ändert sich der Text des Next-Buttons
                if (page_track === allModalPages.length - 2) {
                    $("#next_button").text("Verstanden!");
                }

                //wenn auf letzer Seite next-Button gedrückt wird dann schließt das Popup und die Popup-Seiten werden zurückgesetzt
                if (page_track === allModalPages.length - 1) {
                    document.getElementById("closeModal").click();
                    page_track = 0;
                    return;
                }

                //solange man nicht auf der letzten Seite wird, erhöht "next" nur den aktuellen Seiten-Index und zeigt die entsprechende Seite
                if (page_track < allModalPages.length - 1) {
                    page_track++;

                    allModalPages.hide();
                    allModalPages.eq(page_track).show();
                }

                document.getElementById("pageNr").innerText = page_track + 1;
            });

            $("#back_button").click(function () {
                //Wechsel auf die 1. Seite (Index 0) sorgt dafür, dass back-button verschwindet
                if (page_track === 1) {
                    $("#back_button").hide();
                }

                //zurück auf der letzten Seite setzt den Next-Button-Text zurück
                if (page_track === allModalPages.length - 1) {
                    $("#next_button").text("Weiter");
                }

                if (page_track > 0) {
                    page_track--;

                    allModalPages.hide();
                    allModalPages.eq(page_track).show();
                }
                document.getElementById("pageNr").innerText = page_track + 1;
            });
        }
    });
}
