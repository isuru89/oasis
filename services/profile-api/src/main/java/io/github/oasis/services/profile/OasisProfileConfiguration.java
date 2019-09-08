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

package io.github.oasis.services.profile;

import io.github.oasis.services.profile.internal.dao.ITeamDao;
import io.github.oasis.services.profile.internal.dao.IUserDao;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Isuru Weerarathna
 */
@Configuration
public class OasisProfileConfiguration {

    @Autowired private Jdbi jdbi;

    @Bean
    public ITeamDao getTeamDao() {
        return jdbi.onDemand(ITeamDao.class);
    }

    @Bean
    public IUserDao getUserDao() {
        return jdbi.onDemand(IUserDao.class);
    }

}
