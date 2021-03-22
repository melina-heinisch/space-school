package controllers;

import model.GuideModel;
import model.UserProgressModel;
import play.mvc.*;
import play.db.*;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the active user's guidebook page.
 */
public class GuideController extends Controller {

    private final AssetsFinder assetsFinder;

    @Inject
    public GuideController(AssetsFinder assetsFinder) {
        this.assetsFinder = assetsFinder;
    }

    /**
     * If an username exists in the session (= valid user logged in), their guidebook page is called.
     * If there is no user logged in, the browser will be redirected to the login page.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/guide</code>.
     *
     * @param request Stores session values (username)
     * @return OK response that renders either guidebook page or login page
     */
    public Result guide(Http.Request request) {
        return request
                .session()
                .get("username")
                .map(username -> ok(views.html.guide.render(assetsFinder)))
                .orElseGet(() -> ok(views.html.login.render(assetsFinder)));
    }

    @Inject
    Database db;

    /**
     * Stores return value of {@link GuideModel#getImagesAndText()} in response body.
     * Configuration in <code>routes</code>: <code>POST</code> request with path <code>/guide</code>.
     *
     * @return OK response which renders JSON array (image source, info text)
     */
    public Result getImagesAndTexts() {
        GuideModel guideModel = new GuideModel(db);
        String data = guideModel.getImagesAndText();
        return ok(data);
    }

    /**
     * Stores return value of {@link model.UserProgressModel#getProgress(String)} in response body.
     * Configuration in <code>routes</code>: <code>GET</code> request with path <code>/progress</code>.
     *
     * @param request Stores session values (username)
     * @return OK response containing JSON array (for all levels: booleans whether a mini game was completed yet)
     */
    public Result getUserProgress(Http.Request request) {
        String username = request.session().get("username").get();

        UserProgressModel progressModel = new UserProgressModel(db);
        String data = progressModel.getProgress(username);
        return ok(data);
    }
}
