package io.github.isuru.oasis.parser;

import io.github.isuru.oasis.utils.Utils;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.parser.model.MilestoneDef;
import io.github.isuru.oasis.parser.model.MilestonesDef;
import io.github.isuru.oasis.model.AggregatorType;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneParser {

    public static List<Milestone> parse(InputStream inputStream) throws IOException {
        Yaml yaml = new Yaml();
        MilestonesDef milestonesDef = yaml.loadAs(inputStream, MilestonesDef.class);

        List<Milestone> milestones = new LinkedList<>();
        for (MilestoneDef milestoneDef : milestonesDef.getMilestones()) {
            Milestone milestone = new Milestone();
            milestone.setId(milestoneDef.getId());
            milestone.setEvent(milestoneDef.getEvent());

            String type = milestoneDef.getAggregator() != null
                    ? milestoneDef.getAggregator()
                    : AggregatorType.COUNT.name();

            milestone.setAggregator(AggregatorType.valueOf(type.toUpperCase()));
            if (milestoneDef.getAccumulator() != null) {
                milestone.setAccumulatorExpr(Utils.compileExpression(milestoneDef.getAccumulator()));
            }

            if (milestoneDef.getCondition() != null) {
                milestone.setCondition(Utils.compileExpression(milestoneDef.getCondition()));
            }

            List<Milestone.Level> levels = new LinkedList<>();
            int doubleCount = 0;
            boolean asRealValues = "double".equalsIgnoreCase(milestoneDef.getAccumulatorType());
            for (Map.Entry<Integer, Object> entry : milestoneDef.getLevels().entrySet()) {
                Milestone.Level level = new Milestone.Level();
                level.setLevel(entry.getKey());
                Number number = interpretNumber(entry.getValue(), asRealValues);
                level.setNumber(number);

                if (Double.class.isAssignableFrom(number.getClass())) {
                    doubleCount++;
                }
                levels.add(level);
            }
            milestone.setLevels(levels);

            if (milestoneDef.getAccumulatorType() == null) {
                milestone.setRealValues(doubleCount > 0);
            } else {
                milestone.setRealValues(asRealValues);
            }

            milestones.add(milestone);
        }
        return milestones;
    }

    private static Number interpretNumber(Object val, boolean asRealValues) {
        if (Integer.class.isAssignableFrom(val.getClass()) || Long.class.isAssignableFrom(val.getClass())) {
            return asRealValues ? ((Number)val).doubleValue() : ((Number)val).longValue();
        } else if (Double.class.isAssignableFrom(val.getClass())) {
            return ((Number)val).doubleValue();
        } else {
            Double d = Utils.strNum(val.toString());
            return asRealValues ? d : d.longValue();
        }
    }

}
