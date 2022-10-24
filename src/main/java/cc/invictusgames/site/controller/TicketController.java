package cc.invictusgames.site.controller;

import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.model.ticket.TicketCategory;
import cc.invictusgames.site.model.ticket.TicketModel;
import cc.invictusgames.site.model.ticket.TicketStatus;
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
import java.util.UUID;

@Controller
@RequestMapping("/ticket")
public class TicketController {

    @RequestMapping("/{id}")
    public ModelAndView viewTicket(HttpServletRequest request,
                                   @PathVariable("id") String id) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("ticket/%s", id);

        RequestResponse response = RequestHandler.get("forum/ticket/%s", id);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        TicketModel ticket = new TicketModel(response.asObject());
        if (!profile.getUuid().equals(ticket.getAuthor()) && !profile.hasPermission("website.ticket.viewall"))
            return ErrorUtil.noPerms();

        ModelAndView modelAndView = new ModelAndView("ticket/index");
        modelAndView.addObject("ticket", ticket);
        modelAndView.addObject("bannerTitle", "Ticket #" + ticket.getId());
        modelAndView.addObject("sessionProfile", profile);

        return modelAndView;
    }

    @PostMapping("/new")
    public ModelAndView postNewTicket(HttpServletRequest request,
                                      @RequestParam String category,
                                      @RequestParam String content) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("ticket/new");

        if (profile.hasGrantOf("ticket-ban"))
            return ErrorUtil.noPerms("You are banned from creating support tickets");

        JsonObject body = new JsonObject();
        String randomId = RandomStringUtil.getRandomId(5);
        body.addProperty("id", randomId);
        body.addProperty("category", category);
        body.addProperty("body", content);
        body.addProperty("status", TicketStatus.AWAITING_STAFF_REPLY.name());
        body.addProperty("author", profile.getUuid().toString());

        RequestResponse response = RequestHandler.post("forum/ticket", body);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/ticket/" + randomId);
    }

    @GetMapping("/updateStatus/{id}/{status}")
    public ModelAndView postStatus(HttpServletRequest request,
                                   @PathVariable("id") String id,
                                   @PathVariable("status") String rawStatus) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("website.ticket.status.update"))
            return ErrorUtil.noPerms("You are not allowed to update the status of this ticket");

        TicketStatus status = TicketStatus.getStatus(rawStatus);
        if (status == null)
            return ErrorUtil.displayError(400, "Invalid ticket status");

        JsonObject body = new JsonObject();
        body.addProperty("status", status.name());

        RequestResponse response = RequestHandler.put("forum/ticket/%s", body, id);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/ticket/" + id);
    }

    @PostMapping("/reply")
    public ModelAndView postReply(HttpServletRequest request,
                                  @RequestParam UUID ticketAuthor,
                                  @RequestParam String ticketId,
                                  @RequestParam String content) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("ticket/%s", ticketId);

        JsonObject body = new JsonObject();
        body.addProperty("id", RandomStringUtil.getRandomId(5));
        body.addProperty("body", content);
        body.addProperty("author", profile.getUuid().toString());

        RequestResponse response = RequestHandler.post("forum/ticket/%s/reply", body, ticketId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        TicketStatus newStatus = profile.getUuid().equals(ticketAuthor)
                ? TicketStatus.AWAITING_STAFF_REPLY : TicketStatus.AWAITING_USER_REPLY;

        body = new JsonObject();
        body.addProperty("status", newStatus.name());

        response = RequestHandler.put("forum/ticket/%s", body, ticketId);
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/ticket/" + ticketId);
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public ModelAndView newTicket(HttpServletRequest request) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("ticket/new");

        if (profile.hasGrantOf("ticket-ban"))
            return ErrorUtil.noPerms("You are banned from creating support tickets");

        ModelAndView modelAndView = new ModelAndView("ticket/new/index");
        modelAndView.addObject("bannerTitle", "New Ticket");
        modelAndView.addObject("sessionProfile", profile);

        modelAndView.addObject("categories", TicketCategory.values());

        return modelAndView;
    }

}
