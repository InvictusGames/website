package cc.invictusgames.site.controller;

import cc.invictusgames.site.model.GrantModel;
import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.model.forum.ForumThread;
import cc.invictusgames.site.model.punishment.PunishmentModel;
import cc.invictusgames.site.model.RankModel;
import cc.invictusgames.site.model.punishment.PunishmentType;
import cc.invictusgames.site.model.ticket.TicketModel;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import cc.invictusgames.site.util.ErrorUtil;
import cc.invictusgames.site.util.Tuple;
import cc.invictusgames.site.util.pagination.PaginationModel;
import cc.invictusgames.site.util.SessionUtil;
import cc.invictusgames.site.util.pagination.ViewPaginationModel;
import cc.invictusgames.site.util.uuid.UUIDCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
public class ProfileController {

    @RequestMapping("/profile/{name}")
    public ModelAndView profile(HttpServletRequest request, @PathVariable("name") String name) {
        return this.create(name, "stats", null, request);
    }

    public ModelAndView create(String name, String page, ViewPaginationModel<?> pagination, HttpServletRequest request) {
        Tuple<ProfileModel, ModelAndView> tuple = this.getProfile(name);
        ProfileModel profileModel = tuple.getFirstValue();
        if (profileModel == null)
            return tuple.getSecondValue();

        ModelAndView modelAndView = new ModelAndView("profile/index");
        modelAndView.addObject("bannerTitle", name);
        modelAndView.addObject("bannerIcon", profileModel.getUuid().toString());
        modelAndView.addObject("profile", profileModel);
        modelAndView.addObject("page", page);

        if (pagination != null) {
            modelAndView.addObject(pagination.getKey(), pagination.getPage(pagination.getCurrentPage()));
            modelAndView.addObject("nextPage", pagination.getNextPage());
            modelAndView.addObject("currentPage", pagination.getCurrentPage());
            modelAndView.addObject("previousPage", pagination.getLastPage());
        }

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile != null)
            modelAndView.addObject("sessionProfile", profile);

        return modelAndView;
    }

    @RequestMapping("/profile/{name}/forums")
    public ModelAndView forums(HttpServletRequest request,
                               @PathVariable("name") String name,
                               @RequestParam(value = "page", required = false) Integer page) {
        UUID uuid = UUIDCache.getUuid(name);
        if (uuid == null)
            return ErrorUtil.displayError(404, "Profile not found");

        if (page == null)
            page = 1;

        RequestResponse response = RequestHandler.get("forum/account/threads/%s", uuid.toString());
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        List<ForumThread> threads = new ArrayList<>();
        response.asArray().forEach(element -> threads.add(new ForumThread(element.getAsJsonObject())));

        return this.create(name, "forums", new ViewPaginationModel<>(page, 7, threads, "profileThreads"), request);
    }

    @RequestMapping("/profile/{name}/punishments")
    public ModelAndView punishments(HttpServletRequest request,
                                    @PathVariable("name") String name,
                                    @RequestParam(value = "page", required = false) Integer page) {
        ProfileModel sessionProfile = SessionUtil.getProfile(request);
        if (sessionProfile == null)
            return ErrorUtil.loginRedirect("profile/%s/punishments", name);

        if (!sessionProfile.hasPermission("invictus.command.history"))
            return ErrorUtil.noPerms();

        UUID uuid = UUIDCache.getUuid(name);
        if (uuid == null)
            return ErrorUtil.displayError(404, "Profile not found");

        RequestResponse response = RequestHandler.get("punishment/profile/%s?webResolved=true", uuid.toString());
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        List<PunishmentModel> punishmentModels = new ArrayList<>();
        response.asArray().forEach(element -> {
            PunishmentModel punishment = new PunishmentModel(element.getAsJsonObject());
            if (punishment.getPunishmentType() == PunishmentType.KICK)
                return;

            if (!sessionProfile.hasPermission("invictus.punishments.fullview")
                    && punishment.getPunishmentType() != PunishmentType.MUTE)
                return;

            if (punishment.getPunishmentType() == PunishmentType.BLACKLIST) {
                if (!sessionProfile.hasPermission("invictus.punishments.viewblacklists")
                        && !sessionProfile.hasPermission("invictus.punishments.viewblacklists.light"))
                    return;

                if (!punishment.isActive() && !sessionProfile.hasPermission("invictus.punishments.viewblacklists"))
                    return;

                if (!sessionProfile.hasPermission("invictus.punishments.viewblacklists")
                        && sessionProfile.hasPermission("invictus.punishments.viewblacklists.light")) {
                    punishment.setPunishedBy("<Hidden>");
                    punishment.setPunishedReason("<Hidden>");
                }
            }

            if (!sessionProfile.hasPermission("invictus.punishments.viewexecutor"))
                punishment.setPunishedBy("<Hidden>");

            punishmentModels.add(punishment);
        });

        punishmentModels.sort(PunishmentModel.PUNISHMENT_COMPARATOR.reversed());

        if (page == null)
            page = 1;

        List<PunishmentType> allowedPunishmentTypes = new ArrayList<>();
        for (PunishmentType type : PunishmentType.values()) {
            if (type != PunishmentType.KICK && sessionProfile.hasPermission(type.getPermission()))
                allowedPunishmentTypes.add(type);
        }

        ViewPaginationModel<PunishmentModel> pagination = new ViewPaginationModel<>(page, 7, punishmentModels, "profilePunishments");
        ModelAndView modelAndView = this.create(name, "punishments", pagination, request);
        modelAndView.addObject("allowedPunishmentTypes", allowedPunishmentTypes);
        return modelAndView;
    }

    @RequestMapping("profile/{name}/grants")
    public ModelAndView grants(HttpServletRequest request,
                               @PathVariable("name") String name,
                               @RequestParam(value = "page", required = false) Integer page) {
        ProfileModel sessionProfile = SessionUtil.getProfile(request);
        if (sessionProfile == null)
            return ErrorUtil.loginRedirect("profile/%s/grants", name);

        if (!sessionProfile.hasPermission("invictus.command.grants"))
            return ErrorUtil.noPerms();

        Tuple<ProfileModel, ModelAndView> tuple = this.getProfile(name);
        if (tuple.getFirstValue() == null)
            return tuple.getSecondValue();

        RequestResponse response = RequestHandler.get("rank?webResolved=true");
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        List<RankModel> rankModels = new ArrayList<>();
        response.asArray().forEach(element -> {
            RankModel rankModel = new RankModel(element.getAsJsonObject());
            if (rankModel.canBeGrantedBy(sessionProfile))
                rankModels.add(rankModel);
        });
        rankModels.sort(Comparator.comparing(RankModel::getWeight).reversed());

        if (page == null)
            page = 1;

        ViewPaginationModel<GrantModel> pagination = new ViewPaginationModel<>(page, 7, tuple.getFirstValue().getGrants(), "profileGrants");
        ModelAndView modelAndView = this.create(name, "grants", pagination, request);
        modelAndView.addObject("ranks", rankModels);
        return modelAndView;
    }

    @RequestMapping("profile/{name}/tickets")
    public ModelAndView tickets(HttpServletRequest request,
                                @PathVariable("name") String name,
                                @RequestParam(value = "page", required = false) Integer page) {
        ProfileModel sessionProfile = SessionUtil.getProfile(request);
        if (sessionProfile == null)
            return ErrorUtil.loginRedirect("profile/%s/tickets", name);

        if (!sessionProfile.hasPermission("website.ticket.viewall"))
            return ErrorUtil.noPerms();

        UUID uuid = UUIDCache.getUuid(name);
        if (uuid == null)
            return ErrorUtil.displayError(404, "Profile not found");

        if (page == null)
            page = 1;

        RequestResponse response = RequestHandler.get("forum/ticket/player/%s", uuid.toString());
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        List<TicketModel> tickets = new ArrayList<>();
        response.asArray().forEach(element -> tickets.add(new TicketModel(element.getAsJsonObject())));

        return this.create(name,
                "tickets",
                new ViewPaginationModel<>(page, 7, tickets, "profileTickets"),
                request
        );
    }

    @PostMapping(value = "/grant/add/{name}")
    public ModelAndView postAddGrant(HttpServletRequest request,
                                     @PathVariable("name") String name,
                                     @RequestParam UUID grantRank,
                                     @RequestParam String grantReason,
                                     @RequestParam int grantDuration,
                                     @RequestParam String grantDurationType) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("invictus.command.grant"))
            return ErrorUtil.noPerms();

        if (name == null || name.isEmpty())
            return ErrorUtil.displayError(404, "Profile not found");

        UUID uuid = UUIDCache.getUuid(name);
        if (uuid == null)
            return ErrorUtil.displayError(404, "Profile not found");

        if (grantRank == null)
            return ErrorUtil.displayError(400, "Rank field is missing.");

        if (grantReason == null || grantReason.isEmpty())
            return ErrorUtil.displayError(400, "Reason field is missing.");

        if (grantDurationType == null || grantDurationType.isEmpty())
            return ErrorUtil.displayError(400, "Duration type field is missing.");

        long duration = -1;
        if (grantDuration > 0 && !grantDurationType.equalsIgnoreCase("forever")) {
            switch (grantDurationType.toLowerCase()) {
                case "days":
                    duration = TimeUnit.DAYS.toMillis(grantDuration);
                    break;
                case "hours:":
                    duration = TimeUnit.HOURS.toMillis(grantDuration);
                    break;
                case "minutes":
                default:
                    duration = TimeUnit.MINUTES.toMillis(grantDuration);
                    break;
            }
        }

        RequestResponse response = RequestHandler.get("rank/%s", grantRank.toString());
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        RankModel rank = new RankModel(response.asObject());
        if (!rank.canBeGrantedBy(profile))
            return ErrorUtil.noPerms("You are not allowed to grant this rank");

        JsonObject body = new JsonObject();
        body.addProperty("id", UUID.randomUUID().toString());
        body.addProperty("rank", grantRank.toString());
        body.addProperty("grantedBy", profile.getUuid().toString());
        body.addProperty("grantedAt", System.currentTimeMillis());
        body.addProperty("grantedReason", grantReason);
        body.addProperty("scopes", "GLOBAL");
        body.addProperty("duration", duration);
        body.addProperty("end", duration == -1 ? -1 : System.currentTimeMillis() + duration);

        response = RequestHandler.post("profile/%s/grants", body, uuid.toString());
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/profile/" + name + "/grants");
    }

    @PostMapping(value = "/grant/remove/{name}/{id}")
    public ModelAndView postRemoveGrant(HttpServletRequest request,
                                        @PathVariable("name") String name,
                                        @PathVariable("id") UUID grantId,
                                        @RequestParam String reason) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.notLoggedIn();

        if (!profile.hasPermission("invictus.grants.remove"))
            return ErrorUtil.noPerms();

        if (reason == null || reason.isEmpty())
            return ErrorUtil.displayError(400, "Reason field is missing");

        UUID uuid = UUIDCache.getUuid(name);
        if (uuid == null)
            return ErrorUtil.displayError(404, "Profile not found");

        RequestResponse response = RequestHandler.get(
                "profile/%s/grants/%s?webResolved=true",
                uuid.toString(),
                grantId.toString()
        );

        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        if (!response.asObject().has("resolvedRank"))
            return ErrorUtil.displayError(404, "Resolved rank not in response");

        RankModel rank = new RankModel(response.asObject().get("resolvedRank").getAsJsonObject());
        if (!rank.canBeRemovedBy(profile))
            return ErrorUtil.noPerms("You are not allowed to remove this rank");

        JsonObject body = new JsonObject();
        body.addProperty("removed", true);
        body.addProperty("removedAt", System.currentTimeMillis());
        body.addProperty("removedBy", profile.getUuid().toString());
        body.addProperty("removedReason", reason);

        response = RequestHandler.put(
                "profile/%s/grants/%s",
                body,
                uuid.toString(),
                grantId.toString()
        );

        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/profile/" + name + "/grants");
    }

    @PostMapping(value = "/punishment/add/{name}")
    public ModelAndView postAddPunishment(HttpServletRequest request,
                                          @PathVariable("name") String name,
                                          @RequestParam(name = "punishmentType") String rawPunishmentType,
                                          @RequestParam String punishmentReason,
                                          @RequestParam int punishmentDuration,
                                          @RequestParam String punishmentDurationType) {
        ProfileModel sessionProfile = SessionUtil.getProfile(request);
        if (sessionProfile == null)
            return ErrorUtil.notLoggedIn();

        if (rawPunishmentType == null || rawPunishmentType.isEmpty())
            return ErrorUtil.displayError(400, "Punishment type field is missing.");

        PunishmentType punishmentType = PunishmentType.getType(rawPunishmentType);
        if (punishmentType == null || punishmentType == PunishmentType.KICK)
            return ErrorUtil.displayError(400, "Invalid punishment type");

        if (!sessionProfile.hasPermission(punishmentType.getPermission()))
            return ErrorUtil.noPerms();

        if (name == null || name.isEmpty())
            return ErrorUtil.displayError(404, "Profile not found");

        UUID uuid = UUIDCache.getUuid(name);
        if (uuid == null)
            return ErrorUtil.displayError(404, "Profile not found");

        if (punishmentReason == null || punishmentReason.isEmpty())
            return ErrorUtil.displayError(400, "Reason field is missing.");

        if (punishmentDurationType == null || punishmentDurationType.isEmpty())
            return ErrorUtil.displayError(400, "Duration type field is missing.");

        // TODO: 09.05.22 duration validation, cant ban longer than x days without certain permission

        long duration = -1;
        if (punishmentDuration > 0 && !punishmentDurationType.equalsIgnoreCase("forever")) {
            switch (punishmentDurationType.toLowerCase()) {
                case "days":
                    duration = TimeUnit.DAYS.toMillis(punishmentDuration);
                    break;
                case "hours:":
                    duration = TimeUnit.HOURS.toMillis(punishmentDuration);
                    break;
                case "minutes":
                default:
                    duration = TimeUnit.MINUTES.toMillis(punishmentDuration);
                    break;
            }
        }

        if (punishmentType == PunishmentType.WARN || punishmentType == PunishmentType.KICK
                || punishmentType == PunishmentType.BLACKLIST)
            duration = -1;

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile != null) {
            JsonArray flags = new JsonArray();
            JsonObject body = new JsonObject();

            flags.add("silent");

            body.addProperty("id", UUID.randomUUID().toString());
            body.addProperty("punishmentType", punishmentType.name());
            body.addProperty("punishedBy", profile.getUuid().toString());
            body.addProperty("punishedAt", System.currentTimeMillis());
            body.addProperty("punishedReason", punishmentReason);
            body.addProperty("punishedServerType", "Website");
            body.addProperty("punishedServer", "");
            body.addProperty("duration", duration);
            body.addProperty("end", duration == -1 ? -1 : System.currentTimeMillis() + duration);
            body.add("flags", flags);

            RequestResponse response = RequestHandler.post("punishment/%s", body, uuid.toString());
            if (!response.wasSuccessful())
                return ErrorUtil.displayError(response);

            return new ModelAndView("redirect:/profile/" + name + "/punishments");
        }

        return ErrorUtil.displayError(401, "You are not authorized to punish users.");
    }

    @PostMapping(value = "/punishment/remove/{name}/{id}")
    public ModelAndView postRemovePunishment(HttpServletRequest request,
                                             @PathVariable("name") String name,
                                             @PathVariable("id") UUID punishmentId,
                                             @RequestParam String reason) {
        ProfileModel sessionProfile = SessionUtil.getProfile(request);
        if (sessionProfile == null)
            return ErrorUtil.notLoggedIn();

        // TODO: 09.05.22 add punishment type to this and check if they are allowed to remove

        if (reason == null || reason.isEmpty())
            return ErrorUtil.displayError(400, "Reason field is missing");

        UUID uuid = UUIDCache.getUuid(name);
        if (uuid == null)
            return ErrorUtil.displayError(404, "Profile not found");

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile != null) {
            JsonObject body = new JsonObject();
            body.addProperty("removed", true);
            body.addProperty("removedAt", System.currentTimeMillis());
            body.addProperty("removedBy", profile.getUuid().toString());
            body.addProperty("removedReason", reason);
            body.addProperty("silent", true);

            RequestResponse response = RequestHandler.put("punishment/%s", body, punishmentId.toString());
            if (!response.wasSuccessful())
                return ErrorUtil.displayError(response);

            return new ModelAndView("redirect:/profile/" + name + "/punishments");
        }

        return ErrorUtil.displayError(401, "You are not authorized to remove punishments from users.");
    }

    public Tuple<ProfileModel, ModelAndView> getProfile(String name) {
        UUID uuid = UUIDCache.getUuid(name);
        if (uuid == null)
            return new Tuple<>(null, ErrorUtil.displayError(404, "Profile not found"));

        RequestResponse response = RequestHandler.get("profile/%s?webResolved=true&includePermissions=true", uuid.toString());
        if (!response.wasSuccessful())
            return new Tuple<>(null, ErrorUtil.displayError(response));

        return new Tuple<>(new ProfileModel(response.asObject()), null);
    }

}
