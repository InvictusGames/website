package cc.invictusgames.site.model;

import cc.invictusgames.site.Constants;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RankModel {

    private final UUID uuid;
    private final String name;
    private final String webColor;
    private final int weight;
    private final boolean defaultRank;

    public RankModel(JsonObject object) {
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.name = object.get("name").getAsString();

        if (object.has("webColor"))
            this.webColor = object.get("webColor").getAsString();
        else this.webColor = "#AAAAAA";

        this.weight = object.get("weight").getAsInt();
        this.defaultRank = object.get("defaultRank").getAsBoolean();
    }

    public String getDisplayName() {
        return name.replace('-', ' ');
    }

    public boolean canBeGrantedBy(ProfileModel profile) {
        if (defaultRank)
            return false;

        if (profile.getRank().getWeight() >= Constants.OWNER_WEIGHT
                || profile.getUuid().equals(UUID.fromString("a507f314-d97c-43ca-bab6-99304a492827")))
            return true;

        return profile.getRank().getWeight() > weight && profile.hasPermission("invictus.grant." + name);
    }

    public boolean canBeRemovedBy(ProfileModel profile) {
        return canBeGrantedBy(profile) && profile.hasPermission("invictus.grants.remove");
    }

}
