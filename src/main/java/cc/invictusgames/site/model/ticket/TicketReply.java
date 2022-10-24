package cc.invictusgames.site.model.ticket;

import cc.invictusgames.site.util.TimeUtils;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class TicketReply {

    private final String id;
    private String parentTicketId;
    private String body;
    private UUID author;
    private String authorName;
    private String authorWebColor;
    private long createdAt;

    public TicketReply(JsonObject object) {
        this.id = object.get("id").getAsString();
        this.parentTicketId = object.get("parentTicketId").getAsString();
        this.body = object.get("body").getAsString();
        this.author = UUID.fromString(object.get("author").getAsString());
        this.authorName = object.get("authorName").getAsString();
        this.authorWebColor = object.get("authorWebColor").getAsString();
        this.createdAt = object.get("createdAt").getAsLong();
    }

    public String getPostedAgo() {
        return TimeUtils.getFormattedAgo(this.createdAt);
    }

}
