package controllers;

import model.ProfilePageModel;
import model.UserModel;
import model.UserProgressModel;
import org.json.JSONObject;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's profile page.
 */
public class ProfilePageController extends Controller {

    private final AssetsFinder assetsFinder;
    private final FormFactory formFactory;

    @Inject
    public ProfilePageController(AssetsFinder assetsFinder, FormFactory formFactory) {
        this.assetsFinder = assetsFinder;
        this.formFactory = formFactory;
    }

    @Inject
    Database db;

    /**
     * If an username exists in the session (= valid user logged in), their profile page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/profile</code>.
     *
     * @param request Stores session values (username, rank)
     * @return OK Response that renders either profile page or login page
     */
    public Result profilePage(Http.Request request) {
        String rank = getRank(request.session().get("username").get());
        return request
                .session()
                .get("username")
                .map(username -> ok(views.html.profilePage.render(assetsFinder, username, rank)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    /**
     * If an username exists in the session (= valid user logged in), their edit profile page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/edit</code>.
     *
     * @param request Stores session values (username, rank)
     * @return OK Response that renders either edit profile page or login page
     */
    public Result editProfile(Http.Request request) {
        return request
                .session()
                .get("username")
                .map(username -> ok(views.html.editProfile.render(assetsFinder, username)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    /**
     * If changes (name and profile pic) could be applied successfully, the old username in the session will be
     * replaced by the new name.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/edit</code>.
     *
     * @param request Stores session values (username)
     * @return OK response, if changes could be applied
     */
    public Result changeProfileData(Http.Request request) {
        DynamicForm requestData = formFactory.form().bindFromRequest(request);
        String username = requestData.get("username");
        ProfilePageModel profilePageModel = new ProfilePageModel(formFactory, db);

        if (profilePageModel.changeUserDataInDB(request, username)) {
            return ok().addingToSession(request, "username", username);
        } else {
            return badRequest();
        }
    }

    /**
     * Gets number of levels that the active user has completed fully.
     * Posts the level IDs and reward images of these levels in an JSON array.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/rewardIcon</code>.
     *
     * @param request Stores session values (username)
     * @return JSON array (String) with level IDs and reward images
     */
    public Result getRewardIcons(Http.Request request) {
        DynamicForm req = formFactory.form().bindFromRequest(request);
        ArrayList<JSONObject> jsonArray = new ArrayList<>();
        String username = req.get("username");
        UserProgressModel userProgressModel = new UserProgressModel(db);
        Map<Integer, Boolean> userProgressMap = userProgressModel.getLevelDone(username);
        int rewardIconsUnlocked;

        if (userProgressMap.get(3)) {
            rewardIconsUnlocked = 3;
        } else if (userProgressMap.get(2)) {
            rewardIconsUnlocked = 2;
        } else if (userProgressMap.get(1)) {
            rewardIconsUnlocked = 1;
        } else {
            rewardIconsUnlocked = 0;
        }
        ProfilePageModel profilePageModel = new ProfilePageModel(formFactory, db);
        jsonArray = profilePageModel.rewardIconFiller(jsonArray, rewardIconsUnlocked);
        return ok(jsonArray.toString());
    }

    /**
     * Searches for active user in the database and returns their rank.
     *
     * @param username Name of active user
     * @return rank of user (String)
     */
    private String getRank(String username) {
        UserModel u = new UserModel(db);
        UserModel.User user = u.getUserByName(username);
        return user.getRank();
    }
}