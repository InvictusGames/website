package cc.invictusgames.site.model.punishment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PunishmentType {

    WARN("Warn", "invictus.command.warn", "invictus.punishment.warn.remove"),
    KICK("Kick", "invictus.command.kick", "invictus.punishment.kick.remove"),
    MUTE("Mute", "invictus.command.tempmute", "invictus.command.unmute"),
    BAN("Ban", "invictus.command.tempban", "invictus.command.unmute"),
    BLACKLIST("Blacklist", "invictus.command.blacklist", "invictus.command.unblacklist");

    private final String displayName;
    private final String permission;
    private final String removePermission;

    public static PunishmentType getType(String name) {
        for (PunishmentType value : values())
            if (value.name().equalsIgnoreCase(name))
                return value;

        return null;
    }

}
