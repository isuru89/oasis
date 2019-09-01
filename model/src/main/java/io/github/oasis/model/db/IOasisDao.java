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

package io.github.oasis.model.db;

import io.github.oasis.model.utils.ConsumerEx;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IOasisDao extends AutoCloseable {

    void init(DbProperties properties) throws Exception;

    String getDbType();

    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data,
                                 Class<T> clz, Map<String, Object> templatingData) throws DbException;
    Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws DbException;
    Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data,
                                               Map<String, Object> templatingData) throws DbException;
    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws DbException;
    long executeCommand(String queryId, Map<String, Object> data) throws DbException;
    long executeCommand(String queryId, Map<String, Object> data, Map<String, Object> templatingData) throws DbException;
    long executeRawCommand(String queryStr, Map<String, Object> data) throws DbException;
    Iterable<Map<String, Object>> executeRawQuery(String queryStr, Map<String, Object> data) throws DbException;
    List<Integer> executeBatchInsert(String queryId, List<Map<String, Object>> batchData) throws DbException;
    Long executeInsert(String queryId, Map<String, Object> data, String keyColumn) throws DbException;
    Long executeInsert(String queryId, Map<String, Object> data, Map<String, Object> templatingData, String keyColumn) throws DbException;
    Object runTx(int transactionLevel, ConsumerEx<JdbcTransactionCtx> txBody) throws DbException;
    Object runTx(ConsumerEx<JdbcTransactionCtx> txBody) throws DbException;

    IDefinitionDao getDefinitionDao();

}
