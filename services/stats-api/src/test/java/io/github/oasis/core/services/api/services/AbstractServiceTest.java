/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.services.ApiConstants;
import io.github.oasis.core.services.SerializationSupport;
import io.github.oasis.core.services.annotations.EngineDbPool;
import io.github.oasis.core.services.api.TestUtils;
import io.github.oasis.core.services.api.handlers.OasisErrorHandler;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractServiceTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    @EngineDbPool
    private Db dbPool;

    @Autowired
    protected SerializationSupport serializationSupport;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() throws IOException, SQLException {
        TestUtils.cleanRedisData(dbPool);
        TestUtils.truncateData(dataSource);
    }

    @AfterEach
    void tearDown() throws IOException {
        File file = new File("sample.db");
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    protected void sleepSafe(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            // ignored
        }
    }

    private ResultActions doCallWithBody(HttpMethod httpMethod, String pathUrl, Object body) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.request(httpMethod, pathUrl)
                .header(ApiConstants.APP_ID_HEADER, "root")
                .header(ApiConstants.APP_KEY_HEADER, "root")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        if (body != null) {
            requestBuilder.content(serializationSupport.serialize(body));
        }
        return mockMvc.perform(requestBuilder);
    }

    protected ResultActions doPost(String pathUrl, Object body) throws Exception {
        return doCallWithBody(HttpMethod.POST, pathUrl, body);
    }

    protected ResultActions doPut(String pathUrl, Object body) throws Exception {
        return doCallWithBody(HttpMethod.PUT, pathUrl, body);
    }

    protected ResultActions doPatch(String pathUrl, Object body) throws Exception {
        return doCallWithBody(HttpMethod.PATCH, pathUrl, body);
    }

    protected ResultActions doGet(String pathUrl) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(pathUrl)
                .header(ApiConstants.APP_ID_HEADER, "root")
                .header(ApiConstants.APP_KEY_HEADER, "root")
                .accept(MediaType.APPLICATION_JSON)
        );
    }

    protected ResultActions doGet(String pathUrl, Object... pathParams) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(pathUrl, pathParams)
                .header(ApiConstants.APP_ID_HEADER, "root")
                .header(ApiConstants.APP_KEY_HEADER, "root")
                .accept(MediaType.APPLICATION_JSON);
        return mockMvc.perform(requestBuilder);
    }

    protected ResultActions doDelete(String pathUrl) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.delete(pathUrl)
                .header(ApiConstants.APP_ID_HEADER, "root")
                .header(ApiConstants.APP_KEY_HEADER, "root")
                .accept(MediaType.APPLICATION_JSON)
        );
    }

    protected <T> T doDeleteSuccess(String pathUrl, Class<T> clz) {
        try {
            MvcResult mvcResult = doDelete(pathUrl).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
            if (clz != null) {
                return serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), clz);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected <T> T doGetSuccess(String pathUrl, Class<T> clz) {
        try {
            MvcResult mvcResult = doGet(pathUrl).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
            return serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), clz);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected <T> List<T> doGetListSuccess(String pathUrl, Class<T> clz, Object... params) {
        try {
            MvcResult mvcResult = doGet(pathUrl, params).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
            return serializationSupport.deserializeList(mvcResult.getResponse().getContentAsString(), clz);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> PaginatedResult<T> doGetPaginatedSuccess(String pathUrl, Class<T> clz, Object... params) {
        try {
            MvcResult mvcResult = doGet(pathUrl, params).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();

            return serializationSupport.deserializeParameterized(mvcResult.getResponse().getContentAsString(),
                    PaginatedResult.class,
                    clz);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void doGetListError(String pathUrl, HttpStatus status, String errorCode, Object... params) {
        try {
            MvcResult mvcResult = doGet(pathUrl, params).andExpect(MockMvcResultMatchers.status().is(status.value())).andReturn();
            OasisErrorHandler.ErrorObject errorObject = serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), OasisErrorHandler.ErrorObject.class);
            Assertions.assertEquals(errorCode, errorObject.getErrorCode());
            Assertions.assertNotNull(errorObject.getTimestamp());

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void doGetError(String pathUrl, HttpStatus status, String errorCode) {
        try {
            MvcResult mvcResult = doGet(pathUrl).andExpect(MockMvcResultMatchers.status().is(status.value())).andReturn();
            OasisErrorHandler.ErrorObject errorObject = serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), OasisErrorHandler.ErrorObject.class);
            if (errorCode != null) {
                Assertions.assertEquals(errorCode, errorObject.getErrorCode());
            }
            Assertions.assertNotNull(errorObject.getTimestamp());

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void doDeletetError(String pathUrl, HttpStatus status, String errorCode) {
        try {
            MvcResult mvcResult = doDelete(pathUrl).andExpect(MockMvcResultMatchers.status().is(status.value())).andReturn();
            OasisErrorHandler.ErrorObject errorObject = serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), OasisErrorHandler.ErrorObject.class);
            Assertions.assertEquals(errorCode, errorObject.getErrorCode());
            Assertions.assertNotNull(errorObject.getTimestamp());

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void doPostError(String pathUrl, Object body, HttpStatus status, String errorCode) {
        try {
            MvcResult mvcResult = doPost(pathUrl, body).andExpect(MockMvcResultMatchers.status().is(status.value())).andReturn();
            OasisErrorHandler.ErrorObject errorObject = serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), OasisErrorHandler.ErrorObject.class);
            Assertions.assertEquals(errorCode, errorObject.getErrorCode());
            Assertions.assertNotNull(errorObject.getTimestamp());

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void doPutError(String pathUrl, Object body, HttpStatus status, String errorCode) {
        try {
            MvcResult mvcResult = doPut(pathUrl, body).andExpect(MockMvcResultMatchers.status().is(status.value())).andReturn();
            if (errorCode != null) {
                OasisErrorHandler.ErrorObject errorObject = serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), OasisErrorHandler.ErrorObject.class);
                Assertions.assertEquals(errorCode, errorObject.getErrorCode());
                Assertions.assertNotNull(errorObject.getTimestamp());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void doPatchError(String pathUrl, Object body, HttpStatus status, String errorCode) {
        try {
            MvcResult mvcResult = doPatch(pathUrl, body).andExpect(MockMvcResultMatchers.status().is(status.value())).andReturn();
            OasisErrorHandler.ErrorObject errorObject = serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), OasisErrorHandler.ErrorObject.class);
            Assertions.assertEquals(errorCode, errorObject.getErrorCode());
            Assertions.assertNotNull(errorObject.getTimestamp());

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected <T> T doPostSuccess(String pathUrl, Object body, Class<T> clz) {
        try {
            MvcResult mvcResult = doPost(pathUrl, body).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
            if (clz != null) {
                return serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), clz);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected <T> T doPutSuccess(String pathUrl, Object body, Class<T> clz) {
        try {
            MvcResult mvcResult = doPut(pathUrl, body).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
            if (clz != null) {
                return serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), clz);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected <T> T doPatchSuccess(String pathUrl, Object body, Class<T> clz) {
        try {
            MvcResult mvcResult = doPatch(pathUrl, body).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
            if (clz != null) {
                return serializationSupport.deserialize(mvcResult.getResponse().getContentAsString(), clz);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
