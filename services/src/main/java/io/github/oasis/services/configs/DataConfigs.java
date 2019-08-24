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

package io.github.oasis.services.configs;

import io.github.oasis.db.OasisDbFactory;
import io.github.oasis.model.db.DbProperties;
import io.github.oasis.model.db.IOasisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataConfigs {

    @Autowired
    private OasisConfigurations oasisConfigurations;

    @Bean
    public IOasisDao createDao() throws Exception {
        DbProperties dbProps = createDbProps(oasisConfigurations.getDb());
        return OasisDbFactory.create(dbProps);
    }

    private DbProperties createDbProps(OasisConfigurations.DatabaseConfigurations dbConfigs) throws IOException {
        DbProperties properties = new DbProperties("default");
        properties.setUrl(dbConfigs.getUrl());
        properties.setUsername(dbConfigs.getUsername());
        properties.setPassword(dbConfigs.getPassword());

        File scriptsDir = new File(dbConfigs.getScriptsPath());
        if (scriptsDir.exists()) {
            properties.setQueryLocation(scriptsDir.getAbsolutePath());
        } else {
            throw new FileNotFoundException("The given scripts dir '" + scriptsDir.getAbsolutePath()
                    + "' does not exist!");
        }

        properties.setAutoSchema(dbConfigs.isAutoSchema());
        properties.setSchemaDir(dbConfigs.getSchemaDir());

        Map<String, Object> poolProps = new HashMap<>();
        poolProps.put("maximumPoolSize", dbConfigs.getMaximumPoolSize());
        properties.setOtherOptions(poolProps);
        return properties;
    }

}
