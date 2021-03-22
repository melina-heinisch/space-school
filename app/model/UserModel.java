package model;

import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class UserModel {

    private Database db;

    @Inject
    public UserModel(Database db) {
        if (db == null) {
            throw new IllegalArgumentException("Database cannot be null!");
        }
        this.db = db;
    }

    /**
     * Authenticates a user with the given credentials.
     *
     * @param username username from user input
     * @param password password from user input
     * @return boolean, whether given user was found
     */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and Password cannot be null!");
        } else if (username.equals("") || password.equals("")) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }
        AtomicBoolean done = new AtomicBoolean(false);
        //return
        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM User WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                done.set(true);
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return done.get();
    }

    /**
     * Creates new user with given credentials and creates new user progress.
     *
     * @param name     Name of new user
     * @param password Password of User
     * @return True if new user could be created
     */
    public boolean create(String name, String password) {
        if (name == null || password == null) {
            throw new IllegalArgumentException("Username and Password cannot be null!");
        } else if (name.equals("") || password.equals("")) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }
        return db.withConnection(con -> {
            if (usernameTaken(name)) {
                con.close();
                return false;
            } else {
                PreparedStatement stmt = con.prepareStatement("INSERT INTO User (username, password, idProfilePicture) VALUES (?, ?, ?)");
                stmt.setString(1, name);
                stmt.setString(2, password);
                stmt.setInt(3, 1);
                stmt.executeUpdate();
                stmt.close();
                con.close();
                createUserProgress(name);
                return true;
            }
        });
    }

    /**
     * Checks in database if there is a user with that name already.
     *
     * @param username Name of active user
     * @return true if the username is not available
     */
    public boolean usernameTaken(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null!");
        } else if (username.equals("")) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        return db.withConnection(con -> {
            boolean taken = false;
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM User WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                taken = true;
            }
            rs.close();
            stmt.close();
            con.close();
            return taken;
        });
    }

    /**
     * Sets user progress of user to Level 1, no games completed.
     *
     * @param username Name of new user
     */
    public void createUserProgress(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null!");
        } else if (username.equals("")) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("INSERT INTO UserProgress (idUser, idLevel, collectingDone, puzzleDone, quizDone) VALUES (?, ?, ?, ?, ?)");
            stmt.setInt(1, getUserByName(username).getId());
            stmt.setInt(2, 1);
            stmt.setInt(3, 0);
            stmt.setInt(4, 0);
            stmt.setInt(5, 0);
            stmt.executeUpdate();

            stmt.close();
            con.close();
        });
    }

    /**
     * Retrieves a user from database with given ID
     *
     * @param id id of user to find
     * @return User if found, else null
     */
    public User getUserById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Id cannot be negative");
        }
        return db.withConnection(con -> {
            User user = null;
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM User WHERE idUser = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = new User(rs);
            }
            rs.close();
            stmt.close();
            con.close();
            return user;
        });
    }

    /**
     * Searches in database for user with given name and returns User object from ResultSet
     *
     * @param username Name of logged in user
     * @return User object of user with given name
     */
    public User getUserByName(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null!");
        } else if (username.equals("")) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        return db.withConnection(con -> {
            User user = null;
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM User WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = new User(rs);
            }
            rs.close();
            stmt.close();
            con.close();

            return user;
        });
    }

    /**
     * Polymorphism method for getUserById(int)
     *
     * @param id String of id
     * @return User if found, else null
     */
    public User getUserById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null!");
        } else if (id.equals("")) {
            throw new IllegalArgumentException("Id cannot be empty");
        }
        return getUserById(Integer.parseInt(id));
    }

    /**
     * Generates a list with all users
     *
     * @return List with Users
     */
    public List<User> getAllUsers() {
        return db.withConnection(con -> {
            List<User> users = new ArrayList<>();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM User");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User(rs);
                users.add(user);
            }
            rs.close();
            stmt.close();
            con.close();
            return users;
        });
    }

    public class User {
        private int id;
        private String username;
        private int idProfilePicture;
        private String password;

        private User(int id, String username, String password, int idProfilePicture) {
            if (username == null || password == null) {
                throw new IllegalArgumentException("Username and Password cannot be null!");
            } else if (username.equals("") || password.equals("")) {
                throw new IllegalArgumentException("Username and password cannot be empty");
            } else if (id <= 0 || idProfilePicture <= 0) {
                throw new IllegalArgumentException("Id cannot be negative");
            }
            this.id = id;
            this.username = username;
            this.password = password;
            this.idProfilePicture = idProfilePicture;
        }

        private User(ResultSet rs) throws SQLException {
            if (rs == null) {
                throw new IllegalArgumentException("ResultSet cannot be empty");
            }
            this.id = rs.getInt("idUser");
            this.username = rs.getString("username");
            this.password = rs.getString("password");
            this.idProfilePicture = rs.getInt("idProfilePicture");
        }

        /**
         * Updates the user if it already exists and creates it otherwise. Assumes an
         * autoincrement id column.
         */
        public void save() {
            db.withConnection(con -> {
                String sql = "UPDATE User SET username = ?, password = ?, idProfilePicture = ? WHERE idUser = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, this.username);
                stmt.setString(2, this.password);
                stmt.setInt(3, this.idProfilePicture);
                stmt.setInt(4, this.id);
                stmt.executeUpdate();

                stmt.close();
                con.close();
            });
        }

        /**
         * Deletes the user from the database.
         */
        public void delete() {
            db.withConnection(con -> {
                String sql = "DELETE FROM User WHERE idUser = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setInt(1, this.id);
                stmt.executeUpdate();

                stmt.close();
                con.close();
            });
        }

        /**
         * Searches for all of caller's (User) friends and saves them in a List.
         *
         * @return List with befriended users
         */
        public List<User> getFriends() {
            return db.withConnection(con -> {
                List<User> result = new ArrayList<>();
                String sql = "SELECT * FROM UserFriendlist, User WHERE idUser1 = ? AND Friendship.idUser2 = UserId";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setInt(1, this.id);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    User user = new User(rs);
                    result.add(user);
                }
                String sql2 = "SELECT * FROM UserFriendlist, User WHERE idUser2 = ? AND Friendship.idUser1 = UserId";
                PreparedStatement stmt2 = con.prepareStatement(sql2);
                stmt2.setInt(1, this.id);
                ResultSet rs2 = stmt.executeQuery();
                while (rs2.next()) {
                    User user = new User(rs2);
                    result.add(user);
                }
                rs.close();
                stmt.close();
                con.close();
                return result;
            });
        }

        /**
         * @return current user's ID
         */
        public int getId() {
            return id;
        }

        /**
         * Changes user's ID that mustn't be lower than 1.
         *
         * @param id New user ID
         */
        public void setId(int id) {
            if (id <= 0) {
                throw new IllegalArgumentException("Id cannot be negative!");
            }
            this.id = id;
        }

        /**
         * @return current user's name
         */
        public String getUsername() {
            return username;
        }

        /**
         * Sets new username if not empty nor null. Updates the user in database,
         *
         * @param username New username
         */
        public void setUsername(String username) {
            if (username == null) {
                throw new IllegalArgumentException("Username cannot be null!");
            } else if (username.equals("")) {
                throw new IllegalArgumentException("Username cannot be empty!");
            }
            this.username = username;
            this.save();
        }

        /**
         * @return current user's profile picture ID
         */
        public int getIdProfilePicture() {
            return idProfilePicture;
        }

        /**
         * Sets current user's profile picture ID if not negative.
         *
         * @param idProfilePicture current user's new profile picture ID
         */
        public void setIdProfilePicture(int idProfilePicture) {
            if (idProfilePicture <= 0) {
                throw new IllegalArgumentException("Id cannot be negative!");
            }
            this.idProfilePicture = idProfilePicture;
        }

        /**
         * @return current user's password
         */
        public String getPassword() {
            return password;
        }

        /**
         * Sets current user's password new it not null nor empty.
         *
         * @param password new password
         */
        public void setPassword(String password) {
            if (password == null) {
                throw new IllegalArgumentException("Password cannot be null!");
            } else if (password.equals("")) {
                throw new IllegalArgumentException("Password cannot be empty!");
            }
            this.password = password;
        }

        /**
         * Gets and returns rank of current user
         *
         * @return rank of current user
         */
        public String getRank() {
            final String[] rank = new String[1];
            db.withConnection(con -> {
                PreparedStatement stmt = con.prepareStatement("SELECT Level.`rankNames` from Level join UserProgress UP on Level.idLevel = UP.idLevel where idUser = ?");
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    rank[0] = rs.getString("rankNames");
                }
                rs.close();
                stmt.close();
                con.close();
            });
            return rank[0];
        }
    }
}
