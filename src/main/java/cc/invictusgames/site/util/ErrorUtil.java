package cc.invictusgames.site.util;

import cc.invictusgames.site.request.RequestResponse;
import org.springframework.web.servlet.ModelAndView;

public class ErrorUtil {

    public static ModelAndView displayError(String status, String message) {
        ModelAndView error = new ModelAndView("error");

        error.addObject("status", status);
        error.addObject("error", message);

        return error;
    }

    public static ModelAndView displayError(int status, String message) {
        return displayError(String.valueOf(status), message);
    }

    public static ModelAndView displayError(RequestResponse response) {
        return displayError(response.getCode() + " (API)", response.getErrorMessage());
    }

    public static ModelAndView loginRedirect(String redirect, Object... args) {
        return new ModelAndView("redirect:/login?redirect=" + String.format(redirect, args));
    }

    public static ModelAndView notLoggedIn() {
        return displayError(401, "You must be logged in to view this page");
    }

    public static ModelAndView noPerms() {
        return noPerms("You are not allowed to view this page.");
    }

    public static ModelAndView noPerms(String message) {
        return displayError(403, message);
    }

}
