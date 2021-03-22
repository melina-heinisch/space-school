package controllers;

import model.FriendListModel;
import model.UserModel;
import play.db.Database;
import play.mvc.*;
import views.html.*;
import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class MainPageController extends Controller {

    private final AssetsFinder assetsFinder;

    @Inject
    public MainPageController(AssetsFinder assetsFinder) {
        this.assetsFinder = assetsFinder;
    }

    @Inject
    Database db;

    /**
     * An action that renders an HTML page with a welcome message.
     */
    public Result welcome() {
        return ok(
                welcomepage.render(assetsFinder)
        );
    }

    /**
     * If an username exists in the session (= valid user logged in), their main page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/main</code>.
     *
     * @param request Stores session values (username, rank)
     * @return OK Response that renders either main page or login page
     */
    public Result mainpage(Http.Request request) {
        String rank = getRank(request.session().get("username").get());
        return request.session().get("username")
                .map(username -> ok(views.html.mainpage.render(assetsFinder, username, rank)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    /**
     * Removes the username stored in the session and redirects to the welcome page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/logout</code>.
     *
     * @param request Stores session values (username)
     * @return ok response from {@link #welcome()}
     */
    public Result logout(Http.Request request) {
        return welcome().removingFromSession(request, "username");
    }

    /**
     * Gets all users except the logged in user and posts them in response body.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/main</code>.
     *
     * @param request Stores session values (username)
     * @return OK response that contains all users als JSON Array (String value)
     */
    public Result setUserList(Http.Request request){
        FriendListModel model = new FriendListModel(db);
        String username = request.session().get("username").get();
        String userlist = model.getUserList(username);
        return ok(userlist);
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