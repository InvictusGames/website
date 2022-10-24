package cc.invictusgames.site.model.forum;

import com.google.gson.JsonObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class ForumCategory {

    public static final Comparator<ForumCategory> CATEGORY_COMPARATOR
            = Comparator.comparing(ForumCategory::getWeight).reversed();

    private final String id;
    private String name;
    private int weight;
    private final List<ForumModel> forums = new ArrayList<>();

    public ForumCategory(JsonObject object) {
        this.id = object.get("id").getAsString();
        this.name = object.get("name").getAsString();
        this.weight = object.get("weight").getAsInt();

        if (object.has("forums"))
            object.get("forums").getAsJsonArray().forEach(element ->
                    this.forums.add(new ForumModel(element.getAsJsonObject())));
    }

    public String getUrlName() {
        return this.name.replace(" ", "-").toLowerCase();
    }

    public List<ForumModel> getFormattedForums() {
        if (this.forums.size() == 0)
            return this.forums;

        ArrayList<ForumModel> toReturn = new ArrayList<>(this.forums);
        toReturn.sort(Comparator.comparing(ForumModel::getWeight).reversed());
        return toReturn;
    }

}
