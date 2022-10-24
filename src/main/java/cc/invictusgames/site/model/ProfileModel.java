package cc.invictusgames.site.model;

import cc.invictusgames.site.Constants;
import cc.invictusgames.site.model.account.SocialLink;
import cc.invictusgames.site.util.pagination.PaginationModel;
import cc.invictusgames.site.util.TimeUtils;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Data
@RequiredArgsConstructor
public class ProfileModel {

    private UUID uuid;
    private String name;
    private RankModel rank;
    private long firstLogin;
    private long lastSeen;
    private long playtime;
    private List<GrantModel> grants = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();

    private boolean op;

    private boolean online;
    private String currentServer;
    private PaginationModel<GrantModel> grantsPagination;
    private Map<String, String> settings = new HashMap<>();

    public ProfileModel(JsonObject object) {
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.name = object.get("name").getAsString();
        this.rank = new RankModel(object.get("rank").getAsJsonObject());
        this.firstLogin = object.get("firstLogin").getAsLong();
        this.lastSeen = object.get("lastSeen").getAsLong();
        this.playtime = object.get("playTime").getAsLong();

        this.op = object.has("isOnOplist") && object.get("isOnOplist").getAsBoolean();
        this.online = object.has("lastServer");
        this.currentServer = this.online ? object.get("lastServer").getAsString() : "None";

        object.get("activeGrants").getAsJsonArray().forEach(element ->
                this.grants.add(new GrantModel(element.getAsJsonObject())));

        if (object.has("effectivePermissions")) {
            JsonObject jsonObject = object.get("effectivePermissions").getAsJsonObject();
            for (String permission : jsonObject.keySet()) {
                if (!jsonObject.get(permission).getAsString().equals("true"))
                    continue;

                this.permissions.add(permission);
            }
        }

        if (object.has("webSettings")) {
            JsonObject settingsObject = object.get("webSettings").getAsJsonObject();
            settingsObject.keySet().forEach(s -> this.settings.put(s, settingsObject.get(s).getAsString()));
        }

        this.grants.sort(GrantModel.GRANT_COMPARATOR);
        this.grantsPagination = new PaginationModel<>(7, this.grants);
    }

    public String getSetting(SocialLink link) {
        return this.settings.getOrDefault(link.getSettingName(), "");
    }

    public List<GrantModel> getGrants(int page) {
        return this.grantsPagination.getPage(page);
    }

    public int getGrantsPages() {
        return this.grantsPagination.getPages();
    }

    public String getFirstJoined() {
        return TimeUtils.getFormattedAgo(this.firstLogin);
    }

    public String getLastJoined() {
        return TimeUtils.getFormattedAgo(this.lastSeen);
    }

    public String getOnlineStatus() {
        return "Currently playing " + this.currentServer;
    }

    public String getOfflineStatus() {
        return "Last seen " + this.getLastJoined();
    }

    public String getPlaytime() {
        return TimeUtils.formatIntoSimpleDetailedString(this.playtime);
    }

    public boolean hasPermission(String permission) {
        return this.permissions.contains(permission.toLowerCase())
                || (this.permissions.contains("invictus.command.op") && this.op);
    }

    public List<SocialLink> getActiveSocials() {
        List<SocialLink> toReturn = new ArrayList<>();
        for (SocialLink value : SocialLink.values()) {
            if (!this.settings.containsKey(value.getSettingName()) || this.getSetting(value).isEmpty())
                continue;

            toReturn.add(value);
        }
        return toReturn;
    }

    public boolean hasGrantOf(String rankName) {
        for (GrantModel grant : grants) {
            if (grant.isActive() && grant.getName().equalsIgnoreCase(rankName))
                return true;
        }

        return false;
    }

    public boolean canInteractWith(ProfileModel other) {
        int weight = rank.getWeight();
        int otherWeight = other.getRank().getWeight();

        if (weight >= Constants.OWNER_WEIGHT)
            return true;

        if (weight >= Constants.ADMIN_WEIGHT && otherWeight < Constants.ADMIN_WEIGHT)
            return true;

        return otherWeight < Constants.STAFF_WEIGHT;
    }

}
