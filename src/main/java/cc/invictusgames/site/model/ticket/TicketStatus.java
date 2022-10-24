package cc.invictusgames.site.model.ticket;

import cc.invictusgames.site.model.punishment.PunishmentType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketStatus {

    AWAITING_STAFF_REPLY("Awaiting Staff Reply", "#44a1e3"),
    AWAITING_USER_REPLY("Awaiting User Reply", "#ffc107"),
    RESOLVED("Resolved", "#198754"),
    CLOSED("Closed", "#dc3545");

    private final String displayName;
    private final String webColor;

    public static TicketStatus getStatus(String name) {
        for (TicketStatus value : values())
            if (value.name().equalsIgnoreCase(name))
                return value;

        return null;
    }

}
