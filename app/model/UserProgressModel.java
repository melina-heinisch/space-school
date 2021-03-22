package model;

import org.json.JSONException;
import org.json.JSONObject;
import play.db.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UserProgressModel {

    private final Database db;

    private final UserModel userModel;

    public UserProgressModel(Database db) {
        this.db = db;
        userModel = new UserModel(db);
    }

    /**
     * Active user's progress is fetched from the database. For each level there is a JSON object that
     * records if a mini-game was completed successfully yet.
     *
     * @param username Name of active user (String)
     * @return JSON array as String
     */
    public String getProgress(String username) {
        ArrayList<JSONObject> jsonArray = new ArrayList<>();

        UserModel.User user = userModel.getUserByName(username);
        int idUser = user.getId();

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM UserProgress WHERE idUser = ?");
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JSONObject levelOne = new JSONObject();
                JSONObject levelTwo = new JSONObject();
                JSONObject levelThree = new JSONObject();

                // ask DB, which level active user is on and which games are unlocked
                int currentLevel = rs.getInt("idLevel");
                boolean collectingDone = getBool(rs.getInt("collectingDone"));
                boolean puzzleDone = getBool(rs.getInt("puzzleDone"));
                boolean quizDone = getBool(rs.getInt("quizDone"));

                try {
                    if (currentLevel == 3) {
                        levelOne.put("collect", true);
                        levelOne.put("puzzle", true);
                        levelOne.put("quiz", true);
                        levelOne.put("id", "1");

                        levelTwo.put("collect", true);
                        levelTwo.put("puzzle", true);
                        levelTwo.put("quiz", true);
                        levelTwo.put("id", "2");

                        levelThree.put("collect", collectingDone);
                        levelThree.put("puzzle", puzzleDone);
                        levelThree.put("quiz", quizDone);
                        levelThree.put("id", "3");

                    } else if (currentLevel == 2) {
                        levelOne.put("collect", true);
                        levelOne.put("puzzle", true);
                        levelOne.put("quiz", true);
                        levelOne.put("id", "1");

                        levelTwo.put("collect", collectingDone);
                        levelTwo.put("puzzle", puzzleDone);
                        levelTwo.put("quiz", quizDone);
                        levelTwo.put("id", "2");

                        levelThree.put("collect", false);
                        levelThree.put("puzzle", false);
                        levelThree.put("quiz", false);
                        levelThree.put("id", "3");

                    } else if (currentLevel == 1) {
                        levelOne.put("collect", collectingDone);
                        levelOne.put("puzzle", puzzleDone);
                        levelOne.put("quiz", quizDone);
                        levelOne.put("id", "1");

                        levelTwo.put("collect", false);
                        levelTwo.put("puzzle", false);
                        levelTwo.put("quiz", false);
                        levelTwo.put("id", "2");

                        levelThree.put("collect", false);
                        levelThree.put("puzzle", false);
                        levelThree.put("quiz", false);
                        levelThree.put("id", "3");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.add(levelOne);
                jsonArray.add(levelTwo);
                jsonArray.add(levelThree);
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return jsonArray.toString();
    }

    /**
     * The current level that the active user is playing is stored a Json object.
     * The booleans, whether a mini game of that level was completed or not, are stored, too.
     *
     * @param username Name of active user (String)
     * @return JSON array as String
     */
    public String getCurrentProgress(String username) {
        JSONObject object = new JSONObject();

        UserModel.User user = userModel.getUserByName(username);
        int idUser = user.getId();

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM UserProgress WHERE idUser = ?");
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int currentLevel = rs.getInt("idLevel");
                boolean collectingDone = getBool(rs.getInt("collectingDone"));
                boolean puzzleDone = getBool(rs.getInt("puzzleDone"));
                boolean quizDone = getBool(rs.getInt("quizDone"));

                try {
                    object.put("id", currentLevel);
                    object.put("collect", collectingDone);
                    object.put("puzzle", puzzleDone);
                    object.put("quiz", quizDone);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return object.toString();
    }

    /**
     * Generates a map that contains all levels.
     * Boolean for a level is true if the active user completed all its mini-games.
     *
     * @param username Name of active user (String)
     * @return Map with <code>levelID</code>, boolean (is level completed fully?)>
     */
    public Map<Integer, Boolean> getLevelDone(String username) {
        Map<Integer, Boolean> userProgressHMap = new HashMap<>();
        UserModel.User user = userModel.getUserByName(username);
        int idUser = user.getId();

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM UserProgress WHERE idUser = ?");
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int currentLevel = rs.getInt("idLevel");
                boolean collectingDone = getBool(rs.getInt("collectingDone"));
                boolean puzzleDone = getBool(rs.getInt("puzzleDone"));
                boolean quizDone = getBool(rs.getInt("quizDone"));
                if (currentLevel == 3 && collectingDone && puzzleDone && quizDone) {
                    userProgressHMap.put(1, true);
                    userProgressHMap.put(2, true);
                    userProgressHMap.put(3, true);
                } else if (currentLevel == 3) {
                    userProgressHMap.put(1, true);
                    userProgressHMap.put(2, true);
                    userProgressHMap.put(3, false);
                } else if (currentLevel == 2) {
                    userProgressHMap.put(1, true);
                    userProgressHMap.put(2, false);
                    userProgressHMap.put(3, false);
                } else if (currentLevel == 1) {
                    userProgressHMap.put(1, false);
                    userProgressHMap.put(2, false);
                    userProgressHMap.put(3, false);
                }
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return userProgressHMap;
    }

    /**
     * Converts an int (0 or 1) to boolean value
     *
     * @param i TinyInt in SQL, either 0 or 1
     * @return True, if i = 1
     */
    public boolean getBool(int i) {
        return i == 1;
    }

    /**
     * Marks the collecting game of the current level as completed in the active users progress.
     *
     * @param gameLevel level ID
     * @param username  Name of active user (String)
     * @return False, if gameLevel does not accord with user's current level
     */
    public boolean setCollectBool(int gameLevel, String username) {
        UserModel.User user = userModel.getUserByName(username);
        int idUser = user.getId();

        int currentLevel = getCurrentLevel(username);

        if (currentLevel == gameLevel) {
            db.withConnection(con -> {
                String sql = "UPDATE UserProgress SET collectingDone = ? WHERE idUser = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setInt(1, 1);
                stmt.setInt(2, idUser);
                stmt.executeUpdate();
                stmt.close();
                con.close();
            });
            return true;
        } else {
            return false;
        }
    }

    /**
     * Marks the puzzling game of the current level as completed in the active users progress.
     *
     * @param gameLevel level ID
     * @param username  Name of active user (String)
     * @return False, if gameLevel does not accord with user's current level or puzzle is completed already
     */
    public boolean setPuzzleBool(int gameLevel, String username) {
        UserModel.User user = userModel.getUserByName(username);
        int idUser = user.getId();

        int currentLevel = getCurrentLevel(username);

        if (currentLevel == gameLevel && puzzleNotCompleted(username)) {
            db.withConnection(con -> {
                String sql = "UPDATE UserProgress SET puzzleDone = ? WHERE idUser = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setInt(1, 1);
                stmt.setInt(2, idUser);
                stmt.executeUpdate();
                stmt.close();
                con.close();
            });
            return true;
        } else {
            return false;
        }
    }

    /**
     * Marks the quiz game of the current level as completed in the active users progress.
     * User is upgraded automatically to the next level and all game-bools are set to False (only level 1 and 2).
     *
     * @param gameLevel level ID
     * @param username  Name of active user (String)
     * @return False, if puzzle isn't completed yet
     */
    public boolean setQuizBool(String username, int gameLevel) {
        UserModel.User user = userModel.getUserByName(username);
        int idUser = user.getId();
        int currentLevel = getCurrentLevel(username);
        AtomicBoolean changed = new AtomicBoolean(false);

        db.withConnection(con -> {
            String sql = "UPDATE UserProgress SET idLevel = ? , collectingDone = ? , puzzleDone = ?, quizDone = ? WHERE idUser = ?";
            PreparedStatement stmt = con.prepareStatement(sql);

            if ((currentLevel == gameLevel && currentLevel == 1) || (currentLevel == gameLevel && currentLevel == 2)) {
                if (quizNotCompleted(username)) {
                    changed.set(true);
                }
                stmt.setInt(1, currentLevel + 1);
                stmt.setInt(2, 0);
                stmt.setInt(3, 0);
                stmt.setInt(4, 0);
                stmt.setInt(5, idUser);

            } else if (currentLevel == gameLevel && currentLevel == 3) {
                if (quizNotCompleted(username)) {
                    changed.set(true);
                }

                stmt.setInt(1, 3); //level is not incremented!
                stmt.setInt(2, 1);
                stmt.setInt(3, 1);
                stmt.setInt(4, 1);
                stmt.setInt(5, idUser);
            }
            stmt.executeUpdate();
            stmt.close();
            con.close();
        });
        return changed.get();
    }

    /**
     * @param username Name of active user (String)
     * @return level that the active user is currently playing
     */
    public int getCurrentLevel(String username) {
        UserModel.User user = userModel.getUserByName(username);
        int idUser = user.getId();
        AtomicInteger level = new AtomicInteger();

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM UserProgress WHERE idUser = ?");
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                level.set(rs.getInt("idLevel"));
            }

            rs.close();
            stmt.close();
            con.close();
        });
        return level.get();
    }

    /**
     * Checks, if active user has completed the quiz yet.
     *
     * @param username Name of active user (String)
     * @return True, if quiz is <b>not</b> completed yet
     */
    public boolean quizNotCompleted(String username) {
        UserModel.User user = userModel.getUserByName(username);
        int idUser = user.getId();
        AtomicBoolean currentQuizBool = new AtomicBoolean(false);

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM UserProgress WHERE idUser = ?");
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentQuizBool.set(getBool(rs.getInt("quizDone"))); //true, if quiz is completed
            }

            rs.close();
            stmt.close();
            con.close();
        });
        return !currentQuizBool.get(); // revert boolean!
    }

    /**
     * Checks, if active user has completed the puzzle yet.
     *
     * @param username Name of active user (String)
     * @return True, if quiz is <b>not</b> completed yet
     */
    public boolean puzzleNotCompleted(String username) {
        UserModel.User user = userModel.getUserByName(username);
        int idUser = user.getId();
        AtomicBoolean currentQuizBool = new AtomicBoolean(false);

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM UserProgress WHERE idUser = ?");
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentQuizBool.set(getBool(rs.getInt("puzzleDone")));
            }

            rs.close();
            stmt.close();
            con.close();
        });
        return !currentQuizBool.get();
    }
}
