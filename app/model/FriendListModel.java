package model;

import org.json.JSONException;
import org.json.JSONObject;
import play.db.Database;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FriendListModel {

    private final Database db;

    @Inject
    public FriendListModel(Database db) {
        this.db = db;
    }

    /**
     * All other users except for the active user are stored in a JSON array. Data like username, ID, profile picture
     * and relationship to the active user are stored.
     *
     * @param username Name of the logged in user
     * @return String of JSON objects
     */
    public String getUserList(String username) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY");

        ArrayList<JSONObject> jsonArray = new ArrayList<>();
        List<Integer> friendIDs = getOldFriends(username);

        db.withConnection(con -> { // collect data of all other users
            String sql = "select User.idUser, username, UserProgress.idLevel, Level.rankNames, profilePictureImg from User join ProfilePictures on User.idProfilePicture = ProfilePictures.idProfilePicture join UserProgress on User.idUser = UserProgress.idUser join Level on UserProgress.idLevel = Level.idLevel where username != ?;";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject obj = new JSONObject();

                String id = String.valueOf(rs.getInt("idUser"));
                String name = rs.getString("username");
                int levelProgress = rs.getInt("idLevel");
                String rank = rs.getString("rankNames");
                byte[] pictureBytes = rs.getBytes("profilePictureImg");

                try {
                    obj.put("userID", id);
                    obj.put("username", name);

                    if (friendIDs.contains(rs.getInt("idUser"))) { // check if other user is friend or not
                        obj.put("isFriend", true);
                        obj.put("friendSince", formatter.format(getFriendDate(Integer.parseInt(id), getMyID(username))));
                    } else
                        obj.put("isFriend", false);

                    obj.put("friendProgress", levelProgress);
                    obj.put("friendRank", rank);
                    obj.put("friendPic", DatatypeConverter.printBase64Binary(pictureBytes));

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

    /**
     * All the ID's of the active user's current friends are stored in a list.
     *
     * @param username Name of logged in user
     * @return List with the IDs if active user's friends
     */
    private List<Integer> getOldFriends(String username) {
        return db.withConnection(con -> {
            int myID = getMyID(username); // active user's ID
            List<Integer> friendIDs = new ArrayList<>();
            String sql = "select idUser1, idUser2 from UserFriendList where idUser1 = ? or idUser2 = ?;";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, myID);
            stmt.setInt(2, myID);
            ResultSet rs = stmt.executeQuery(); // search for all friendships that involve active user

            while (rs.next()) {
                // friendships contain 2 users; if the first user in friendship isn't the active user,
                // the second user in the friendship must be
                int u1 = rs.getInt("idUser1");
                if (u1 != myID)
                    friendIDs.add(u1); //add the ID of active user's friend
                else
                    friendIDs.add(rs.getInt("idUser2"));
            }
            rs.close();
            stmt.close();
            con.close();
            return friendIDs;
        });
    }

    /**
     * @param friendId ID of active user's friend
     * @param myID     ID of active user
     * @return Date, since when the two users are friends
     */
    private LocalDate getFriendDate(int friendId, int myID) {
        AtomicReference<LocalDate> d = new AtomicReference<>(LocalDate.now());

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT friendSince from UserFriendList where (idUser1 = ? and idUser2 = ?) or (idUser1 = ? and idUser2 = ?);");
            stmt.setInt(1, friendId);
            stmt.setInt(2, myID);
            stmt.setInt(3, myID);
            stmt.setInt(4, friendId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Date test = rs.getDate("friendSince");
                if (test != null) //if the two users are friends
                    d.set(rs.getDate("friendSince").toLocalDate());
                else {
                    d.set(LocalDate.now());
                    setCurrentDateInDB(myID, friendId);
                }
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return d.get();
    }

    /**
     * If the other user is currently a friend, they are deleted from the active user's friend list.
     * If they aren't friends yet, the other user is added to the active user's friend list.
     *
     * @param username Name of logged in user
     * @param updateID ID of another user whose status with the current user should be updated
     */
    public void updateMyFriends(String username, String updateID) {
        List<Integer> myFriends = getOldFriends(username);
        int updatedID = Integer.parseInt(updateID);

        db.withConnection(con -> {
            int myID = getMyID(username);

            if (myFriends.contains(updatedID)) { // if the other user is currently a friend, they are deleted from the current user's friend list
                PreparedStatement stmt = con.prepareStatement("DELETE FROM UserFriendList WHERE (idUser1 = ? AND idUser2 = ?) OR (idUser1 = ? AND idUser2 = ?);");
                stmt.setInt(1, myID);
                stmt.setInt(2, updatedID);
                stmt.setInt(3, updatedID);
                stmt.setInt(4, myID);

                stmt.executeUpdate();
                stmt.close();
            } else { // if the other user is no friend yet
                Date date = Date.valueOf(LocalDate.now());
                PreparedStatement stmt2 = con.prepareStatement("INSERT INTO UserFriendList (idUser1, idUser2, friendSince) VALUES (? , ?, ?);");
                stmt2.setInt(1, myID);
                stmt2.setInt(2, updatedID);
                stmt2.setDate(3, date);

                stmt2.executeUpdate();
                stmt2.close();
            }
            con.close();
        });
    }

    /**
     * Search for active user's ID in the database, using their username.
     *
     * @param username Name of logged in user
     * @return ID of active user
     */
    private int getMyID(String username) {
        final int[] myID = new int[1]; //variables in PreparedStatements have to be final
        db.withConnection(con -> {

            PreparedStatement stmt = con.prepareStatement("SELECT idUser FROM User WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                myID[0] = rs.getInt("idUser");
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return myID[0];
    }

    /**
     * Set the date of friendship to local current date.
     *
     * @param myID     ID of logged in user
     * @param friendID ID of active user's friend
     */
    private void setCurrentDateInDB(int myID, int friendID) {
        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("UPDATE UserFriendList SET friendSince = ? WHERE (idUser2 = ? and idUser1 = ?) or (idUser2 = ? and idUser1 = ?);");
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, friendID);
            stmt.setInt(3, myID);
            stmt.setInt(4, myID);
            stmt.setInt(5, friendID);
            stmt.executeUpdate();

            stmt.close();
            con.close();
        });
    }
}
