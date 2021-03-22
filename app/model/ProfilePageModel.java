package model;

import org.json.JSONException;
import org.json.JSONObject;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Http;
import play.db.Database;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;


public class ProfilePageModel {

    private final FormFactory formFactory;

    @Inject
    public ProfilePageModel(FormFactory formFactory, Database db) {
        this.formFactory = formFactory;
        this.db = db;
    }

    @Inject
    Database db;

    /**
     * The database values of the user name and picture id are updated (if necessary)
     *
     * @param request     Stores session values (old username, new picture id)
     * @param newUsername New name of user
     * @return "true" if changes were successful
     */
    public boolean changeUserDataInDB(Http.Request request, String newUsername) {
        DynamicForm req = formFactory.form().bindFromRequest(request);
        UserModel userModel = new UserModel(db);
        int newPicID = Integer.parseInt(req.get("newPic"));
        String oldName = req.get("oldName");
        if (!userModel.usernameTaken(newUsername) || oldName.equals(newUsername)) {
            db.withConnection(con -> {
                PreparedStatement stmt = con.prepareStatement("UPDATE User SET idProfilePicture = ?, username = ? WHERE username = ?;");
                stmt.setInt(1, newPicID);
                stmt.setString(2, newUsername);
                stmt.setString(3, oldName);
                stmt.executeUpdate();

                stmt.close();
                con.close();
            });
            return true;
        }
        return false;
    }

    /**
     * Stores the level ID and reward image of all completed levels in a JSON array.
     *
     * @param jsonArray     Empty JSON array (ArrayList)
     * @param unlockedLevel Number of levels that user completed fully
     * @return given JSON array but filled with objects
     */
    public ArrayList<JSONObject> rewardIconFiller(ArrayList<JSONObject> jsonArray, int unlockedLevel) {
        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM Level");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int level = rs.getInt("idLevel");
                if (level <= unlockedLevel) {
                    JSONObject obj = new JSONObject();
                    byte[] rewardIcons = rs.getBytes("rewardIcon");
                    try {
                        obj.put("rewardImage", DatatypeConverter.printBase64Binary(rewardIcons));
                        obj.put("level", level);

                        jsonArray.add(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return jsonArray;
    }

    /**
     * Generates a JSON array of all profile pictures, where the objects store the profile picture's ID, image source
     * and a boolean (is this picture the current profile picture?).
     *
     * @param currentPicID A profile picture ID, that is currently used
     * @return JSON array as String
     */
    public String getPictures(int currentPicID) {
        ArrayList<JSONObject> userJsonArray = new ArrayList<>();

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM ProfilePictures;");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject obj = new JSONObject();

                String id = String.valueOf(rs.getInt("idProfilePicture"));
                byte[] pictureBytes = rs.getBytes("profilePictureImg");

                try {
                    obj.put("id", id);
                    obj.put("profilePic", DatatypeConverter.printBase64Binary(pictureBytes));

                    //gibt an ob jeweiliges Profilbild das Profilbild des Nutzers ist
                    obj.put("isCurrPic", Integer.parseInt(id) == currentPicID);
                    userJsonArray.add(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return userJsonArray.toString();
    }

    /**
     * Searches for user in database and returns the profile picture ID they are currently using.
     *
     * @param username Name of logged in user (String)
     * @return current profile picture ID of user
     */
    public int getCurrentPictureID(String username) {
        final int[] picID = {1};
        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM User WHERE username = ?;");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                picID[0] = rs.getInt("idProfilePicture");
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return picID[0];
    }
}
