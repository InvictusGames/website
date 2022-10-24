package cc.invictusgames.site.controller.forum;

import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.model.forum.ForumCategory;
import cc.invictusgames.site.model.forum.ForumModel;
import cc.invictusgames.site.model.forum.ForumThread;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import cc.invictusgames.site.util.ErrorUtil;
import cc.invictusgames.site.util.RandomStringUtil;
import cc.invictusgames.site.util.SessionUtil;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/forum")
public class ThreadController {

    @RequestMapping("/{forum}/{thread}")
    public ModelAndView thread(HttpServletRequest request,
                               @PathVariable("thread") String thread) {
        RequestResponse response = RequestHandler.get("forum/thread/%s", thread);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        ModelAndView modelAndView = new ModelAndView("thread/index");
        modelAndView.addObject("bannerTitle", "Thread");

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile != null)
            modelAndView.addObject("sessionProfile", profile);

        modelAndView.addObject("thread", new ForumThread(
                response.asObject()
        ));

        return modelAndView;
    }

    @PostMapping("/thread/reply")
    public ModelAndView postReply(HttpServletRequest request,
                                  @RequestParam String threadId,
                                  @RequestParam String forumId,
                                  @RequestParam String content) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("forum/%s/%s", forumId, threadId);

        if (profile.hasGrantOf("post-ban"))
            return ErrorUtil.noPerms("You are banned from posting replies");

        RequestResponse response = RequestHandler.get("forum/thread/%s", threadId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        ForumThread thread = new ForumThread(response.asObject());
        if (thread.isLocked() && !profile.hasPermission("website.thread.lock"))
            return ErrorUtil.noPerms("You are not allowed to reply to locked threads");

        JsonObject body = new JsonObject();
        String randomId = RandomStringUtil.getRandomId(5);
        body.addProperty("id", randomId);
        body.addProperty("forumId", forumId);
        body.addProperty("title", "reply-" + randomId);
        body.addProperty("body", content);
        body.addProperty("author", profile.getUuid().toString());

        response = RequestHandler.post("forum/thread/%s/reply", body, threadId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        JsonObject object = response.asObject();
        String forumName = object.get("forumName").getAsString().replace(" ", "-").toLowerCase();

        return new ModelAndView("redirect:/forum/" + forumName + "/" + threadId);
    }

    @PostMapping("/thread/pin")
    public ModelAndView postPin(HttpServletRequest request,
                                @RequestParam String threadId,
                                @RequestParam String forumName) {
        forumName = forumName.replace(" ", "-").toLowerCase();

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("forum/%s/%s", forumName, threadId);

        if (!profile.hasPermission("website.thread.pin"))
            return ErrorUtil.noPerms();

        RequestResponse response = RequestHandler.get("forum/thread/%s", threadId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);


        ForumThread thread = new ForumThread(response.asObject());

        JsonObject body = new JsonObject();
        body.addProperty("pinned", !thread.isPinned());

        response = RequestHandler.put("forum/thread/%s", body, threadId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forum/"
                + thread.getForumName().replace(" ", "-").toLowerCase() + "/" + threadId);
    }

    @PostMapping("/thread/lock")
    public ModelAndView postLock(HttpServletRequest request,
                                 @RequestParam String threadId,
                                 @RequestParam String forumName) {
        forumName = forumName.replace(" ", "-").toLowerCase();

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("forum/%s/%s", forumName, threadId);

        if (!profile.hasPermission("website.thread.lock"))
            return ErrorUtil.noPerms();

        RequestResponse response = RequestHandler.get("forum/thread/%s", threadId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        ForumThread thread = new ForumThread(response.asObject());

        JsonObject body = new JsonObject();
        body.addProperty("locked", !thread.isLocked());

        response = RequestHandler.put("forum/thread/%s", body, threadId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forum/"
                + thread.getForumName().replace(" ", "-").toLowerCase() + "/" + threadId);
    }

    @PostMapping("/reply/delete")
    public ModelAndView postDeleteReply(HttpServletRequest request,
                                        @RequestParam String forumName,
                                        @RequestParam String parentId,
                                        @RequestParam String replyId) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("");

        RequestResponse response = RequestHandler.delete("forum/thread/%s/%s", parentId, replyId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forum/" + forumName + "/" + parentId);
    }

    @PostMapping("/thread/delete")
    public ModelAndView postDeleteThread(HttpServletRequest request,
                                         @RequestParam String threadId,
                                         @RequestParam String forumName) {
        forumName = forumName.replace(" ", "-").toLowerCase();

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("forum/%s", forumName);

        RequestResponse response = RequestHandler.get("forum/thread/%s", threadId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        ForumThread thread = new ForumThread(response.asObject());
        if (thread.getAuthor() != profile.getUuid() && !profile.hasPermission("website.thread.delete.*"))
            return ErrorUtil.noPerms("You are not allowed to delete this thread");

        response = RequestHandler.delete("forum/thread/%s", threadId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forum/" + forumName);
    }

    @PostMapping("/thread/edit")
    public ModelAndView postEditThread(HttpServletRequest request,
                                       @RequestParam String threadId,
                                       @RequestParam String forumName,
                                       @RequestParam String title,
                                       @RequestParam String body) {
        forumName = forumName.replace(" ", "-").toLowerCase();

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("forum/%s/%s", forumName, threadId);

        if (profile.hasGrantOf("post-ban"))
            return ErrorUtil.noPerms("You are banned from editing threads");

        RequestResponse response = RequestHandler.get("forum/thread/%s", threadId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        ForumThread thread = new ForumThread(response.asObject());
        if (thread.getAuthor() != profile.getUuid() && !profile.hasPermission("website.thread.edit.*"))
            return ErrorUtil.noPerms("You are not allowed to edit this thread");

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("title", title);
        jsonBody.addProperty("body", body);
        jsonBody.addProperty("lastEditedAt", System.currentTimeMillis());
        jsonBody.addProperty("lastEditedBy", profile.getUuid().toString());

        response = RequestHandler.put("forum/thread/%s", jsonBody, threadId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/forum/" + forumName + "/" + threadId);
    }

    @RequestMapping(value = "/thread/edit/{thread}", method = RequestMethod.GET)
    public ModelAndView editThread(HttpServletRequest request,
                                   @PathVariable("thread") String threadName) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("forum/thread/edit/%s", threadName);

        if (profile.hasGrantOf("post-ban"))
            return ErrorUtil.noPerms("You are banned from editing threads");

        RequestResponse response = RequestHandler.get("forum/thread/%s", threadName);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        ForumThread thread = new ForumThread(response.asObject());
        if (thread.getAuthor() != profile.getUuid() && !profile.hasPermission("website.thread.edit.*"))
            return ErrorUtil.noPerms("You are not allowed to edit this thread");

        ModelAndView modelAndView = new ModelAndView("thread/edit/index");
        modelAndView.addObject("bannerTitle", "Edit Thread");
        modelAndView.addObject("sessionProfile", profile);
        modelAndView.addObject("thread", thread);

        return modelAndView;
    }

    @PostMapping("/thread/new")
    public ModelAndView postThread(HttpServletRequest request,
                                   @RequestParam String forumId,
                                   @RequestParam String title,
                                   @RequestParam String content,
                                   @RequestParam UUID author) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("forum/thread/new");

        if (profile.hasGrantOf("post-ban"))
            return ErrorUtil.noPerms("You are banned from posting threads");

        RequestResponse response = RequestHandler.get("forum/forum/%s", forumId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        ForumModel forum = new ForumModel(response.asObject());
        if (forum.isLocked() && !profile.hasPermission("website.forum.post." + forum.getUrlName()))
            return ErrorUtil.noPerms("You are not allowed to post in this forum");

        JsonObject body = new JsonObject();
        String randomId = RandomStringUtil.getRandomId(8);
        body.addProperty("forumId", forumId);
        body.addProperty("id", randomId);
        body.addProperty("title", title);
        body.addProperty("body", content);
        body.addProperty("author", author.toString());

        response = RequestHandler.post("forum/thread", body);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        JsonObject object = response.asObject();
        String forumName = object.get("forumName").getAsString();

        return new ModelAndView("redirect:/forum/"
                + forumName.replace(" ", "-").toLowerCase() + "/" + randomId);
    }

    @RequestMapping(value = "/thread/new", method = RequestMethod.GET)
    public ModelAndView newThread(HttpServletRequest request,
                                  @RequestParam(name = "selected", required = false) String selected) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("forum/thread/new");

        if (profile.hasGrantOf("post-ban"))
            return ErrorUtil.noPerms("You are banned from posting threads");

        ModelAndView modelAndView = new ModelAndView("thread/new/index");
        modelAndView.addObject("bannerTitle", "New Thread");

        List<ForumModel> forums = new ArrayList<>();

        RequestResponse response = RequestHandler.get("forum/category");
        if (response.wasSuccessful()) {
            response.asArray().forEach(element -> {
                ForumCategory category = new ForumCategory(element.getAsJsonObject());

                for (ForumModel forum : category.getForums()) {
                    if (!forum.isLocked() || profile.hasPermission("website.forum.post." + forum.getUrlName()))
                        forums.add(forum);
                }
            });
        }

        forums.sort(ForumModel.COMPARATOR);

        modelAndView.addObject("sessionProfile", profile);
        modelAndView.addObject("forums", forums);
        if (selected != null && !selected.isEmpty())
            modelAndView.addObject("selected", selected);

        return modelAndView;
    }

}
