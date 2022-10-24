package cc.invictusgames.site.controller.forum;

import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.model.forum.ForumCategory;
import cc.invictusgames.site.model.forum.ForumModel;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import cc.invictusgames.site.util.ErrorUtil;
import cc.invictusgames.site.util.RandomStringUtil;
import cc.invictusgames.site.util.SessionUtil;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ForumsController {

    @RequestMapping("forums")
    public ModelAndView forums(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("forums/index");
        modelAndView.addObject("bannerTitle", "Forums");

        List<ForumCategory> categories = new ArrayList<>();
        RequestResponse response = RequestHandler.get("forum/category");
        if (response.wasSuccessful()) {
            response.asArray().forEach(element ->
                    categories.add(new ForumCategory(element.getAsJsonObject())));
        }

        categories.sort(ForumCategory.CATEGORY_COMPARATOR);
        modelAndView.addObject("categories", categories);

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile != null)
            modelAndView.addObject("sessionProfile", profile);

        return modelAndView;
    }

    @RequestMapping(value = "/forum/{name}", method = RequestMethod.GET)
    public ModelAndView forum(HttpServletRequest request,
                              @PathVariable("name") String name) {
        RequestResponse response = RequestHandler.get("forum/forum/%s?page=1", name);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        JsonObject object = response.asObject();
        ForumModel forumModel = new ForumModel(object);

        ModelAndView modelAndView = new ModelAndView("forum/index");
        modelAndView.addObject("bannerTitle", forumModel.getName());
        modelAndView.addObject("forum", forumModel);

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile != null)
            modelAndView.addObject("sessionProfile", profile);

        return modelAndView;
    }

    @PostMapping("/forum/new")
    public ModelAndView newForum(HttpServletRequest request,
                                 @RequestParam String forumCategory,
                                 @RequestParam String forumName,
                                 @RequestParam int forumWeight,
                                 @RequestParam String forumDescription,
                                 @RequestParam(required = false) boolean forumLocked) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("website.forum.new"))
            return ErrorUtil.noPerms();

        if (forumCategory == null || forumCategory.isEmpty())
            return ErrorUtil.displayError(401, "Forum category field is required.");

        if (forumName == null || forumName.isEmpty())
            return ErrorUtil.displayError(401, "Forum name is required.");

        if (forumDescription == null || forumDescription.isEmpty())
            return ErrorUtil.displayError(401, "Forum description is required.");

        JsonObject body = new JsonObject();
        body.addProperty("categoryId", forumCategory);
        body.addProperty("id", RandomStringUtil.getRandomId(5));
        body.addProperty("name", forumName);
        body.addProperty("description", forumDescription);
        body.addProperty("weight", forumWeight);
        body.addProperty("locked", forumLocked);

        RequestResponse response = RequestHandler.post("forum/forum", body, forumCategory);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forums");
    }

    @PostMapping("/forum/edit")
    public ModelAndView editForum(HttpServletRequest request,
                                  @RequestParam String forumId,
                                  @RequestParam String forumName,
                                  @RequestParam int forumWeight,
                                  @RequestParam String forumDescription,
                                  @RequestParam(required = false) boolean forumLocked) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("website.forum.edit"))
            return ErrorUtil.noPerms();

        if (forumId == null || forumId.isEmpty())
            return ErrorUtil.displayError(401, "Forum id field is required.");

        if (forumName == null || forumName.isEmpty())
            return ErrorUtil.displayError(401, "Forum name is required.");

        if (forumDescription == null || forumDescription.isEmpty())
            return ErrorUtil.displayError(401, "Forum description is required.");

        JsonObject body = new JsonObject();
        body.addProperty("name", forumName);
        body.addProperty("description", forumDescription);
        body.addProperty("weight", forumWeight);
        body.addProperty("locked", forumLocked);

        RequestResponse response = RequestHandler.put("forum/forum/%s", body, forumId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forum/" +
                forumName.replace(" ", "-").toLowerCase());
    }

    @PostMapping("/forum/delete")
    public ModelAndView deleteForum(HttpServletRequest request,
                                    @RequestParam String forumId) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("website.forum.delete"))
            return ErrorUtil.noPerms();

        if (forumId == null || forumId.isEmpty())
            return ErrorUtil.displayError(401, "Forum id field is required.");

        RequestResponse response = RequestHandler.delete("forum/forum/%s", forumId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forums");
    }


}
