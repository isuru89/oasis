package io.github.isuru.oasis.game.parser;

import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.Parsers;
import io.github.isuru.oasis.model.defs.StateDef;
import io.github.isuru.oasis.model.defs.StatesDef;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class OStateParser {

    public static List<OState> parse(List<StateDef> stateDefs) throws IOException {
        if (stateDefs == null) {
            return new LinkedList<>();
        }
        // convert def to state
        List<OState> states = new LinkedList<>();
        for (StateDef def : stateDefs) {
            OState oState = Parsers.parse(def);
            states.add(oState);
        }
        return states;
    }

    public static List<OState> parse(InputStream inputStream) throws IOException {
        Yaml yaml = new Yaml();
        StatesDef statesDef = yaml.loadAs(inputStream, StatesDef.class);

        return parse(statesDef.getStates());
    }

}
