package io.github.isuru.oasis.game.parser;

import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.Parsers;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.MilestonesDef;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author iweerarathna
 */
public class MilestoneParser {

    public static List<Milestone> parse(List<MilestoneDef> milestoneDefs) {
        List<Milestone> milestones = new LinkedList<>();
        for (MilestoneDef milestoneDef : milestoneDefs) {
            Milestone milestone = Parsers.parse(milestoneDef);
            milestones.add(milestone);
        }
        return milestones;
    }

    public static List<Milestone> parse(InputStream inputStream) {
        Yaml yaml = new Yaml();
        MilestonesDef milestonesDef = yaml.loadAs(inputStream, MilestonesDef.class);

        return parse(milestonesDef.getMilestones());
    }

}
