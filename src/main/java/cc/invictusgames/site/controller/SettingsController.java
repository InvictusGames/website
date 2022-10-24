package cc.invictusgames.site.controller;

import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.model.account.SocialLink;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import cc.invictusgames.site.util.ErrorUtil;
import cc.invictusgames.site.util.SessionUtil;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.gson.JsonObject;
import org.passay.PasswordData;
import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private static final Escaper ESCAPER = UrlEscapers.urlFragmentEscaper();
    private static final String TOTP_URL_FORMAT = "otpauth://totp/%s?secret=%s&issuer=%s";
    //otpauth://totp/%s:%s?secret=%s&issuer=%s
    private static final String QR_CODE_URL_FORMAT = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=%s";

    @Autowired
    private BCryptPasswordEncoder encoder;

    @GetMapping
    public ModelAndView settings(HttpServletRequest request,
                                 @ModelAttribute(name = "passwordError") String passwordError,
                                 @ModelAttribute(name = "authError") String authError) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("settings");

        ModelAndView modelAndView = new ModelAndView("profile/settings");
        modelAndView.addObject("sessionProfile", profile);
        modelAndView.addObject("bannerTitle", "Settings");
        modelAndView.addObject("socials", SocialLink.values());

        if (passwordError != null && !passwordError.isEmpty())
            modelAndView.addObject("passErrorMessage", passwordError);

        if (authError != null && !authError.isEmpty())
            modelAndView.addObject("authErrorMessage", authError);

        RequestResponse response = RequestHandler.get("totp/%s", profile.getUuid().toString());
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        JsonObject object = response.asObject();
        if (!object.has("confirmedEnabled")) {
            String url = String.format(
                    TOTP_URL_FORMAT,
                    ESCAPER.escape(profile.getName()),
                    object.get("secretKey").getAsString(),
                    ESCAPER.escape("Brave Network")
            );

            String qrUrl = String.format(QR_CODE_URL_FORMAT, URLEncoder.encode(url));
            modelAndView.addObject("codeLink", qrUrl);
        }

        return modelAndView;
    }

    @PostMapping("enable-2fa")
    public ModelAndView enable2fa(HttpServletRequest request,
                                  RedirectAttributes attributes,
                                  @RequestParam(required = false) Integer token) {
        ModelAndView modelAndView = new ModelAndView("redirect:/settings");
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("settings");

        if (token == null) {
            attributes.addFlashAttribute("authError", "You must enter a token!");
            return modelAndView;
        }

        JsonObject body = new JsonObject();
        body.addProperty("code", token);

        RequestResponse response = RequestHandler.post("totp/%s/tryEnable", body, profile.getUuid().toString());
        if (!response.wasSuccessful()) {
            attributes.addFlashAttribute("authError", response.getErrorMessage());
            return modelAndView;
        }

        JsonObject object = response.asObject();
        if (!object.has("success") || !object.get("success").getAsBoolean()) {
            attributes.addFlashAttribute("authError", "You entered an invalid token.");
            return modelAndView;
        }

        return modelAndView;
    }

    @PostMapping("disable-2fa")
    public ModelAndView disable2fa(HttpServletRequest request,
                                   RedirectAttributes attributes,
                                   @RequestParam(name = "disableToken", required = false) Integer token) {
        ModelAndView modelAndView = new ModelAndView("redirect:/settings");
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("settings");

        if (token == null) {
            attributes.addFlashAttribute("authError", "You must enter a token!");
            return modelAndView;
        }

        JsonObject body = new JsonObject();
        body.addProperty("code", token);

        RequestResponse response = RequestHandler.post("totp/%s/tryAuth", body, profile.getUuid().toString());
        if (!response.wasSuccessful()) {
            attributes.addFlashAttribute("authError", response.getErrorMessage());
            return modelAndView;
        }

        JsonObject object = response.asObject();
        if (!object.has("success") || !object.get("success").getAsBoolean()) {
            attributes.addFlashAttribute("authError", "You entered an invalid token.");
            return modelAndView;
        }

        response = RequestHandler.delete("totp/%s", profile.getUuid().toString());
        if (!response.wasSuccessful()) {
            attributes.addFlashAttribute("authError", response.getErrorMessage());
            return modelAndView;
        }

        return modelAndView;
    }

    @PostMapping("/update-password")
    public ModelAndView updatePassword(HttpServletRequest request,
                                       RedirectAttributes redirectAttributes,
                                       @RequestParam("current_password") String currentPassword,
                                       @RequestParam("new_password") String newPassword,
                                       @RequestParam("confirm_new_password") String confirmedPassword) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("settings");

        ModelAndView modelAndView = new ModelAndView("redirect:/settings");

        if (!newPassword.equals(confirmedPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Your new passwords don't match!");
            return modelAndView;
        }

        PasswordData passwordData = new PasswordData(newPassword);
        RuleResult result = AccountController.VALIDATOR.validate(passwordData);

        if (!result.isValid()) {
            redirectAttributes.addFlashAttribute("passwordError", AccountController.VALIDATOR.getMessages(result).get(0));
            return modelAndView;
        }

        JsonObject body = new JsonObject();
        body.addProperty("currentPassword", this.encoder.encode(currentPassword));
        body.addProperty("password", this.encoder.encode(newPassword));

        RequestResponse response = RequestHandler.put("forum/account/password/%s", body, profile.getUuid().toString());
        if (!response.wasSuccessful()) {
            if (response.getCode() == 403 && response.getResponse() != null && response.asObject().has("invalidPassword")) {
                redirectAttributes.addFlashAttribute("passwordError",
                        "The password you entered as your current password is incorrect.");
            } else redirectAttributes.addFlashAttribute("passwordError", response.getErrorMessage());
        }

        return modelAndView;
    }

    @PostMapping("/update-socials")
    public ModelAndView updateSocials(HttpServletRequest request,
                                      @RequestParam("YOUTUBE") String youtubeUrl,
                                      @RequestParam("TWITTER") String twitterUrl,
                                      @RequestParam("TWITCH") String twitchUrl,
                                      @RequestParam("REDDIT") String redditUrl,
                                      @RequestParam("GITHUB") String githubUrl,
                                      @RequestParam("TELEGRAM") String telegramUrl) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null)
            return ErrorUtil.loginRedirect("settings");

        JsonObject body = new JsonObject();
        body.addProperty(SocialLink.YOUTUBE.getSettingName(), youtubeUrl.trim());
        body.addProperty(SocialLink.TWITTER.getSettingName(), twitterUrl.trim());
        body.addProperty(SocialLink.TWITCH.getSettingName(), twitchUrl.trim());
        body.addProperty(SocialLink.REDDIT.getSettingName(), redditUrl.trim());
        body.addProperty(SocialLink.GITHUB.getSettingName(), githubUrl.trim());
        body.addProperty(SocialLink.TELEGRAM.getSettingName(), telegramUrl.trim());

        RequestResponse response = RequestHandler.put("forum/account/setting/%s", body, profile.getUuid().toString());
        if (!response.wasSuccessful())
            return ErrorUtil.displayError(response);

        return new ModelAndView("redirect:/settings");
    }

}
