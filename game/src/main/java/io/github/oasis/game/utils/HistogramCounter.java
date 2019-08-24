/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.game.utils;

import org.apache.flink.api.common.state.MapState;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
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

    public static int processSeparate(String key, MapState<String, Integer> mapState,
                                      Predicate<LocalDate> isHoliday) throws Exception {
        int p = 0;
        for (Map.Entry<String, Integer> entry : mapState.entries()) {
            Integer count = entry.getValue();
            if (count == null || count <= 0) {
                continue;
            }
            p++;
        }
        return p;
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
