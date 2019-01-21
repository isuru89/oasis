package io.github.isuru.oasis.game;

import io.github.isuru.oasis.game.utils.HistogramCounter;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.game.utils.TestMapState;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

class UtilsTest {

    private static final long ONE_MONTH = 1000 * 3600L * 24L * 30L;
    private static final long ONE_DAY = 1000 * 3600 * 24;
    private static final long ONE_HOUR = 1000 * 3600;
    private static final long ONE_MIN = 1000 * 60;
    private static final long ONE_SEC = 1000;

    @Test
    void testHistogramCounterCts() throws Exception {
        Predicate<LocalDate> noHoliday = localDate -> false;
        Predicate<LocalDate> weekdaysOnly = localDate -> localDate.getDayOfWeek() == DayOfWeek.SUNDAY
                || localDate.getDayOfWeek() == DayOfWeek.SATURDAY;

        Map<String, Integer> data = new HashMap<>();
        data.put("2018-12-20", 1);
        data.put("2018-12-21", 1);
        data.put("2018-12-22", 1);
        data.put("2018-12-23", 1);
        data.put("2018-12-24", 1);
        data.put("2018-12-25", 1);
        data.put("2018-12-26", 1);
        data.put("2018-12-27", 1);
        data.put("2018-12-28", 1);
        data.put("2018-12-29", 1);
        data.put("2018-12-30", 1);
        data.put("2018-12-31", 1);

        TestMapState<String, Integer> histogram = new TestMapState<>();
        histogram.putAll(data);

        Assertions.assertEquals(1, HistogramCounter.processContinuous("2018-12-20", histogram, noHoliday));
        Assertions.assertEquals(3, HistogramCounter.processContinuous("2018-12-22", histogram, noHoliday));
        Assertions.assertEquals(3, HistogramCounter.processContinuous("2018-12-22", histogram, weekdaysOnly));
        Assertions.assertEquals(3, HistogramCounter.processContinuous("2018-12-23", histogram, weekdaysOnly));
        Assertions.assertEquals(3, HistogramCounter.processContinuous("2018-12-24", histogram, weekdaysOnly));
        Assertions.assertEquals(4, HistogramCounter.processContinuous("2018-12-25", histogram, weekdaysOnly));
        Assertions.assertEquals(6, HistogramCounter.processContinuous("2018-12-25", histogram, noHoliday));
        Assertions.assertEquals(12, HistogramCounter.processContinuous("2018-12-31", histogram, noHoliday));
        Assertions.assertEquals(8, HistogramCounter.processContinuous("2018-12-31", histogram, weekdaysOnly));

        // non existing dates
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2019-01-01", histogram, weekdaysOnly));
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2019-01-01", histogram, noHoliday));
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2018-12-01", histogram, weekdaysOnly));
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2018-12-01", histogram, noHoliday));

        // clearings
        HistogramCounter.clearLessThan("2018-12-01", histogram);
        Assertions.assertEquals(12, histogram.size());

        HistogramCounter.clearLessThan("2018-12-22", histogram);
        Assertions.assertEquals(9, histogram.size());

        HistogramCounter.clearLessThan("2018-12-28", histogram);
        Assertions.assertEquals(3, histogram.size());

        HistogramCounter.clearLessThan("2019-01-22", histogram);
        Assertions.assertEquals(0, histogram.size());
    }

    @Test
    void testHistogramCounterMiss() throws Exception {
        Predicate<LocalDate> noHoliday = localDate -> false;
        Predicate<LocalDate> weekdaysOnly = localDate -> localDate.getDayOfWeek() == DayOfWeek.SUNDAY
                || localDate.getDayOfWeek() == DayOfWeek.SATURDAY;

        Map<String, Integer> data = new HashMap<>();
        data.put("2018-12-20", 1);
        data.put("2018-12-21", 1);
        data.put("2018-12-23", 1);
        data.put("2018-12-24", 1);
        data.put("2018-12-27", 1);
        data.put("2018-12-28", 1);
        data.put("2018-12-29", 1);
        data.put("2018-12-30", 1);
        data.put("2018-12-31", 1);

        TestMapState<String, Integer> histogram = new TestMapState<>();
        histogram.putAll(data);

        Assertions.assertEquals(1, HistogramCounter.processContinuous("2018-12-20", histogram, noHoliday));
        Assertions.assertEquals(1, HistogramCounter.processContinuous("2018-12-20", histogram, weekdaysOnly));
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2018-12-22", histogram, noHoliday));
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2018-12-22", histogram, weekdaysOnly));
        Assertions.assertEquals(3, HistogramCounter.processContinuous("2018-12-23", histogram, weekdaysOnly));
        Assertions.assertEquals(3, HistogramCounter.processContinuous("2018-12-24", histogram, weekdaysOnly));
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2018-12-25", histogram, weekdaysOnly));
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2018-12-25", histogram, noHoliday));
        Assertions.assertEquals(5, HistogramCounter.processContinuous("2018-12-31", histogram, noHoliday));
        Assertions.assertEquals(3, HistogramCounter.processContinuous("2018-12-31", histogram, weekdaysOnly));

        // non existing dates
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2019-01-01", histogram, weekdaysOnly));
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2019-01-01", histogram, noHoliday));
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2018-12-01", histogram, weekdaysOnly));
        Assertions.assertEquals(0, HistogramCounter.processContinuous("2018-12-01", histogram, noHoliday));

        // clearings
        HistogramCounter.clearLessThan("2018-12-01", histogram);
        Assertions.assertEquals(9, histogram.size());

        HistogramCounter.clearLessThan("2018-12-22", histogram);
        Assertions.assertEquals(7, histogram.size());

        HistogramCounter.clearLessThan("2018-12-28", histogram);
        Assertions.assertEquals(3, histogram.size());

        HistogramCounter.clearLessThan("2019-01-22", histogram);
        Assertions.assertEquals(0, histogram.size());
    }


    @Test
    void testNullEmpty() {
        Assertions.assertTrue(Utils.isNullOrEmpty(""));
        Assertions.assertTrue(Utils.isNullOrEmpty(" "));
        Assertions.assertTrue(Utils.isNullOrEmpty("     "));
        Assertions.assertTrue(Utils.isNullOrEmpty("    \n" +
                " "));

        Assertions.assertFalse(Utils.isNullOrEmpty("a"));
        Assertions.assertFalse(Utils.isNullOrEmpty("$a > 100"));
        Assertions.assertFalse(Utils.isNullOrEmpty("isuru"));
    }

    @Test
    void propTest() {
        {
            Properties props = new Properties();
            props.put("a.b.c", 1);
            props.put("a.b.f", 2);
            props.put("a.f", 2);

            Map<String, Object> map = OasisUtils.filterKeys(props, "a.b.");
            Assertions.assertEquals(2, map.size());
            Assertions.assertTrue(map.containsKey("c"));
            Assertions.assertTrue(map.containsKey("f"));
        }
        {
            Properties props = new Properties();
            props.put("a.b.c", 1);
            props.put("a.b.f", 2);
            props.put("a.f", 2);

            Map<String, Object> map = OasisUtils.filterKeys(props, "x.y.");
            Assertions.assertEquals(0, map.size());
        }
    }

    @Test
    void numTest() {
        double val = Utils.strNum("1");
        Assertions.assertEquals(val, 1.0);

        Assertions.assertEquals((double) Utils.strNum("0k"), 0.0);

        Assertions.assertEquals((double) Utils.strNum("0.5"), 0.5);
        Assertions.assertEquals((double) Utils.strNum("0.1"), 0.1);
        Assertions.assertEquals((double) Utils.strNum("5"), 5);
        Assertions.assertEquals((double) Utils.strNum("5000"), 5000);
        Assertions.assertEquals((double) Utils.strNum("5.097"), 5.097);

        Assertions.assertEquals((double) Utils.strNum("5k"), 5000);
        Assertions.assertEquals((double) Utils.strNum("5K"), 5000);
        Assertions.assertEquals((double) Utils.strNum("5 k"), 5000);
        Assertions.assertEquals((double) Utils.strNum("5 K"), 5000);
        Assertions.assertEquals((double) Utils.strNum(" 5 K  "), 5000);
        Assertions.assertEquals((double) Utils.strNum(" 5 kilo  "), 5000);
        Assertions.assertEquals((double) Utils.strNum("5.2k "), 5200);
        Assertions.assertEquals((double) Utils.strNum("5.256K"), 5256);

        Assertions.assertEquals((double) Utils.strNum("5m"), 5000000);
        Assertions.assertEquals((double) Utils.strNum("5M"), 5000000);
        Assertions.assertEquals((double) Utils.strNum("5 m"), 5000000);
        Assertions.assertEquals((double) Utils.strNum("5 m"), 5000000);
        Assertions.assertEquals((double) Utils.strNum(" 5 M  "), 5000000);
        Assertions.assertEquals((double) Utils.strNum(" 5 mil  "), 5000000);
        Assertions.assertEquals((double) Utils.strNum("5.2m "), 5200000);
        Assertions.assertEquals((double) Utils.strNum("5.256M"), 5256000);

        Assertions.assertEquals((double) Utils.strNum("5b"), 5000000000.0);
        Assertions.assertEquals((double) Utils.strNum("5B"), 5000000000.0);
        Assertions.assertEquals((double) Utils.strNum("5 b"), 5000000000.0);
        Assertions.assertEquals((double) Utils.strNum("5 b"), 5000000000.0);
        Assertions.assertEquals((double) Utils.strNum(" 5 B  "), 5000000000.0);
        Assertions.assertEquals((double) Utils.strNum(" 5 bil  "), 5000000000.0);
    }

    @Test
    void numStrFail() {
        try {
            //Utils.strNum("k3");
            //Assertions.fail();
        } catch (IllegalArgumentException e) {
            // do nothing
        }
    }

    @Test
    void timeStrDaysTest() {
        Time time = Utils.fromStr("1days");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY);

        time = Utils.fromStr("1 day");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY);

        time = Utils.fromStr("1 Day");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY);

        time = Utils.fromStr("7 days");
        Assertions.assertEquals(time.getSize(), 7);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY * 7);

        time = Utils.fromStr("1d");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY);

        time = Utils.fromStr("2ds");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY * 2);

        time = Utils.fromStr(" 2 d");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY * 2);

        time = Utils.fromStr(" 2 ds  ");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY * 2);

        time = Utils.fromStr("1businessdays");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY);

        time = Utils.fromStr("1 business days");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY);

        time = Utils.fromStr("1 bdays");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY);

        time = Utils.fromStr("1b");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_DAY);

        Assertions.assertTrue(Utils.isDurationBusinessDaysOnly("1businessdays"));
        Assertions.assertTrue(Utils.isDurationBusinessDaysOnly("3 business days"));
        Assertions.assertTrue(Utils.isDurationBusinessDaysOnly("1 businessdays"));
        Assertions.assertTrue(Utils.isDurationBusinessDaysOnly("1 bdays"));
        Assertions.assertTrue(Utils.isDurationBusinessDaysOnly("1 businessday"));
        Assertions.assertFalse(Utils.isDurationBusinessDaysOnly("1 day"));
        Assertions.assertFalse(Utils.isDurationBusinessDaysOnly("7 days"));
    }

    @Test
    void timeStrHoursTest() {
        Time time = Utils.fromStr("1hour");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_HOUR);

        time = Utils.fromStr("1 hour");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_HOUR);

        time = Utils.fromStr("24 hours");
        Assertions.assertEquals(time.getSize(), 24);
        Assertions.assertEquals(time.toMilliseconds(), ONE_HOUR * 24);

        time = Utils.fromStr("1h");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_HOUR);

        time = Utils.fromStr("1H");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_HOUR);

        time = Utils.fromStr("2hs");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_HOUR * 2);

        time = Utils.fromStr(" 2 hour");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_HOUR * 2);

        time = Utils.fromStr(" 2 hours  ");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_HOUR * 2);
    }

    @Test
    void timeStrMinutesTest() {
        Time time = Utils.fromStr("1minute");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MIN);

        time = Utils.fromStr("1 min");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MIN);

        time = Utils.fromStr("24 minutes");
        Assertions.assertEquals(time.getSize(), 24);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MIN * 24);

        time = Utils.fromStr("1min");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MIN);

        time = Utils.fromStr("1 mins");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MIN);

        time = Utils.fromStr("2mins");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MIN * 2);

        time = Utils.fromStr(" 2 min");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MIN * 2);

        time = Utils.fromStr(" 2 mins  ");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MIN * 2);
    }

    @Test
    void timeStrSecondsTest() {
        Time time = Utils.fromStr("1seconds");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_SEC);

        time = Utils.fromStr("1 sec");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_SEC);

        time = Utils.fromStr("24 seconds");
        Assertions.assertEquals(time.getSize(), 24);
        Assertions.assertEquals(time.toMilliseconds(), ONE_SEC * 24);

        time = Utils.fromStr("1s");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_SEC);

        time = Utils.fromStr("1 secs");
        Assertions.assertEquals(time.getSize(), 1);
        Assertions.assertEquals(time.toMilliseconds(), ONE_SEC);

        time = Utils.fromStr("2sec");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_SEC * 2);

        time = Utils.fromStr(" 2 s");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_SEC * 2);

        time = Utils.fromStr(" 2 secp  ");
        Assertions.assertEquals(time.getSize(), 2);
        Assertions.assertEquals(time.toMilliseconds(), ONE_SEC * 2);
    }

    @Test
    void timeStrMonthTest() {
        Time time = Utils.fromStr("1months");
        Assertions.assertEquals(time.getSize(), 30);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MONTH);

        time = Utils.fromStr("1 mon");
        Assertions.assertEquals(time.getSize(), 30);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MONTH);

        time = Utils.fromStr("24 months");
        Assertions.assertEquals(time.getSize(), 30 * 24);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MONTH * 24);

        time = Utils.fromStr("1m");
        Assertions.assertEquals(time.getSize(), 30);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MONTH);

        time = Utils.fromStr("1 mon");
        Assertions.assertEquals(time.getSize(), 30);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MONTH);

        time = Utils.fromStr("2M");
        Assertions.assertEquals(time.getSize(), 2 * 30);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MONTH * 2);

        time = Utils.fromStr(" 2 m");
        Assertions.assertEquals(time.getSize(), 2 * 30);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MONTH * 2);

        time = Utils.fromStr(" 2 month  ");
        Assertions.assertEquals(time.getSize(), 2 * 30);
        Assertions.assertEquals(time.toMilliseconds(), ONE_MONTH * 2);
    }

    @Test
    void timeStrFails() {
        try {
            Time time = Utils.fromStr("months");
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            // do nothing
        }
    }
}
