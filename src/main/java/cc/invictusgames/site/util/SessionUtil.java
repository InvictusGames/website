package cc.invictusgames.site.util;

import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class SessionUtil {

    public static void storeSession(HttpServletRequest request, UUID uuid) {
        request.getSession().setAttribute("uuid", uuid);
    }

    public static ProfileModel getProfile(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute("uuid");

        if (attribute == null)
            return null;

        UUID uuid = (UUID) attribute;
        RequestResponse response = RequestHandler.get("profile/%s?webResolved=true&includePermissions=true", uuid.toString());

        if (!response.wasSuccessful())
            return null;

        return new ProfileModel(response.asObject());
    }

}
