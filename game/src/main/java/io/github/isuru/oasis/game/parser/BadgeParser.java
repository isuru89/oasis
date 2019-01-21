package io.github.isuru.oasis.game.parser;

import io.github.isuru.oasis.model.Parsers;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.BadgesDef;
import io.github.isuru.oasis.model.rules.BadgeRule;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeParser {

    public static List<BadgeRule> parse(List<BadgeDef> badgeDefs) {
        List<BadgeRule> badgeRules = new LinkedList<>();
        for (BadgeDef badgeDef : badgeDefs) {
            if (badgeDef.isManual()) {
                continue;
            }

            badgeRules.add(Parsers.parse(badgeDef));
        }
        return badgeRules;
    }

    public static List<BadgeRule> parse(InputStream inputStream) {
        Yaml yaml = new Yaml();
        BadgesDef badgesDef = yaml.loadAs(inputStream, BadgesDef.class);

        return parse(badgesDef.getBadges());
    }

}
