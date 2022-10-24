package cc.invictusgames.site.model.staff;

import lombok.Data;

import java.util.UUID;

@Data
public class StaffProfileEntry {

    private final UUID uuid;
    private final String name;

}
