package cc.invictusgames.site.controller.forum;

import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import cc.invictusgames.site.util.ErrorUtil;
import cc.invictusgames.site.util.RandomStringUtil;
import cc.invictusgames.site.util.SessionUtil;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @PostMapping("/add")
    public ModelAndView add(HttpServletRequest request,
                            @RequestParam String categoryName,
                            @RequestParam int categoryWeight) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("website.category.new"))
            return ErrorUtil.noPerms();

        if (categoryName == null || categoryName.isEmpty())
            return ErrorUtil.displayError(400, "Category name field is missing.");

        JsonObject body = new JsonObject();
        body.addProperty("id", RandomStringUtil.getRandomId(5));
        body.addProperty("name", categoryName);
        body.addProperty("weight", categoryWeight);

        RequestResponse response = RequestHandler.post("forum/category", body);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forums");
    }

    @PostMapping("/edit")
    public ModelAndView edit(HttpServletRequest request,
                             @RequestParam String ctId,
                             @RequestParam String ctName,
                             @RequestParam int ctWeight) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("website.category.edit"))
            return ErrorUtil.noPerms();

        if (ctName == null || ctName.isEmpty())
            return ErrorUtil.displayError(400, "Category name field is missing.");

        JsonObject body = new JsonObject();
        body.addProperty("name", ctName);
        body.addProperty("weight", ctWeight);

        RequestResponse response = RequestHandler.put("forum/category/%s", body, ctId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forums");
    }

    @PostMapping("/delete")
    public ModelAndView delete(HttpServletRequest request,
                               @RequestParam String catId) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("website.category.delete"))
            return ErrorUtil.noPerms();

        if (catId == null || catId.isEmpty())
            return ErrorUtil.displayError(400, "Category name field is missing.");

        RequestResponse response = RequestHandler.delete("forum/category/%s", catId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forums");
    }

}
