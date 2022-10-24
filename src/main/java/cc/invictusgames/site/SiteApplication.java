package cc.invictusgames.site;

import cc.invictusgames.site.service.UploadService;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Getter
@SpringBootApplication
public class SiteApplication {

    public static final JsonParser JSON_PARSER = new JsonParser();
    public static final Gson GSON = new Gson();

    public static final Parser MARKDOWN_PARSER = Parser.builder().build();
    public static final HtmlRenderer MARKDOWN_RENDERER = HtmlRenderer.builder()
            .softbreak("<br/>")
            .build();

    public static SiteApplication INSTANCE;
    private final UploadService uploadService;

    public SiteApplication() {
        INSTANCE = this;
        this.uploadService = new UploadService();
    }

    public static void main(String[] args) {
        SpringApplication.run(SiteApplication.class);
    }

}
