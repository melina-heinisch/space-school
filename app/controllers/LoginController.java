package controllers;

import model.ProfilePageModel;
import model.UserModel;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.*;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's login page.
 */
public class LoginController extends Controller {

    private final AssetsFinder assetsFinder;
    private final FormFactory formFactory;

    @Inject
    public LoginController(AssetsFinder assetsFinder, final FormFactory formFactory) {
        this.assetsFinder = assetsFinder;
        this.formFactory = formFactory;
    }

    /**
     * Applications login page is called.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/login</code>.
     *
     * @return OK response that renders login page
     */
    public Result login() {
        return ok(views.html.login.render(assetsFinder));
    }

    @Inject
    Database db;

    /**
     * The username and password from input fields are validated from database. Username gets stored in session.
     * Response body contains a JSON array with all profile pictures, pic ID, image source, and boolean
     * (if it's user's profile picture).
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/login</code>.
     *
     * @param request Stores session values (username, password)
     * @return OK response if user exists, containing JSON array (String)
     */
    public Result loginValidate(Http.Request request) {
        UserModel userModel = new UserModel(db);
        DynamicForm requestData = formFactory.form().bindFromRequest(request);
        String username = requestData.get("username");
        String password = requestData.get("password");
        ProfilePageModel profilePageModel = new ProfilePageModel(formFactory, db);
        String allProfilePictures = profilePageModel.getPictures(getCurrentPictureID(username));

        if (userModel.authenticate(username, password)) { //überprüft, ob User existiert
            return ok(allProfilePictures).addingToSession(request, "username", username);
        } else {
            return badRequest();
        }
    }

    /**
     * New user is created. Username gets stored in session.
     * Response body contains a JSON array string with all profile pictures, pic ID, image source, and boolean
     * (if it's user's profile picture).
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/register</code>.
     *
     * @param request Stores session values (username, password)
     * @return OK response if user exists
     */
    public Result registerValidate(Http.Request request) {
        UserModel userModel = new UserModel(db);
        ProfilePageModel profilePageModel = new ProfilePageModel(formFactory, db);
        DynamicForm requestData = formFactory.form().bindFromRequest(request);
        String username = requestData.get("username");
        String password = requestData.get("password");
        String allProfilePictures = profilePageModel.getPictures(getCurrentPictureID(username));

        if (userModel.create(username, password)) {
            return ok(allProfilePictures).addingToSession(request, "username", username);
        } else {
            return badRequest();
        }
    }

    /**
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/register</code>.
     *
     * @return OK response that renders register page
     */
    public Result register() {
        return ok(views.html.register.render(assetsFinder));
    }

    /**
     * Returns return value of {@link model.ProfilePageModel#getCurrentPictureID(String)}.
     *
     * @param username Name of logged in user (String)
     * @return Current profile picture ID of user
     */
    private int getCurrentPictureID(String username) {
        ProfilePageModel profilePageModel = new ProfilePageModel(formFactory, db);
        return profilePageModel.getCurrentPictureID(username);
    }
}
