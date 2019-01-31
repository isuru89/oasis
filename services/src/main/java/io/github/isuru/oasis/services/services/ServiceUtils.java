package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.db.IOasisDao;

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
