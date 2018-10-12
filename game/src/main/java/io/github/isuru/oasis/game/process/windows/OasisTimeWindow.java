package io.github.isuru.oasis.game.process.windows;

/**
 * @author iweerarathna
 */
public class OasisTimeWindow {

    public static OasisTimeWindowAssigner WEEKLY() {
        return new WeeklyEventTimeWindow();
    }

    public static OasisTimeWindowAssigner MONTHLY() {
        return new MonthlyEventTimeWindow();
    }

    public static OasisTimeWindowAssigner DAILY() {
        return new DailyEventTimeWindow();
    }
}
