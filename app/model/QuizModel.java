package model;

import org.json.JSONException;
import org.json.JSONObject;
import play.db.Database;

import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuizModel {

    private final Database db;

    @Inject
    public QuizModel(Database db) {
        this.db = db;
    }

    /**
     * Saves all question and answers as JSON objects in an array.
     *
     * @param currentlevel level ID of current level
     * @return JSON Array (String)
     */
    public String getQuestionsFromDatabase(int currentlevel) {
        ArrayList<JSONObject> questions = new ArrayList<>();
        db.withConnection(con -> {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM Questions WHERE idLevel = ?;");
            stmt.setInt(1, currentlevel);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject currentQuestion = new JSONObject();
                Question dbQuestion = new Question(rs);
                try {
                    currentQuestion.put("question", dbQuestion.getQuestion());
                    currentQuestion.put("rightAnswer", dbQuestion.getRightAnswer());
                    currentQuestion.put("wrongAnswer1", dbQuestion.getWrongAnswer1());
                    currentQuestion.put("wrongAnswer2", dbQuestion.getWrongAnswer2());
                    currentQuestion.put("wrongAnswer3", dbQuestion.getWrongAnswer3());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                questions.add(currentQuestion);
            }
            rs.close();
            stmt.close();
            con.close();
        });
        return questions.toString();
    }

    public class Question {
        private int idLevel;
        private String question;
        private String rightAnswer;
        private String wrongAnswer1;
        private String wrongAnswer2;
        private String wrongAnswer3;

        private Question(int idLevel, String question, String rightAnswer, String wrongAnswer1,
                         String wrongAnswer2, String wrongAnswer3) {
            this.idLevel = idLevel;
            this.question = question;
            this.rightAnswer = rightAnswer;
            this.wrongAnswer1 = wrongAnswer1;
            this.wrongAnswer2 = wrongAnswer2;
            this.wrongAnswer3 = wrongAnswer3;
        }

        private Question(ResultSet rs) throws SQLException {
            this.idLevel = rs.getInt("idLevel");
            this.question = rs.getString("question");
            this.rightAnswer = rs.getString("rightAnswer");
            this.wrongAnswer1 = rs.getString("wrongAnswer1");
            this.wrongAnswer2 = rs.getString("wrongAnswer2");
            this.wrongAnswer3 = rs.getString("wrongAnswer3");
        }

        /**
         * Generates a new Map that stores the question's level ID, question (String), answers (1x right, 3x wrong) (String)
         *
         * @return question map
         */
        public Map<String, String> getMap() {
            Map<String, String> question = new HashMap<>();
            question.put("idLevel", String.valueOf(this.idLevel));
            question.put("question", this.question);
            question.put("rightAnswer", this.rightAnswer);
            question.put("wrongAnswer1", this.wrongAnswer1);
            question.put("wrongAnswer2", this.wrongAnswer2);
            question.put("wrongAnswer3", this.wrongAnswer3);
            return question;
        }

        /**
         * Updates question in database with its current values.
         */
        public void save() {
            db.withConnection(con -> {
                String sql = "UPDATE Questions SET idLevel = ?, question = ?, rightAnswer = ?, wrongAnswer1 = ?, wrongAnswer2 = ?, wrongAnswer3 = ? WHERE idUser = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setInt(1, this.idLevel);
                stmt.setString(2, this.question);
                stmt.setString(3, this.rightAnswer);
                stmt.setString(4, this.wrongAnswer1);
                stmt.setString(5, this.wrongAnswer2);
                stmt.setString(6, this.wrongAnswer3);
                stmt.executeUpdate();

                stmt.close();
                con.close();
            });
        }

        /**
         * Delete the question from the database.
         */
        public void delete() {
            db.withConnection(con -> {
                PreparedStatement stmt = con.prepareStatement("DELETE FROM Questions WHERE idLevel = ?");
                stmt.setInt(1, this.idLevel);
                stmt.executeUpdate();

                stmt.close();
                con.close();
            });
        }

        /**
         * @return level ID (int)
         */
        public int getIdLevel() {
            return idLevel;
        }

        /**
         * @param idLevel level ID (int)
         */
        public void setIdLevel(int idLevel) {
            this.idLevel = idLevel;
        }

        /**
         * @return Question as String
         */
        public String getQuestion() {
            return question;
        }

        /**
         * @param question Text (String) containing question
         */
        public void setQuestion(String question) {
            this.question = question;
        }

        /**
         * @return Right answer as String
         */
        public String getRightAnswer() {
            return rightAnswer;
        }

        /**
         * @param rightAnswer Text (String) containing right answer
         */
        public void setRightAnswer(String rightAnswer) {
            this.rightAnswer = rightAnswer;
        }

        /**
         * @return First wrong answer as String
         */
        public String getWrongAnswer1() {
            return wrongAnswer1;
        }

        /**
         * @param wrongAnswer1 Text (String) containing first wrong answer
         */
        public void setWrongAnswer1(String wrongAnswer1) {
            this.wrongAnswer1 = wrongAnswer1;
        }

        /**
         * @return Second wrong answer as String
         */
        public String getWrongAnswer2() {
            return wrongAnswer2;
        }

        /**
         * @param wrongAnswer2 Text (String) containing second wrong answer
         */
        public void setWrongAnswer2(String wrongAnswer2) {
            this.wrongAnswer2 = wrongAnswer2;
        }

        /**
         * @return Third answer as String
         */
        public String getWrongAnswer3() {
            return wrongAnswer3;
        }

        /**
         * @param wrongAnswer3 Text (String) containing third wrong answer
         */
        public void setWrongAnswer3(String wrongAnswer3) {
            this.wrongAnswer3 = wrongAnswer3;
        }
    }
}
