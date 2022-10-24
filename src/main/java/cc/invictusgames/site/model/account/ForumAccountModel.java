package cc.invictusgames.site.model.account;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ForumAccountModel {

    private UUID uuid;
    private String email;
    private String password;
    private String token;

    public ForumAccountModel(JsonObject object) {
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.email = object.get("email").getAsString();
        this.token = object.get("token").getAsString();
        this.password = object.has("password") ? object.get("password").getAsString() : null;
    }

}
