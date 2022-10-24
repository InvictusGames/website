package cc.invictusgames.site.controller;

import cc.invictusgames.site.SiteApplication;
import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.model.TrophyModel;
import cc.invictusgames.site.model.admin.stats.StatEntryModel;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import cc.invictusgames.site.util.ErrorUtil;
import cc.invictusgames.site.util.RandomStringUtil;
import cc.invictusgames.site.util.SessionUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    public ModelAndView create(String page, ProfileModel profile) {
        ModelAndView modelAndView = new ModelAndView("admin/index");

        modelAndView.addObject("bannerTitle", "Admin Panel");
        modelAndView.addObject("bannerDescription", StringUtils.capitalize(page));
        modelAndView.addObject("page", page);
        modelAndView.addObject("sessionProfile", profile);

        return modelAndView;
    }

    @RequestMapping("/trophies")
    public ModelAndView trophies(HttpServletRequest request) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("admin/trophies");

        if (!profile.hasPermission("website.admin.view"))
            return ErrorUtil.noPerms();

        ModelAndView modelAndView = this.create("trophies", profile);

        List<TrophyModel> trophyModels = new ArrayList<>();
        RequestResponse response = RequestHandler.get("forum/trophy");
        if (response.wasSuccessful()) {
            JsonArray array = response.asArray();

            array.forEach(element -> trophyModels.add(
                    new TrophyModel(element.getAsJsonObject())
            ));
        }

        modelAndView.addObject("trophies", trophyModels);
        modelAndView.addObject("prevMav", "admin/trophies");

        return modelAndView;
    }

    @PostMapping("/trophy/add")
    public ModelAndView addTrophy(HttpServletRequest request,
                                  @RequestParam("trophyName") String trophyName,
                                  @RequestParam("file") MultipartFile file) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("website.admin.view"))
            return ErrorUtil.noPerms();

        JsonObject body = new JsonObject();
        String randomId = RandomStringUtil.getRandomId(5);
        body.addProperty("id", randomId);
        body.addProperty("name", trophyName);

        RequestResponse response = RequestHandler.post("forum/trophy", body);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        try {
            SiteApplication.INSTANCE.getUploadService().tryUploadFile(file, "trophy-" + randomId + ".png");
        } catch (Exception exception) {
//            return ErrorUtil.displayError("Error whilst uploading file.", exception.getMessage());
            exception.printStackTrace();
        }

        return new ModelAndView("redirect:/admin/trophies");
    }

    @PostMapping("/trophy/delete")
    public ModelAndView deleteTrophy(HttpServletRequest request, @RequestParam("trophyId") String trophyId) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("website.admin.view"))
            return ErrorUtil.noPerms();

        RequestResponse response = RequestHandler.delete("forum/trophy/%s", trophyId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        SiteApplication.INSTANCE.getUploadService().tryDeleteFile("trophy-" + trophyId + ".png");
        return new ModelAndView("redirect:/admin/trophies");
    }

    @RequestMapping
    public ModelAndView template(HttpServletRequest request) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("admin");

        if (!profile.hasPermission("website.admin.view"))
            return ErrorUtil.noPerms();

        ModelAndView modelAndView = this.create("stats", profile);

        // TODO: 11.05.22 make this just display a small thing in the page
        RequestResponse response = RequestHandler.get("stats/combined");
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        List<StatEntryModel> cacheGeneral = new ArrayList<>();
        List<StatEntryModel> cacheForums = new ArrayList<>();
        List<StatEntryModel> api = new ArrayList<>();
        List<StatEntryModel> master = new ArrayList<>();
        List<StatEntryModel> network = new ArrayList<>();

        JsonObject parent = response.asObject();

        JsonObject object = parent.get("cache").getAsJsonObject();
        object.get("General").getAsJsonObject().entrySet().forEach(entry ->
                cacheGeneral.add(new StatEntryModel(entry.getKey(), entry.getValue().getAsString())));

        object.get("Forums").getAsJsonObject().entrySet().forEach(entry ->
                cacheForums.add(new StatEntryModel(entry.getKey(), entry.getValue().getAsString())));

        parent.get("api").getAsJsonObject().entrySet().forEach(entry ->
                api.add(new StatEntryModel(entry.getKey(), entry.getValue().getAsString())));

        parent.get("master").getAsJsonObject().entrySet().forEach(entry ->
                master.add(new StatEntryModel(entry.getKey(), entry.getValue().getAsString())));

        parent.get("network").getAsJsonObject().entrySet().forEach(entry ->
                network.add(new StatEntryModel(entry.getKey(), entry.getValue().getAsString())));

        modelAndView.addObject("cacheGeneral", cacheGeneral);
        modelAndView.addObject("cacheForums", cacheForums);
        modelAndView.addObject("api", api);
        modelAndView.addObject("master", master);
        modelAndView.addObject("network", network);

        return modelAndView;
    }

}
