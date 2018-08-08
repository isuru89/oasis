package io.github.isuru.oasis.services.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.services.api.IOasisApiService;

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
    private final IOasisApiService apiService;

    BaseService(IOasisDao dao, IOasisApiService apiService) {
        this.dao = dao;
        this.apiService = apiService;
    }

    IOasisDao getDao() {
        return dao;
    }

    ObjectMapper getMapper() {
        return mapper;
    }

    IOasisApiService getApiService() {
        return apiService;
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

    @SuppressWarnings("unchecked")
    <T> List<T> toList(Iterable<T> src) {
        if (src instanceof List) {
            return (List)src;
        }
        List<T> results = new LinkedList<>();
        for (T t : src) {
            results.add(t);
        }
        return results;
    }

}
