package cc.invictusgames.site.model.punishment;

import cc.invictusgames.site.util.TimeUtils;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class PunishmentModel {

    public static final Comparator<PunishmentModel> PUNISHMENT_COMPARATOR = (o1, o2) -> {
        PunishmentType firstType = PunishmentType.valueOf(o1.punishmentType);
        PunishmentType secondType = PunishmentType.valueOf(o2.punishmentType);

        if (firstType.ordinal() >= 2 && o1.isActive() && !o2.isActive())
            return 1;
        else if (secondType.ordinal() >= 2 && o2.isActive() && !o1.isActive())
            return -1;

        return (int) (o1.punishedAt - o2.punishedAt);
    };

    private final UUID id;
    private final UUID uuid;
    private final String punishmentType;
    private final long punishedAt;
    private final long duration;
    private final long end;
    private final boolean removed;

    private String punishedBy;
    private String punishedReason;

    public PunishmentModel(JsonObject object) {
        this.id = UUID.fromString(object.get("id").getAsString());
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.punishmentType = object.get("punishmentType").getAsString();
        this.punishedBy = object.get("resolvedPunishedBy").getAsString();
        this.punishedAt = object.get("punishedAt").getAsLong();
        this.punishedReason = object.get("punishedReason").getAsString();
        this.duration = object.get("duration").getAsLong();
        this.end = object.get("end").getAsLong();
        this.removed = object.get("removed").getAsBoolean();
    }

    public String getColor() {
        switch (this.punishmentType.toLowerCase()) {
            case "warn":
            case "kick":
                return "#FFFF55";
            case "mute":
                return "#FFAA00";
            case "ban":
                return "#FF5555";
            default:
                return "#AA0000";
        }
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
        return TimeUtils.getFormattedAgo(punishedAt);
    }

    public boolean isActive() {
        if (this.removed)
            return false;

        if (end == -1)
            return true;

        return end >= System.currentTimeMillis();
    }

    public PunishmentType getPunishmentType() {
        return PunishmentType.valueOf(this.punishmentType);
    }

}
