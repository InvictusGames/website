package cc.invictusgames.site.model.forum;

import cc.invictusgames.site.SiteApplication;
import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.util.TimeUtils;
import com.google.gson.JsonObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ForumThread {

    private final String id;

    private String title;
    private String body;

    private String forum;
    private String forumName;
    private UUID author;
    private String authorName;
    private String authorWebColor;
    private long createdAt;

    private UUID lastEditedBy;
    private long lastEditedAt;

    private List<ForumThread> replies = new ArrayList<>();
    private long lastReplyAt;

    private boolean pinned;
    private boolean locked;

    private String parentThreadId;

    public ForumThread(JsonObject object) {
        this.id = object.get("id").getAsString();
        this.title = object.get("title").getAsString();
        this.body = object.get("body").getAsString();

        if (object.has("forum"))
            this.forum = object.get("forum").getAsString();

        if (object.has("forumName"))
            this.forumName = object.get("forumName").getAsString();

        this.author = UUID.fromString(object.get("author").getAsString());
        this.authorName = object.get("authorName").getAsString();
        this.authorWebColor = object.get("authorWebColor").getAsString();
        this.createdAt = object.get("createdAt").getAsLong();

        if (object.has("lastEditedBy"))
            this.lastEditedBy = UUID.fromString(object.get("lastEditedBy").getAsString());

        this.lastEditedAt = object.get("lastEditedAt").getAsLong();
        this.lastReplyAt = object.get("lastReplyAt").getAsLong();
        this.pinned = object.get("pinned").getAsBoolean();
        this.locked = object.get("locked").getAsBoolean();

        if (object.has("parentThreadId"))
            this.parentThreadId = object.get("parentThreadId").getAsString();

        if (object.has("replies"))
            object.get("replies").getAsJsonArray().forEach(element ->
                    this.replies.add(new ForumThread(element.getAsJsonObject())));
    }

    public String getFormattedForum() {
        return this.forumName.replace(" ", "-").toLowerCase();
    }

    public String getLastReplyAgo() {
        if (this.lastReplyAt == -1)
            return "Never";

        return TimeUtils.getFormattedAgo(this.lastReplyAt);
    }

    public String getLastEditedAgo() {
        if (this.lastEditedAt == -1)
            return "Never";

        return TimeUtils.getFormattedAgo(this.lastEditedAt);
    }

    public String getPostedAgo() {
        return TimeUtils.getFormattedAgo(this.createdAt);
    }

    public boolean canDelete(ProfileModel profileModel) {
        if (profileModel.hasPermission("website.thread.delete.*"))
            return true;

        return this.author.equals(profileModel.getUuid());
    }

    public String getContent() {
        return SiteApplication.MARKDOWN_RENDERER.render(
                SiteApplication.MARKDOWN_PARSER.parse(this.body)
        );
    }

}
