package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.game.parser.BadgeParser;
import io.github.isuru.oasis.game.parser.FieldCalculationParser;
import io.github.isuru.oasis.game.parser.MilestoneParser;
import io.github.isuru.oasis.game.parser.PointParser;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class TestUtils {


    public static List<Tuple5<Long, String, String, Double, Long>> parsePointOutput(String file) throws IOException {
        try (InputStream inputStream = TestUtils.loadResource(file)) {
            LineIterator lineIterator = IOUtils.lineIterator(inputStream, StandardCharsets.UTF_8);
            List<Tuple5<Long, String, String, Double, Long>> list = new LinkedList<>();
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("#")) continue;

                String[] parts = line.split("[,]");

                Tuple5<Long, String, String, Double, Long> row = Tuple5.of(
                        Long.parseLong(parts[0]),
                        parts[1],
                        parts[2],
                        Double.parseDouble(parts[3]),
                        Long.parseLong(parts[4])
                );
                list.add(row);
            }
            return list;
        }
    }

    public static List<Tuple4<Long, String, Integer, Long>> parseMilestoneOutput(String file) throws IOException {
        try (InputStream inputStream = TestUtils.loadResource(file)) {
            LineIterator lineIterator = IOUtils.lineIterator(inputStream, StandardCharsets.UTF_8);
            List<Tuple4<Long, String, Integer, Long>> list = new LinkedList<>();
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("#")) continue;
                String[] parts = line.split("[,]");

                Tuple4<Long, String, Integer, Long> row = Tuple4.of(
                        Long.parseLong(parts[0]),
                        parts[1],
                        Integer.parseInt(parts[2]),
                        Long.parseLong(parts[3])
                );
                list.add(row);
            }
            return list;
        }
    }

    public static List<Tuple5<Long, String, String, Long, Long>> parseBadgesOutput(String file) throws IOException {
        try (InputStream inputStream = TestUtils.loadResource(file)) {
            LineIterator lineIterator = IOUtils.lineIterator(inputStream, StandardCharsets.UTF_8);
            List<Tuple5<Long, String, String, Long, Long>> list = new LinkedList<>();
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("#")) continue;
                String[] parts = line.split("[,]");

                Tuple5<Long, String, String, Long, Long> row = Tuple5.of(
                        Long.parseLong(parts[0]),
                        parts[1],
                        parts[2],
                        Long.parseLong(parts[3]),
                        Long.parseLong(parts[4])
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
                                                  IMilestoneHandler milestoneHandler) {
        return new AssertOutputHandler(badgeHandler, milestoneHandler, pointHandler);
    }

    public static List<FieldCalculator> getFields(String resourceId) throws IOException {
        return FieldCalculationParser.parse(TestUtils.loadResource(resourceId));
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
}
