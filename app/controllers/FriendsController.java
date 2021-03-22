package controllers;

import model.FriendListModel;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the active user's friends page.
 */
public class FriendsController extends Controller {

    private final AssetsFinder assetsFinder;
    private final FormFactory formFactory;

    @Inject
    public FriendsController(AssetsFinder assetsFinder, FormFactory formFactory) {
        this.assetsFinder = assetsFinder;
        this.formFactory = formFactory;
    }

    @Inject
    Database db;

    /**
     * If an username exists in the session (= valid user logged in), their friends page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/friends</code>.
     *
     * @param request Stores session values (username)
     * @return OK Response that renders either friends page or login page
     */
    public Result friends(Http.Request request) {
        return request
                .session()
                .get("username")
                .map(username -> ok(views.html.friendsPage.render(assetsFinder)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    /**
     * If an username exists in the session (= valid user logged in), their friend's profile page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/friendProfile</code>.
     *
     * @param request Stores session values (username)
     * @return OK response that renders either friend's profile page or login page
     */
    public Result openFriendProfile(Http.Request request) {
        return request
                .session()
                .get("username")
                .map(username -> ok(views.html.friendProfile.render(assetsFinder)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    /**
     * If user with ID stored in session 'friendIDToUpdate' is friend (or not friend) of the active user,
     * they will be unfriended (or befriended) and the list of all users and their relationship to the active
     * user are updated.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/friends</code>.
     *
     * @param request Stores session values (username, friend's ID number)
     * @return OK response: String (JSON array) of all users, except the active user, and their relationship to them
     */
    public Result updateFriendships(Http.Request request) {
        FriendListModel model = new FriendListModel(db);

        DynamicForm requestData = formFactory.form().bindFromRequest(request);
        String myUsername = request.session().get("username").get();
        String update = requestData.get("friendIDToUpdate");
        model.updateMyFriends(myUsername, update);

        return ok(model.getUserList(myUsername));
    }
}
