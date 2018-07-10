package io.github.isuru.oasis.process.windows;

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

}
