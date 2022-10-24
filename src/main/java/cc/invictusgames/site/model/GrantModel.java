package cc.invictusgames.site.model;

import cc.invictusgames.site.util.TimeUtils;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.Comparator;
import java.util.UUID;

@Getter
public class GrantModel {

    public static final Comparator<GrantModel> GRANT_COMPARATOR = (o1, o2) -> {
        if (!o1.isActive() && o2.isActive()) {
            return 1;
        } else if (o1.isActive() && !o2.isActive()) {
            return -1;
        }

        return (int) (o2.getGrantedAt() - o1.getGrantedAt());
    };

    private final UUID id;
    private final UUID uuid;
    private final String name;
    private final boolean removed;
    private final long end;
    private final long duration;
    private final String grantedReason;
    private final String grantedBy;
    private final long grantedAt;
    private final String webColor;

    public GrantModel(JsonObject object) {
        this.id = UUID.fromString(object.get("id").getAsString());
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.name = object.get("resolvedRank").getAsString();
        this.removed = object.get("removed").getAsBoolean();
        this.end = object.get("end").getAsLong();
        this.duration = object.get("duration").getAsLong();
        this.grantedReason = object.get("grantedReason").getAsString();
        this.grantedBy = object.get("resolvedGrantedBy").getAsString();
        this.grantedAt = object.get("grantedAt").getAsLong();
        this.webColor = object.get("webColor").getAsString();
    }

    public String getStatus() {
        if (this.removed)
            return "Removed";

        return this.isActive() ? "Active" : "Expired";
    }

    public String getExpiresAt() {
        if (!this.isActive())
            return duration == -1 ? "Forever"
                    : TimeUtils.formatIntoSimpleDetailedString(duration);

        if (this.end == -1)
            return "Forever";

        return TimeUtils.formatIntoSimpleDetailedString(this.end - System.currentTimeMillis());
    }

    public String getAddedAt() {
        return TimeUtils.getFormattedAgo(this.grantedAt);
    }

    public boolean isActive() {
        if (this.removed)
            return false;

        if (end == -1)
            return true;

        return end >= System.currentTimeMillis();
    }

    public String getDisplayName() {
        return name.replace('-', ' ');
    }

}
