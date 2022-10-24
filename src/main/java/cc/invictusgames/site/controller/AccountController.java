package cc.invictusgames.site.controller;

import cc.invictusgames.site.model.account.ForumAccountModel;
import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import cc.invictusgames.site.util.SessionUtil;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.gson.JsonObject;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;

@Controller
public class AccountController {

    public static final PasswordValidator VALIDATOR = new PasswordValidator(Arrays.asList(
            new LengthRule(6, 32),
            new WhitespaceRule(),
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1)
    ));

    @Autowired
    private BCryptPasswordEncoder encoder;

    @RequestMapping("/login")
    public ModelAndView login(HttpServletRequest request,
                              @RequestParam(value = "redirect", required = false, defaultValue = "home") String redirect,
                              @RequestParam(value = "success", required = false) String success,
                              @RequestParam(value = "error", required = false) String error) {
        ModelAndView modelAndView = new ModelAndView("redirect:/" + redirect);
        request.setAttribute("redirect", "/" + redirect);
        request.getSession().setAttribute("redirect", "/" + redirect);

        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile != null)
            return modelAndView;

        modelAndView.setViewName("account/login");
        modelAndView.addObject("bannerTitle", "Account");

        if (success != null)
            modelAndView.addObject("successMessage", success);

        if (error != null)
            modelAndView.addObject("errorMessage", error);

        return modelAndView;
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView registerWithEmail(@RequestParam(value = "token", required = false) String token,
                                          @RequestParam(value = "email", required = false) String email,
                                          @RequestParam(value = "username", required = false) String username,
                                          @RequestParam(value = "error", required = false) String error) {

        if (email == null || username == null || token == null)
            return new ModelAndView("account/index");

        ModelAndView modelAndView = new ModelAndView("account/register");

        ForumAccountModel accountModel = new ForumAccountModel();
        modelAndView.addObject("accountModel", accountModel);

        modelAndView.addObject("bannerTitle", "Account");
        modelAndView.addObject("token", token);
        modelAndView.addObject("username", username);
        modelAndView.addObject("email", email);

        if (error != null)
            if (error.equals("0"))
                modelAndView.addObject("errorMessage", "The passwords do not match.");
            else modelAndView.addObject("errorMessage", error);

        return modelAndView;
    }

    @PostMapping("/register/{token}")
    public ModelAndView postRegister(@Valid ForumAccountModel accountModel,
                                     BindingResult bindingResult,
                                     @PathVariable("token") String token,
                                     @RequestParam("email") String email,
                                     @RequestParam("username") String username,
                                     @RequestParam("password") String password,
                                     @RequestParam("confirm_password") String passwordConfirm) {
        ModelAndView modelAndView = new ModelAndView("redirect:/login");

        String encodedPassword = this.encoder.encode(password);
        if (!encoder.matches(passwordConfirm, encodedPassword))
            return new ModelAndView(String.format(
                    "redirect:/register?token=%s&email=%s&username=%s&error=%s",
                    token, email, username, 0
            ));

        PasswordData passwordData = new PasswordData(password);
        RuleResult result = VALIDATOR.validate(passwordData);

        if (!result.isValid())
            return new ModelAndView(String.format(
                    "redirect:/register?token=%s&email=%s&username=%s&error=%s",
                    token, email, username, VALIDATOR.getMessages(result).get(0)
            ));

        JsonObject body = new JsonObject();
        body.addProperty("token", token);
        body.addProperty("password", encodedPassword);

        RequestResponse response = RequestHandler.post("forum/account/register", body);
        if (!response.wasSuccessful())
            return new ModelAndView(String.format(
                    "redirect:/register?token=%s&email=%s&username=%s&error=%s",
                    token, email, username, response.getErrorMessage()
            ));

        modelAndView.addObject("success", "Account successfully created.");

        return modelAndView;
    }

}
