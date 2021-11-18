/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.core.services.api;

import io.github.oasis.core.services.ApiConstants;
import io.github.oasis.core.services.SerializationSupport;
import io.github.oasis.core.services.api.services.AbstractServiceTest;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * @author Isuru Weerarathna
 */
@AutoConfigureMockMvc
public class OasisStatsApiWebTest extends AbstractServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SerializationSupport serializationSupport;

    @Test
    void test() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/games")
                        .header(ApiConstants.APP_ID_HEADER, "root")
                        .header(ApiConstants.APP_KEY_HEADER, "root")
                .content(serializationSupport.serialize(GameCreateRequest.builder().name("Game 1").description("aaa").build()))
                .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void test2() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/games")
                .header(ApiConstants.APP_ID_HEADER, "root")
                .header(ApiConstants.APP_KEY_HEADER, "root")
                .content(serializationSupport.serialize(GameCreateRequest.builder().name("Game 2").description("aaa").build()))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/games")
                        .header(ApiConstants.APP_ID_HEADER, "root")
                        .header(ApiConstants.APP_KEY_HEADER, "root")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        System.out.println(contentAsString);
    }
}
