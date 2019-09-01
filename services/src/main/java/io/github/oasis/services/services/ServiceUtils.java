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

package io.github.oasis.services.services;

import io.github.oasis.model.db.DbException;
import io.github.oasis.model.db.IOasisDao;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ServiceUtils {

    @SuppressWarnings("unchecked")
    static <T> List<T> toList(Iterable<T> src) {
        if (src instanceof List) {
            return (List)src;
        }
        List<T> results = new LinkedList<>();
        if (src != null) {
            for (T t : src) {
                results.add(t);
            }
        }
        return results;
    }

    static <T> T getTheOnlyRecord(IOasisDao dao, String queryId, Map<String, Object> data, Class<T> clz) throws DbException {
        Iterable<T> itUsers = dao.executeQuery(queryId, data, clz);
        Iterator<T> iterator = itUsers.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    static int orDefault(Integer val, int def) {
        return val == null ? def : val;
    }

    static long orDefault(Long val, long def) {
        return val == null ? def : val;
    }

    static boolean isValid(Boolean val) {
        return val != null;
    }

    static boolean isValid(Double val) {
        return val != null && val != Double.NaN;
    }

    static boolean isValid(Integer val) {
        return val != null && val > 0;
    }

    static boolean isValid(Long val) {
        return val != null && val > 0;
    }

}
