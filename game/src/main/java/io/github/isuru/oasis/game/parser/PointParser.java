package io.github.isuru.oasis.game.parser;

import io.github.isuru.oasis.model.Parsers;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.defs.PointsDef;
import io.github.isuru.oasis.model.rules.PointRule;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author iweerarathna
 */
public class PointParser {

    public static List<PointRule> parse(List<PointDef> pointDefs) {
        List<PointRule> pointRules = new LinkedList<>();
        for (PointDef record : pointDefs) {
            PointRule pr = Parsers.parse(record);
            pointRules.add(pr);
        }
        return pointRules;
    }

    public static List<PointRule> parse(InputStream inputStream) {
        Yaml yaml = new Yaml();
        PointsDef pointsDef = yaml.loadAs(inputStream, PointsDef.class);

        return parse(pointsDef.getPoints());
    }

}
