package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.game.parser.*;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.handlers.*;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class TestUtils {

    public static void main(String[] args) {
        Instant start = LocalDateTime.of(2018, 12, 1, 0, 1)
                .atZone(ZoneId.systemDefault()).toInstant();
        Instant end = LocalDateTime.of(2018, 12, 3, 23, 59)
                .atZone(ZoneId.systemDefault()).toInstant();
        int diff = (int) (end.toEpochMilli() - start.toEpochMilli());
        Random r = new Random(123);
        List<Integer> nums = new ArrayList<>();
        for (int i = 0; i < 33; i++) {
            nums.add(r.nextInt(diff));
        }
        Collections.sort(nums);

        long sm = start.toEpochMilli();
        for (int i = 0; i < nums.size(); i++) {
            System.out.println(sm + nums.get(i));
        }
    }


    public static List<Tuple3<Long, String, Double>> parseWinners(String file) throws IOException {
        try (InputStream inputStream = TestUtils.loadResource(file)) {
            LineIterator lineIterator = IOUtils.lineIterator(inputStream, StandardCharsets.UTF_8);
            List<Tuple3<Long, String, Double>> list = new LinkedList<>();
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("#")) continue;

                String[] parts = line.split("[,]");

                list.add(Tuple3.of(
                        Long.parseLong(parts[0]),
                        parts[1],
                        Double.parseDouble(parts[2])));
            }
            return list;
        }
    }

    public static List<Tuple5<Long, String, String, Double, String>> parsePointOutput(String file) throws IOException {
        try (InputStream inputStream = TestUtils.loadResource(file)) {
            LineIterator lineIterator = IOUtils.lineIterator(inputStream, StandardCharsets.UTF_8);
            List<Tuple5<Long, String, String, Double, String>> list = new LinkedList<>();
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("#")) continue;

                String[] parts = line.split("[,]");

                Tuple5<Long, String, String, Double, String> row = Tuple5.of(
                        Long.parseLong(parts[0]),
                        parts[1],
                        parts[2],
                        Double.parseDouble(parts[3]),
                        parts[4]
                );
                list.add(row);
            }
            return list;
        }
    }

    public static List<Tuple4<Long, String, Integer, String>> parseMilestoneOutput(String file) throws IOException {
        try (InputStream inputStream = TestUtils.loadResource(file)) {
            LineIterator lineIterator = IOUtils.lineIterator(inputStream, StandardCharsets.UTF_8);
            List<Tuple4<Long, String, Integer, String>> list = new LinkedList<>();
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("#")) continue;
                String[] parts = line.split("[,]");

                Tuple4<Long, String, Integer, String> row = Tuple4.of(
                        Long.parseLong(parts[0]),
                        parts[1],
                        Integer.parseInt(parts[2]),
                        parts[3]
                );
                list.add(row);
            }
            return list;
        }
    }

    public static List<Tuple5<Long, String, String, String, String>> parseBadgesOutput(String file) throws IOException {
        try (InputStream inputStream = TestUtils.loadResource(file)) {
            LineIterator lineIterator = IOUtils.lineIterator(inputStream, StandardCharsets.UTF_8);
            List<Tuple5<Long, String, String, String, String>> list = new LinkedList<>();
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("#")) continue;
                String[] parts = line.split("[,]");

                Tuple5<Long, String, String, String, String> row = Tuple5.of(
                        Long.parseLong(parts[0]),
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4]
                );
                list.add(row);
            }
            return list;
        }
    }

    public static List<Tuple5<Long, Integer, String, Integer, String>> parseStatesOutput(String file) throws IOException {
        try (InputStream inputStream = TestUtils.loadResource(file)) {
            LineIterator lineIterator = IOUtils.lineIterator(inputStream, StandardCharsets.UTF_8);
            List<Tuple5<Long, Integer, String, Integer, String>> list = new LinkedList<>();
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("#")) continue;
                String[] parts = line.split("[,]");

                Tuple5<Long, Integer, String, Integer, String> row = Tuple5.of(
                        Long.parseLong(parts[0]),
                        Integer.parseInt(parts[1]),
                        parts[2],
                        Integer.parseInt(parts[3]),
                        parts[4]
                );
                list.add(row);
            }
            return list;
        }
    }

    public static boolean isResourceExist(String resourceId) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceId)) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }

    public static InputStream loadResource(String resourceId) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceId);
    }

    public static StreamExecutionEnvironment createEnv() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        return env;
    }

    public static IOutputHandler getAssertConfigs(IPointHandler pointHandler,
                                                  IBadgeHandler badgeHandler,
                                                  IMilestoneHandler milestoneHandler,
                                                  IStatesHandler statesHandler) {
        return new AssertOutputHandler(badgeHandler, milestoneHandler, pointHandler, statesHandler);
    }

    public static IOutputHandler getAssertConfigs(IPointHandler pointHandler,
                                                  IBadgeHandler badgeHandler,
                                                  IMilestoneHandler milestoneHandler,
                                                  IChallengeHandler challengeHandler,
                                                  IStatesHandler statesHandler) {
        return new AssertOutputHandler(badgeHandler, milestoneHandler, pointHandler, challengeHandler,
                statesHandler);
    }

    public static List<FieldCalculator> getFields(String resourceId) throws IOException {
        return KpiParser.parse(TestUtils.loadResource(resourceId));
    }

    public static List<BadgeRule> getBadgeRules(String resourceId) throws IOException {
        return BadgeParser.parse(TestUtils.loadResource(resourceId));
    }

    public static List<PointRule> getPointRules(String resourceId) throws IOException {
        return PointParser.parse(TestUtils.loadResource(resourceId));
    }

    public static List<Milestone> getMilestoneRules(String resourceId) throws IOException {
        return MilestoneParser.parse(TestUtils.loadResource(resourceId));
    }

    public static List<OState> getStateRules(String resourceId) throws IOException {
        return OStateParser.parse(TestUtils.loadResource(resourceId));
    }
}
