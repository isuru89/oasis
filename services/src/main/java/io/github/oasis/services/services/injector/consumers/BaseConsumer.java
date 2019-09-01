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

package io.github.oasis.services.services.injector.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.model.db.DbException;
import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.services.services.injector.ConsumerContext;
import io.github.oasis.services.services.injector.IConsumer;
import io.github.oasis.services.services.injector.MsgAcknowledger;
import io.github.oasis.services.utils.BufferedRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public abstract class BaseConsumer<T> implements IConsumer<T>, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(BaseConsumer.class);

    public static final ObjectMapper MAPPER = new ObjectMapper();

    protected IOasisDao dao;
    private Class<T> clz;
    ConsumerContext contextInfo;
    protected MsgAcknowledger acknowledger;

    private final BufferedRecords buffer;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     */
    BaseConsumer(IOasisDao dao, Class<T> clz, ConsumerContext context, MsgAcknowledger acknowledger) {
        this(dao, clz, context, true, acknowledger);
    }

    BaseConsumer(IOasisDao dao, Class<T> clz, ConsumerContext context, boolean buffered, MsgAcknowledger acknowledger) {
        this.dao = dao;
        this.clz = clz;
        this.contextInfo = context;
        this.acknowledger = acknowledger;

        if (buffered) {
            this.buffer = new BufferedRecords(this::pumpRecordsToDb);
            this.buffer.init(contextInfo.getPool());
        } else {
            this.buffer = null;
        }
    }

    @Override
    public void handleMessage(byte[] body, Object deliveryTag) {
        try {
            T message = MAPPER.readValue(body, clz);
            Map<String, Object> serializedData = handle(message);
            BufferedRecords.ElementRecord record = new BufferedRecords.ElementRecord(serializedData,
                    (long) deliveryTag);
            buffer.push(record);
            contextInfo.getInterceptor().accept(message);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public abstract Map<String, Object> handle(T msg);

    private void pumpRecordsToDb(List<BufferedRecords.ElementRecord> recordList) {
        flushRecords(recordList, getInsertScriptName());
    }

    void flushNow() {
        if (buffer != null) {
            buffer.flushNow();
        }
    }

    void flushRecords(List<BufferedRecords.ElementRecord> recordList,
                      String scriptName) {
        try {
            List<Map<String, Object>> maps = recordList.stream()
                    .map(BufferedRecords.ElementRecord::getData)
                    .collect(Collectors.toList());
            dao.executeBatchInsert(scriptName, maps);

            List<Long> tags = recordList.stream()
                    .map(BufferedRecords.ElementRecord::getDeliveryTag)
                    .collect(Collectors.toList());
            for (long tag : tags) {
                acknowledger.ack(tag);
            }
            LOG.debug("{} message ack completed for #{} messages.", clz.getSimpleName(), tags.size());

        } catch (DbException e) {
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error("Error while sending ack for {} messages!", clz.getSimpleName(), e);
        }
    }

    public abstract String getInsertScriptName();

    @Override
    public void close() {
        if (buffer != null) {
            buffer.close();
        }
    }
}
