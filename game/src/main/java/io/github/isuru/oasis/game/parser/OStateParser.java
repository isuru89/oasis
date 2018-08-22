package io.github.isuru.oasis.game.parser;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.MilestonesDef;
import io.github.isuru.oasis.model.defs.StateDef;
import io.github.isuru.oasis.model.defs.StatesDef;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class OStateParser {

    public static List<OState> parse(List<StateDef> stateDefs) throws IOException {
        // convert def to state
        List<OState> states = new LinkedList<>();
        for (StateDef def : stateDefs) {
            OState oState = new OState();
            oState.setId(def.getId());
            oState.setName(def.getName());
            oState.setDisplayName(def.getDisplayName());

            oState.setEvent(def.getEvent());
            oState.setDefaultState(def.getDefaultState());
            if (def.getCondition() != null && !def.getCondition().trim().isEmpty()) {
                oState.setCondition(Utils.compileExpression(def.getCondition()));
            }
            if (def.getStateValueExpression() != null && ! def.getStateValueExpression().trim().isEmpty()) {
                oState.setStateValueExpression(Utils.compileExpression(def.getStateValueExpression()));
            } else {
                throw new IOException("State 'stateValueExpression' has not been set!");
            }

            if (def.getStateChangeAwards() != null) {
                oState.setStateChangeAwards(def.getStateChangeAwards().stream()
                        .map(OStateParser::fromDefTo)
                        .collect(Collectors.toList()));
            }

            List<OState.OAState> oaStateList = new ArrayList<>();
            boolean defStateFound = false;
            for (StateDef.State state : def.getStates()) {
                defStateFound = defStateFound || Integer.compare(state.getId(), oState.getDefaultState()) == 0;
                OState.OAState oaState = new OState.OAState();
                oaState.setId(state.getId());
                oaState.setName(state.getName());
                oaState.setPoints(state.getPoints());
                if (state.getCondition() != null && !state.getCondition().trim().isEmpty()) {
                    oaState.setCondition(Utils.compileExpression(state.getCondition()));
                }
                oaStateList.add(oaState);
            }

            if (!defStateFound) {
                throw new IOException("The default state id is not found within state list in '" +
                        oState.getName() + "'");
            }

            oState.setStates(oaStateList.stream()
                    .sorted(Comparator.comparingInt(OState.OAState::getId))
                    .collect(Collectors.toList()));
            states.add(oState);
        }
        return states;
    }

    private static OState.OAStateChangeAwards fromDefTo(StateDef.StateChangeAwards changeAwards) {
        OState.OAStateChangeAwards awards = new OState.OAStateChangeAwards();
        awards.setTo(changeAwards.getTo());
        awards.setFrom(changeAwards.getFrom());
        awards.setPoints(changeAwards.getPoints());
        return awards;
    }

    public static List<OState> parse(InputStream inputStream) throws IOException {
        Yaml yaml = new Yaml();
        StatesDef statesDef = yaml.loadAs(inputStream, StatesDef.class);

        return parse(statesDef.getStates());
    }

}
