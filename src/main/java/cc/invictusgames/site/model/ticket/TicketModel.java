package cc.invictusgames.site.model.ticket;

import cc.invictusgames.site.SiteApplication;
import cc.invictusgames.site.util.TimeUtils;
import com.google.gson.JsonObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Data
public class TicketModel {

    public static final Comparator<TicketModel> TICKET_COMPARATOR = (o1, o2) -> {
        if (!o1.isClosed() && o2.isClosed())
            return -1;
        if (o1.isClosed() && !o2.isClosed())
            return 1;

        return (int) (o2.getCreatedAt() - o1.getCreatedAt());
    };

    public static final Comparator<TicketReply> REPLY_COMPARATOR =
            (o1, o2) -> (int) (o1.getCreatedAt() - o2.getCreatedAt());

    private final String id;
    private TicketCategory category;

    private String body;
    private UUID author;
    private String authorName;
    private String authorWebColor;
    private long createdAt;

    private TicketStatus status;
    private List<TicketReply> replies = new ArrayList<>();

    public TicketModel(JsonObject object) {
        this.id = object.get("id").getAsString();
        this.body = object.get("body").getAsString();
        this.category = TicketCategory.valueOf(object.get("category").getAsString());

        this.author = UUID.fromString(object.get("author").getAsString());
        this.authorName = object.get("authorName").getAsString();
        this.authorWebColor = object.get("authorWebColor").getAsString();
        this.createdAt = object.get("createdAt").getAsLong();
        this.status = TicketStatus.valueOf(object.get("status").getAsString());

        if (object.has("replies"))
            object.get("replies").getAsJsonArray().forEach(element ->
                    this.replies.add(new TicketReply(element.getAsJsonObject())));

        this.replies.sort(REPLY_COMPARATOR);
    }

    public TicketReply getLastReply() {
        if (this.replies.size() == 0)
            return null;

        return this.replies.get(replies.size() - 1);
    }

    public String getPostedAgo() {
        return TimeUtils.getFormattedAgo(this.createdAt);
    }

    public boolean isClosed() {
        return this.status == TicketStatus.CLOSED || this.status == TicketStatus.RESOLVED;
    }

    public String getContent() {
        return SiteApplication.MARKDOWN_RENDERER.render(
                SiteApplication.MARKDOWN_PARSER.parse(this.body)
        );
    }

}
