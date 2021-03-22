package model;

import org.json.JSONException;
import org.json.JSONObject;
import play.db.Database;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class PuzzlePieceModel {

    private final Database db;

    @Inject
    public PuzzlePieceModel(Database db) {
        this.db = db;
    }

    /**
     * Collects all information of a level and stores them as JSON objects in an array
     *
     * @param selectedLevel ID of a level
     * @return JSON array (String)
     */
    public String getLevelData(int selectedLevel) {
        ArrayList<JSONObject> jsonArray = new ArrayList<>();

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM PuzzlePieces WHERE idLevel = ?");
            stmt.setInt(1, selectedLevel); //substitutes first ? with selectedLevel
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject obj = new JSONObject();

                String infoText = rs.getString("infoPuzzlePiece");
                byte[] pieceImgs = rs.getBytes("puzzlePieceImg");
                byte[] pieceBubbles = rs.getBytes("speechBubble");
                try {
                    obj.put("id", rs.getInt("idPuzzlePiece"));
                    obj.put("info", infoText);
                    obj.put("pImage", DatatypeConverter.printBase64Binary(pieceImgs));
                    obj.put("bubble", DatatypeConverter.printBase64Binary(pieceBubbles));
                    jsonArray.add(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return jsonArray.toString();
    }
}
