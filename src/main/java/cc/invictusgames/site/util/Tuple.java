package cc.invictusgames.site.util;

import lombok.Data;

@Data
public class Tuple<A, B> {

    private final A firstValue;
    private final B secondValue;

}
