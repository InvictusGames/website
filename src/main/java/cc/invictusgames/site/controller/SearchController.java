package cc.invictusgames.site.controller;

import cc.invictusgames.site.SiteApplication;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchController {

    @GetMapping("/search")
    @ResponseBody
    public String search(@RequestParam("query") String query) {
        RequestResponse response = RequestHandler.get("search?query=%s", query);
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();

        if (response.wasSuccessful()) {
            for (JsonElement jsonElement : response.asArray()) {
                JsonObject userObject = jsonElement.getAsJsonObject();

                userObject.addProperty("link", "/profile/" + userObject.get("name").getAsString());
                userObject.addProperty("avatar", "https://crafatar.com/avatars/" + userObject.get("uuid").getAsString());

                array.add(userObject);
            }
        }

        object.add("users", array);
        return SiteApplication.GSON.toJson(object);
    }

}
