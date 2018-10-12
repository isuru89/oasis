package io.github.isuru.oasis.game.parser;

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

    public static List<BadgeRule> parse(List<BadgeDef> badgeDefs) throws IOException {
        List<BadgeRule> badgeRules = new LinkedList<>();
        for (BadgeDef badgeDef : badgeDefs) {
            if (badgeDef.isManual()) {
                continue;
            }

            Badge badge = new Badge(badgeDef.getId(), badgeDef.getName());
            badge.setAwardPoints(badgeDef.getAwardPoints());

            BadgeSourceDef from = badgeDef.getFrom();
            if (from != null) {
                if (from.getPointsRef() != null) {
                    BadgeFromPoints bp = new BadgeFromPoints();
                    bp.setId(badgeDef.getId());
                    bp.setMaxBadges(badgeDef.getMaxBadges());
                    bp.setPointsId(from.getPointsRef());
                    bp.setDuration(from.getWithin());
                    bp.setStreak(from.getStreak() != null ? from.getStreak() : 0);
                    bp.setBadge(badge);

                    if (from.getSubBadges() != null) {
                        List<BadgeFromPoints.StreakSubBadge> subBadges = new LinkedList<>();
                        for (BadgeDef.SubBadgeDef sbd : from.getSubBadges()) {
                            BadgeFromPoints.StreakSubBadge streakSubBadge = new BadgeFromPoints.StreakSubBadge(sbd.getName(), badge, sbd.getStreak());
                            streakSubBadge.setAwardPoints(sbd.getAwardPoints());
                            subBadges.add(streakSubBadge);
                        }
                        bp.setSubBadges(subBadges);
                    }
                    badgeRules.add(bp);

                } else if (from.getMilestoneRef() != null) {
                    BadgeFromMilestone bfm = new BadgeFromMilestone();
                    bfm.setId(badgeDef.getId());
                    bfm.setMilestoneId(from.getMilestoneRef());
                    bfm.setLevel(from.getLevel());
                    bfm.setBadge(badge);

                    if (from.getSubBadges() != null) {
                        List<BadgeFromMilestone.LevelSubBadge> subBadges = new LinkedList<>();
                        for (BadgeDef.SubBadgeDef sbd : from.getSubBadges()) {
                            BadgeFromMilestone.LevelSubBadge levelSubBadge = new BadgeFromMilestone.LevelSubBadge(sbd.getName(), badge, sbd.getLevel());
                            levelSubBadge.setAwardPoints(sbd.getAwardPoints());
                            subBadges.add(levelSubBadge);
                        }
                        bfm.setSubBadges(subBadges);
                    }
                    badgeRules.add(bfm);
                }

            } else {
                BadgeFromEvents bfe = new BadgeFromEvents();
                bfe.setId(badgeDef.getId());
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
                            BadgeFromPoints.StreakSubBadge streakSubBadge = new BadgeFromPoints.StreakSubBadge(sbd.getName(), badge, sbd.getStreak());
                            streakSubBadge.setAwardPoints(sbd.getAwardPoints());
                            subBadges.add(streakSubBadge);
                        } else if (sbd.getCondition() != null) {
                            Serializable serializable = MVEL.compileExpression(sbd.getCondition());
                            BadgeFromEvents.ConditionalSubBadge conditionalSubBadge = new BadgeFromEvents.ConditionalSubBadge(sbd.getName(), badge, serializable);
                            conditionalSubBadge.setAwardPoints(sbd.getAwardPoints());
                            subBadges.add(conditionalSubBadge);
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

    public static List<BadgeRule> parse(InputStream inputStream) throws IOException {
        Yaml yaml = new Yaml();
        BadgesDef badgesDef = yaml.loadAs(inputStream, BadgesDef.class);

        return parse(badgesDef.getBadges());
    }

}
