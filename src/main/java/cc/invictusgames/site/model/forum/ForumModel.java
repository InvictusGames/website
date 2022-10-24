package cc.invictusgames.site.model.forum;

import cc.invictusgames.site.util.TimeUtils;
import com.google.gson.JsonObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class ForumModel {

    public static final Comparator<ForumModel> COMPARATOR = (o1, o2) -> {
        int result = Integer.compare(o2.getCategoryWeight(), o1.getCategoryWeight());

        if (result == 0)
            result = Integer.compare(o2.getWeight(), o1.getWeight());

        return result;
    };

    private final String id;
    private String name;
    private String description;
    private int weight;

    private boolean locked;

    private String categoryName;
    private int categoryWeight;
    private List<ForumThread> threads = new ArrayList<>();

    private ForumThread lastThread;
    private long threadAmount;

    public ForumModel(JsonObject object) {
        this.id = object.get("id").getAsString();
        this.name = object.get("name").getAsString();
        this.description = object.get("description").getAsString();
        this.weight = object.get("weight").getAsInt();
        this.locked = object.get("locked").getAsBoolean();

        if (object.has("categoryName"))
            this.categoryName = object.get("categoryName").getAsString();

        if (object.has("categoryWeight"))
            this.categoryWeight = object.get("categoryWeight").getAsInt();

        if (object.has("threads"))
            object.get("threads").getAsJsonArray().forEach(element ->
                    this.threads.add(new ForumThread(element.getAsJsonObject())));

        if (object.has("lastThread"))
            this.lastThread = new ForumThread(object.get("lastThread").getAsJsonObject());

        this.threadAmount = object.get("threadAmount").getAsLong();

        this.threads.sort((o1, o2) -> {
            if (o1.isPinned() && !o2.isPinned())
                return 1;

            if (!o1.isPinned() && o2.isPinned())
                return -1;

            return (int) (o1.getCreatedAt() - o2.getCreatedAt());
        });

        Collections.reverse(this.threads);
    }

    public String getUrlName() {
        return this.name.replace(" ", "-").toLowerCase();
    }

}
