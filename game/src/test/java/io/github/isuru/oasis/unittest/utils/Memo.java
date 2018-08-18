package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import scala.Int;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Memo {

    private static final Map<String, List<Tuple4<Long, List<? extends Event>, PointRule, Double>>> pointsMap = new ConcurrentHashMap<>();
    private static final Map<String, List<Tuple4<Long, List<? extends Event>, Badge, BadgeRule>>> badgeMap = new ConcurrentHashMap<>();
    private static final Map<String, List<Tuple4<Long, Integer, Event, Milestone>>> milestoneMap = new ConcurrentHashMap<>();
    private static final Map<String, List<Tuple5<Long, Integer, String, Integer, String>>> statesMap = new ConcurrentHashMap<>();
    private static final Map<String, List<Tuple3<Throwable, Event, PointRule>>> pointsErrorMap = new ConcurrentHashMap<>();
    private static final Map<String, List<Tuple3<Throwable, Event, BadgeRule>>> badgesErrorMap = new ConcurrentHashMap<>();
    private static final Map<String, List<Tuple3<Throwable, Event, Milestone>>> milestoneErrorMap = new ConcurrentHashMap<>();

    private static final Map<String, List<ChallengeEvent>> challengesMap = new ConcurrentHashMap<>();

    public static void addChallenge(String name, ChallengeEvent event) {
        challengesMap.computeIfAbsent(name, s -> new ArrayList<>())
                .add(event);
    }

    public static List<ChallengeEvent> getChallenges(String name) {
        return challengesMap.getOrDefault(name, new ArrayList<>());
    }


    public static void addPoint(String id, Tuple4<Long, List<? extends Event>, PointRule, Double> record) {
        pointsMap.computeIfAbsent(id, s -> new ArrayList<>()).add(record);
    }

    public static void addPointError(String id, Tuple3<Throwable, Event, PointRule> err) {
        pointsErrorMap.computeIfAbsent(id, s -> new ArrayList<>()).add(err);
    }

    public static List<Tuple4<Long, List<? extends Event>, PointRule, Double>> getPoints(String id) {
        return pointsMap.get(id);
    }

    public static List<Tuple3<Throwable, Event, PointRule>> getPointErrors(String id) {
        return pointsErrorMap.get(id);
    }

    //
    // BADGE EVENTS
    //

    public static void addBadge(String id, Tuple4<Long, List<? extends Event>, Badge, BadgeRule> record) {
        badgeMap.computeIfAbsent(id, s -> new ArrayList<>()).add(record);
    }

    public static void addBadgeError(String id, Tuple3<Throwable, Event, BadgeRule> err) {
        badgesErrorMap.computeIfAbsent(id, s -> new ArrayList<>()).add(err);
    }

    public static List<Tuple4<Long, List<? extends Event>, Badge, BadgeRule>> getBadges(String id) {
        return badgeMap.get(id);
    }

    public static List<Tuple3<Throwable, Event, BadgeRule>> getBadgeErrors(String id) {
        return badgesErrorMap.get(id);
    }

    //
    // MILESTONE EVENTS
    //

    public static void addMilestone(String id, Tuple4<Long, Integer, Event, Milestone> record) {
        milestoneMap.computeIfAbsent(id, s -> new ArrayList<>()).add(record);
    }

    public static void addMilestoneError(String id, Tuple3<Throwable, Event, Milestone> err) {
        milestoneErrorMap.computeIfAbsent(id, s -> new ArrayList<>()).add(err);
    }

    public static List<Tuple4<Long, Integer, Event, Milestone>> getMilestones(String id) {
        return milestoneMap.get(id);
    }

    public static List<Tuple3<Throwable, Event, Milestone>> getMilestoneErrors(String id) {
        return milestoneErrorMap.get(id);
    }

    //
    // STATES EVENTS
    //

    public static void addState(String id, Tuple5<Long, Integer, String, Integer, String> record) {
        statesMap.computeIfAbsent(id, s -> new ArrayList<>()).add(record);
    }

    public static List<Tuple5<Long, Integer, String, Integer, String>> getStates(String id) {
        return statesMap.get(id);
    }

    //
    // CLEAR ALL
    //

    public static void clearAll(String id) {
        pointsMap.remove(id);
        pointsErrorMap.remove(id);
        milestoneMap.remove(id);
        milestoneErrorMap.remove(id);
        badgeMap.remove(id);
        badgesErrorMap.remove(id);
    }
}
