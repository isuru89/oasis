package io.github.isuru.oasis.game.parser;

import io.github.isuru.oasis.model.Rating;
import io.github.isuru.oasis.model.Parsers;
import io.github.isuru.oasis.model.defs.RatingDef;
import io.github.isuru.oasis.model.defs.RatingsDef;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class RatingsParser {

    public static List<Rating> parse(List<RatingDef> stateDefs) throws IOException {
        if (stateDefs == null) {
            return new LinkedList<>();
        }
        // convert def to state
        List<Rating> states = new LinkedList<>();
        for (RatingDef def : stateDefs) {
            Rating oState = Parsers.parse(def);
            states.add(oState);
        }
        return states;
    }

    public static List<Rating> parse(InputStream inputStream) throws IOException {
        Yaml yaml = new Yaml();
        RatingsDef statesDef = yaml.loadAs(inputStream, RatingsDef.class);

        return parse(statesDef.getRatings());
    }

}
