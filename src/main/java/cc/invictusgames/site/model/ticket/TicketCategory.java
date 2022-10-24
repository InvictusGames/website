package cc.invictusgames.site.model.ticket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketCategory {

    // ban appeal ?
    // mute appeal ?

    GENERAL("General"),
    STAFF_REPORT("Staff Report"),
    SERVER_ISSUE("Server Issue"),
    PAYMENT_ISSUE("Payment Issue");

    private final String displayName;

}
