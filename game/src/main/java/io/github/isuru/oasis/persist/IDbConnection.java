package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.OasisConfigurations;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IDbConnection extends Serializable {

    IDbConnection init(OasisConfigurations configurations) throws Exception;

    int executeCommand(String command, Map<String, Object> params) throws Exception;

    List<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws Exception;

    List<Map<String, Object>> runQueryStr(String queryStr, Map<String, Object> params) throws Exception;

    void shutdown();
}
