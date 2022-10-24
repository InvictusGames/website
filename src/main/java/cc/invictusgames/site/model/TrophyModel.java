package cc.invictusgames.site.model;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrophyModel {

    private final String id;
    private String name;

    public TrophyModel(JsonObject object) {
        this.id = object.get("id").getAsString();
        this.name = object.get("name").getAsString();
    }

    public String getImage() {
        return "/get-upload?file=trophy-" + this.id + ".png";
    }

}
