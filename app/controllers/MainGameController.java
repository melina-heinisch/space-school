package controllers;

import model.PuzzlePieceModel;
import model.QuizModel;
import model.UserProgressModel;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.*;
import play.db.*;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's game pages.
 */
public class MainGameController extends Controller {
    private final FormFactory formFactory;
    private final AssetsFinder assetsFinder;

    @Inject
    public MainGameController(FormFactory formFactory, AssetsFinder assetsFinder) {
        this.formFactory = formFactory;
        this.assetsFinder = assetsFinder;
    }

    /**
     * If an username exists in the session (= valid user logged in), the levels page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/levels</code>.
     *
     * @param request Stores session values (username)
     * @return OK Response that renders either levels page or login page
     */
    public Result chooseLevel(Http.Request request) {
        return request
                .session()
                .get("username")
                .map(username -> ok(views.html.levels.render(assetsFinder)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    /**
     * If an username exists in the session (= valid user logged in), the main game (collecting game) page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/mainGame</code>.
     *
     * @param request Stores session values (username)
     * @return OK Response that renders either main game page or login page
     */
    public Result mainGame(Http.Request request) {
        return request
                .session()
                .get("username")
                .map(username -> ok(views.html.mainGame.render(assetsFinder)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    /**
     * If an username exists in the session (= valid user logged in), the quiz levels page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/levelsQuiz</code>.
     *
     * @param request Stores session values (username)
     * @return OK Response that renders either quiz levels page or login page
     */
    public Result chooseLevelQuiz(Http.Request request) {
        return request
                .session()
                .get("username")
                .map(username -> ok(views.html.levelsQuiz.render(assetsFinder)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    /**
     * If an username exists in the session (= valid user logged in), the quiz page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/quiz</code>.
     *
     * @param request Stores session values (username)
     * @return OK Response that renders either quiz page or login page
     */
    public Result quiz(Http.Request request) {
        return request
                .session()
                .get("username")
                .map(username -> ok(views.html.quiz.render(assetsFinder)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    /**
     * If an username exists in the session (= valid user logged in), the puzzle levels page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/levelsPuzzle</code>.
     *
     * @param request Stores session values (username)
     * @return OK Response that renders either puzzle levels page or login page
     */
    public Result chooseLevelPuzzle(Http.Request request) {
        return request
                .session()
                .get("username")
                .map(username -> ok(views.html.levelsPuzzle.render(assetsFinder)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    /**
     * If an username exists in the session (= valid user logged in), the right puzzle page to the current level is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/puzzle</code>.
     *
     * @param request Stores session values (username, current level ID)
     * @return OK Response that renders either puzzle page or login page
     */
    public Result puzzle(Http.Request request) {
        String currentPuzzle = request.session().get("currentlevel").get();
        return request
                .session()
                .get("username")
                .map(username -> {
                    switch (currentPuzzle) {
                        case "1":
                            return ok(views.html.puzzleSatellite.render(assetsFinder));
                        case "2":
                            return ok(views.html.puzzleSuit.render(assetsFinder));
                        case "3":
                            return ok(views.html.puzzleSpaceLauncher.render(assetsFinder));
                        default:
                            return badRequest();
                    }
                })
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    @Inject
    Database db;
    /**
     * Gets all questions and their answers of the current level from the database and stores them in
     * a String (JSON array) in the response body.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/quiz</code>.
     *
     * @param request Stores session values (current level ID)
     * @return OK response that contains all questions and their answers in a String
     */
    public Result getQuestions(Http.Request request) {
        DynamicForm requestData = formFactory.form().bindFromRequest(request);
        int currentlevel = Integer.parseInt(requestData.get("currentlevel"));

        QuizModel handler = new QuizModel(db);
        String questionString = handler.getQuestionsFromDatabase(currentlevel);

        return ok(questionString);
    }

    /**
     * Saves current level ID to session and posts all data of the current level in response body.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/mainGame</code>.
     *
     * @param request Stores session values (current level ID)
     * @return Data of selected level as JSON array String
     */
    public Result getLevel(Http.Request request) {
        DynamicForm requestData = formFactory.form().bindFromRequest(request);
        int selectedLevel = Integer.parseInt(requestData.get("currentlevel"));

        PuzzlePieceModel pieceModel = new PuzzlePieceModel(db);
        String data = pieceModel.getLevelData(selectedLevel);
        return ok(data).addingToSession(request, "currentlevel", String.valueOf(selectedLevel));
    }

    /**
     * Gets the progress of active user = which level they are currently playing and which mini games
     * are completed successfully.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/currentProgress</code>.
     *
     * @param request Stores session values (username)
     * @return OK response that contains level progress data (JSON array) as String
     */
    public Result getCurrentProgress(Http.Request request) {
        String username = request.session().get("username").get();

        UserProgressModel progressModel = new UserProgressModel(db);
        String data = progressModel.getCurrentProgress(username);
        return ok(data);
    }

    /**
     * Sets the boolean for the collecting game of the current level to true in the model.
     * If successful, "true" is stored in the response body.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/collectProgress</code>.
     *
     * @param request Stores session values (current level ID, username)
     * @return OK response that contains boolean value (as String), if collecting game boolean could be changed
     */
    public Result setCollectProgress(Http.Request request) {
        DynamicForm requestData = formFactory.form().bindFromRequest(request);
        int gameLevel = Integer.parseInt(requestData.get("gameLevel"));
        String username = request.session().get("username").get();

        UserProgressModel progressModel = new UserProgressModel(db);
        String changed = String.valueOf(progressModel.setCollectBool(gameLevel, username));
        return ok(changed);
    }

    /**
     * Sets the boolean for the puzzle game of the current level to true in the model.
     * If successful, "true" is stored in the response body.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/puzzleProgress</code>.
     *
     * @param request Stores session values (current level ID, username)
     * @return OK response that contains boolean value (as String), if collecting game boolean could be changed
     */
    public Result setPuzzleProgress(Http.Request request) {
        DynamicForm requestData = formFactory.form().bindFromRequest(request);
        int gameLevel = Integer.parseInt(requestData.get("gameLevel"));
        String username = request.session().get("username").get();

        UserProgressModel progressModel = new UserProgressModel(db);
        String changed = String.valueOf(progressModel.setPuzzleBool(gameLevel, username));
        return ok(changed);

    }

    /**
     * Sets the boolean for the quiz of the current level to true in the model.
     * If successful, "true" is stored in the response body.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/quizProgress</code>.
     *
     * @param request Stores session values (current level ID, username)
     * @return OK response that contains boolean value (as String), if collecting game boolean could be changed
     */
    public Result setQuizProgress(Http.Request request) {
        DynamicForm requestData = formFactory.form().bindFromRequest(request);
        int gameLevel = Integer.parseInt(requestData.get("gameLevel"));
        String username = request.session().get("username").get();

        UserProgressModel progressModel = new UserProgressModel(db);
        String changed = String.valueOf(progressModel.setQuizBool(username, gameLevel));
        return ok(changed);
    }
}
