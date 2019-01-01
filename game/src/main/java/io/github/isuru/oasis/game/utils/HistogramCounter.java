package io.github.isuru.oasis.game.utils;

import org.apache.flink.api.common.state.MapState;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * @author iweerarathna
 */
public class HistogramCounter {

    public static int processContinuous(String key, MapState<String, Integer> mapState,
                                        Predicate<LocalDate> isHoliday) throws Exception {
        LocalDate currDate = LocalDate.parse(key);
        int p = 0;
        while (true) {
            Integer count = mapState.get(currDate.toString());
            if (count == null || count <= 0) {
                return p;
            }
            p++;
            currDate = currDate.minusDays(1);
            while (isHoliday.test(currDate)) {
                currDate = currDate.minusDays(1);
            }
        }
    }

    public static void clearLessThan(String key, MapState<String, Integer> mapState) throws Exception {
        Iterator<String> keys = mapState.keys().iterator();
        LinkedList<String> tmp = new LinkedList<>();
        while (keys.hasNext()) {
            String nk = keys.next();
            if (key.compareTo(nk) >= 0) {
                tmp.add(nk);
            }
        }

        for (String k : tmp) {
            mapState.remove(k);
        }
    }


}
