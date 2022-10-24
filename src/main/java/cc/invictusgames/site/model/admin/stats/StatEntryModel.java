package cc.invictusgames.site.model.admin.stats;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StatEntryModel {

    private final String key;
    private final String value;

}
