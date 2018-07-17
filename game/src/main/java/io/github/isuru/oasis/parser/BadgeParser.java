package io.github.isuru.oasis.parser;

import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.rules.BadgeFromEvents;
import io.github.isuru.oasis.model.rules.BadgeFromMilestone;
import io.github.isuru.oasis.model.rules.BadgeFromPoints;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.BadgeSourceDef;
import io.github.isuru.oasis.model.defs.BadgesDef;
import org.mvel2.MVEL;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeParser {

    public static List<BadgeRule> parse(InputStream inputStream) throws IOException {
        Yaml yaml = new Yaml();
        BadgesDef badgesDef = yaml.loadAs(inputStream, BadgesDef.class);

        int b = 0;
        List<BadgeRule> badgeRules = new LinkedList<>();
        for (BadgeDef badgeDef : badgesDef.getBadges()) {
            Badge badge = new Badge(badgeDef.getId(), badgeDef.getName());
            BadgeSourceDef from = badgeDef.getFrom();
            if (from != null) {
                if (from.getPointsId() != null) {
                    BadgeFromPoints bp = new BadgeFromPoints();
                    bp.setId(++b);
                    bp.setMaxBadges(badgeDef.getMaxBadges());
                    bp.setPointsId(from.getPointsId());
                    bp.setDuration(from.getWithin());
                    bp.setStreak(from.getStreak() != null ? from.getStreak() : 0);
                    bp.setBadge(badge);

                    if (from.getSubBadges() != null) {
                        List<BadgeFromPoints.StreakSubBadge> subBadges = new LinkedList<>();
                        for (BadgeDef.SubBadgeDef sbd : from.getSubBadges()) {
                            subBadges.add(new BadgeFromPoints.StreakSubBadge(sbd.getName(), badge, sbd.getStreak()));
                        }
                        bp.setSubBadges(subBadges);
                    }
                    badgeRules.add(bp);

                } else if (from.getMilestoneId() != null) {
                    BadgeFromMilestone bfm = new BadgeFromMilestone();
                    bfm.setId(++b);
                    bfm.setMilestoneId(from.getMilestoneId());
                    bfm.setLevel(from.getLevel());
                    bfm.setBadge(badge);

                    if (from.getSubBadges() != null) {
                        List<BadgeFromMilestone.LevelSubBadge> subBadges = new LinkedList<>();
                        for (BadgeDef.SubBadgeDef sbd : from.getSubBadges()) {
                            subBadges.add(new BadgeFromMilestone.LevelSubBadge(sbd.getName(), badge, sbd.getLevel()));
                        }
                        bfm.setSubBadges(subBadges);
                    }
                    badgeRules.add(bfm);
                }

            } else {
                BadgeFromEvents bfe = new BadgeFromEvents();
                bfe.setId(++b);
                bfe.setBadge(badge);
                bfe.setEventType(badgeDef.getEvent());
                bfe.setDuration(badgeDef.getWithin());
                bfe.setMaxBadges(badgeDef.getMaxBadges());
                if (badgeDef.getCondition() != null) {
                    bfe.setCondition(MVEL.compileExpression(badgeDef.getCondition()));
                }
                if (badgeDef.getStreak() != null) {
                    bfe.setStreak(badgeDef.getStreak());
                }

                if (badgeDef.getSubBadges() != null) {
                    List<Badge> subBadges = new LinkedList<>();
                    for (BadgeDef.SubBadgeDef sbd : badgeDef.getSubBadges()) {
                        if (sbd.getStreak() != null) {
                            subBadges.add(new BadgeFromPoints.StreakSubBadge(sbd.getName(), badge, sbd.getStreak()));
                        } else if (sbd.getCondition() != null) {
                            Serializable serializable = MVEL.compileExpression(sbd.getCondition());
                            subBadges.add(new BadgeFromEvents.ConditionalSubBadge(sbd.getName(), badge, serializable));
                        } else {
                            throw new IOException("Unknown sub badge type!");
                        }
                    }
                    bfe.setSubBadges(subBadges);
                }
                badgeRules.add(bfe);
            }

        }
        return badgeRules;
    }

}
