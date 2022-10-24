package cc.invictusgames.site.controller;

import cc.invictusgames.site.SiteApplication;
import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.service.UploadService;
import cc.invictusgames.site.util.ErrorUtil;
import cc.invictusgames.site.util.SessionUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Controller
public class UploadController {

    @PostMapping("/upload")
    public ModelAndView upload(HttpServletRequest request,
                               @RequestParam("prevMav") String previousMav,
                               @RequestParam("file") MultipartFile file) {
        ProfileModel profile = SessionUtil.getProfile(request);
        if (profile == null || !profile.hasPermission("website.admin.view"))
            return ErrorUtil.displayError(404, "Not Found");

        try {
            SiteApplication.INSTANCE.getUploadService().tryUploadFile(file);
        } catch (Exception exception) {
            return ErrorUtil.displayError("Error whilst uploading file.", exception.getMessage());
        }

        return new ModelAndView("redirect:/" + previousMav);
    }

    @PostMapping(value = "/delete-upload")
    public ModelAndView deleteUpload(HttpServletRequest request, @RequestParam("file") String fileName) {
        ProfileModel profile = SessionUtil.getProfile(request);
        UploadService uploadService = SiteApplication.INSTANCE.getUploadService();
        Optional<File> fileOpt = uploadService.getFile(fileName);

        if (!fileOpt.isPresent() || profile == null || !profile.hasPermission("website.admin.view"))
            return ErrorUtil.noPerms("You are not authorized to delete uploads.");

        uploadService.tryDeleteFile(fileOpt.get());
        return new ModelAndView("redirect:/");
    }

    @ResponseBody
    @GetMapping(value = "/get-upload", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getUpload(HttpServletRequest request, @RequestParam("file") String fileName) {
        ProfileModel profile = SessionUtil.getProfile(request);
        Optional<File> file = SiteApplication.INSTANCE.getUploadService().getFile(fileName);

        if (!file.isPresent() || profile == null || !profile.hasPermission("website.admin.view"))
            return null;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);
            ImageIO.write(ImageIO.read(file.get()), "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

}
