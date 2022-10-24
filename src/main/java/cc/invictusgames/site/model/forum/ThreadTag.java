package cc.invictusgames.site.model.forum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ThreadTag {

    ACCEPTED("Accepted", "#198754"),
    DENIED("Denied", "#dc3545"),
    PENDING("Pending", "#44a1e3");

    private final String displayName;
    private final String color;

}
