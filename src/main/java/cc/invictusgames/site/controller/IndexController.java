package cc.invictusgames.site.controller;

import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.model.forum.ForumThread;
import cc.invictusgames.site.model.ticket.TicketModel;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import cc.invictusgames.site.util.ErrorUtil;
import cc.invictusgames.site.util.SessionUtil;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class IndexController {

    @RequestMapping({"/", "home"})
    public ModelAndView showIndex(HttpServletRequest request) {
        ModelAndView index = new ModelAndView("index");

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile != null)
            index.addObject("sessionProfile", profile);

        index.addObject("bannerTitle", "BravePvP");
        index.addObject("bannerDescription", "Best PvP Network");

        RequestResponse response = RequestHandler.get("forum/forum/announcements?page=1");
        List<ForumThread> threads = new ArrayList<>();
        if (response.wasSuccessful()) {
            JsonObject object = response.asObject();
            object.get("threads").getAsJsonArray().forEach(element ->
                    threads.add(new ForumThread(element.getAsJsonObject())));
        }

        Collections.reverse(threads);
        index.addObject("threads", threads);

        return index;
    }

    @RequestMapping("tickets")
    public ModelAndView tickets(HttpServletRequest request) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("tickets");

        RequestResponse response = RequestHandler.get("forum/ticket/player/%s", profile.getUuid().toString());
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        ModelAndView modelAndView = new ModelAndView("tickets/index");
        modelAndView.addObject("bannerTitle", "Tickets");
        modelAndView.addObject("sessionProfile", profile);

        List<TicketModel> tickets = new ArrayList<>();
        response.asArray().forEach(element -> tickets.add(new TicketModel(element.getAsJsonObject())));
        tickets.sort(TicketModel.TICKET_COMPARATOR);

        modelAndView.addObject("tickets", tickets);
        return modelAndView;
    }

    @RequestMapping("tickets/admin")
    public ModelAndView adminTickets(HttpServletRequest request) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("tickets/admin");

        if (!profile.hasPermission("website.ticket.viewall"))
            return ErrorUtil.noPerms();

        RequestResponse response = RequestHandler.get("forum/tickets/all");
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        ModelAndView modelAndView = new ModelAndView("tickets/index");
        modelAndView.addObject("bannerTitle", "Tickets");
        modelAndView.addObject("sessionProfile", profile);

        List<TicketModel> tickets = new ArrayList<>();
        response.asArray().forEach(element -> tickets.add(new TicketModel(element.getAsJsonObject())));
        tickets.sort(TicketModel.TICKET_COMPARATOR);

        modelAndView.addObject("tickets", tickets);
        return modelAndView;
    }

}
