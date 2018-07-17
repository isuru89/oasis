package io.github.isuru.oasis.services.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.db.IOasisDao;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
abstract class BaseService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final IOasisDao dao;

    BaseService(IOasisDao dao) {
        this.dao = dao;
    }

    IOasisDao getDao() {
        return dao;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    <T> List<T> getAsList(String queryId, Map<String, Object> data, Class<T> clz) throws Exception {
        Iterable<T> itUsers = getDao().executeQuery(queryId, data, clz);
        Iterator<T> iterator = itUsers.iterator();

        List<T> items = new LinkedList<>();
        while (iterator.hasNext()) {
            items.add(iterator.next());
        }
        return items;
    }

    <T> T getTheOnlyRecord(String queryId, Map<String, Object> data, Class<T> clz) throws Exception {
        Iterable<T> itUsers = getDao().executeQuery(queryId, data, clz);
        Iterator<T> iterator = itUsers.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }
}
