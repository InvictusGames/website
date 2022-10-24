package cc.invictusgames.site.service;

import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import com.google.gson.JsonObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UploadService {

    private static final File UPLOAD_DIR = new File("trophies/");
    private final Map<String, File> fileMap = new ConcurrentHashMap<>();

    public UploadService() {
        if (!UPLOAD_DIR.exists() && !UPLOAD_DIR.mkdir())
            return;

        if (UPLOAD_DIR.listFiles() == null)
            return;

        for (File file : Objects.requireNonNull(UPLOAD_DIR.listFiles()))
            this.fileMap.put(file.getName(), file);

        // trophy specific
        RequestResponse response = RequestHandler.get("forum/trophy");
        if (!response.wasSuccessful())
            return;

        List<String> trophyNames = new ArrayList<>();
        response.asArray().forEach(element -> {
            JsonObject object = element.getAsJsonObject();
            trophyNames.add(object.get("id").getAsString());
        });

        this.fileMap.keySet().stream()
                .filter(s -> !trophyNames.contains(s))
                .forEach(this::tryDeleteFile);
    }

    public Optional<File> getFile(String name) {
        if (this.fileMap.containsKey(name))
            return Optional.of(this.fileMap.get(name));

        return Optional.empty();
    }

    public void tryDeleteFile(String name) {
        this.getFile(name).ifPresent(this::tryDeleteFile);
    }

    public void tryUploadFile(MultipartFile file, String name) throws Exception {
        File newFile = new File(UPLOAD_DIR, name);
        Files.copy(file.getInputStream(), newFile.toPath());

        this.fileMap.put(name, newFile);
    }

    public void tryUploadFile(MultipartFile file) throws Exception {
        this.tryUploadFile(file, file.getOriginalFilename());
    }

    public void tryDeleteFile(File file) {
        if (file.delete())
            this.fileMap.remove(file.getName());
    }

}
