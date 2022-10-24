package cc.invictusgames.site.controller;

import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.model.staff.StaffEntryModel;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import cc.invictusgames.site.util.ErrorUtil;
import cc.invictusgames.site.util.SessionUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class StaffController {

    @RequestMapping("staff")
    public ModelAndView staff(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("staff/index");
        modelAndView.addObject("bannerTitle", "Staff");

        RequestResponse response = RequestHandler.get("staffList");
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        List<StaffEntryModel> staffEntries = new ArrayList<>();
        response.asArray().forEach(element ->
                staffEntries.add(new StaffEntryModel(element.getAsJsonObject())));

        staffEntries.sort(Comparator.comparing(StaffEntryModel::getWeight).reversed());
        modelAndView.addObject("staff", staffEntries);

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile != null)
            modelAndView.addObject("sessionProfile", profile);

        return modelAndView;
    }

}
