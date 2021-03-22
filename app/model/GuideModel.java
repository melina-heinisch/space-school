package model;

import org.json.JSONException;
import org.json.JSONObject;
import play.db.Database;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.sql.*;
import java.util.ArrayList;

public class GuideModel {

    private final Database db;

    @Inject
    public GuideModel(Database db) {
        this.db = db;
    }

    /**
     * The complete puzzled image (source string) and a short info text are stored in a JSON object for each level.
     *
     * @return String (JSON array)
     */
    public String getImagesAndText() {
        ArrayList<JSONObject> jsonArray = new ArrayList<>();

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM Level");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject obj = new JSONObject();

                byte[] img = rs.getBytes("completePuzzleImg");
                String info = rs.getString("infoTextGuide");
                try {
                    obj.put("image", DatatypeConverter.printBase64Binary(img));
                    obj.put("info", info);
                    obj.put("id", rs.getInt("idLevel"));
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
