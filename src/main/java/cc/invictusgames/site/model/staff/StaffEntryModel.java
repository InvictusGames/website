package cc.invictusgames.site.model.staff;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class StaffEntryModel {

    private final UUID uuid;
    private final String name;
    private final int weight;
    private final String webColor;
    private final List<StaffProfileEntry> members = new ArrayList<>();

    public StaffEntryModel(JsonObject object) {
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.name = object.get("name").getAsString();
        this.weight = object.get("weight").getAsInt();
        this.webColor = object.get("webColor").getAsString();

        object.get("members").getAsJsonArray().forEach(element -> {
            JsonObject memberObject = element.getAsJsonObject();
            StaffProfileEntry entry = new StaffProfileEntry(
                    UUID.fromString(memberObject.get("uuid").getAsString()),
                    memberObject.get("name").getAsString()
            );

            if (!this.members.contains(entry))
                this.members.add(entry);
        });

        members.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));
    }

    public String getDisplayName() {
        return name.replace('-', ' ');
    }

}
