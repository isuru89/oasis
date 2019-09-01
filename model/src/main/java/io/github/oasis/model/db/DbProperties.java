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

import io.github.oasis.model.configs.ConfigKeys;
import io.github.oasis.model.configs.Configs;
import io.github.oasis.model.configs.EnvKeys;
import io.github.oasis.model.utils.OasisUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class DbProperties {

    private final String daoName;

    private String queryLocation;
    private String url;
    private String username;
    private String password;

    private boolean autoSchema = false;
    private String schemaDir;

    private Map<String, Object> otherOptions;

    public boolean isAutoSchema() {
        return autoSchema;
    }

    public void setAutoSchema(boolean autoSchema) {
        this.autoSchema = autoSchema;
    }

    public String getSchemaDir() {
        return schemaDir;
    }

    public void setSchemaDir(String schemaDir) {
        this.schemaDir = schemaDir;
    }

    public DbProperties(String daoName) {
        this.daoName = daoName;
    }

    public String getDaoName() {
        return daoName;
    }

    public String getQueryLocation() {
        return queryLocation;
    }

    public void setQueryLocation(String queryLocation) {
        this.queryLocation = queryLocation;
    }

    public Map<String, Object> getOtherOptions() {
        return otherOptions;
    }

    public void setOtherOptions(Map<String, Object> otherOptions) {
        this.otherOptions = otherOptions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static DbProperties fromProps(Configs configs) throws FileNotFoundException {
        String name = configs.getStr("oasis.db.name", "default");
        DbProperties dbProps = new DbProperties(name);
        String oasisJdbcUrl = System.getenv(EnvKeys.OASIS_JDBC_URL);
        if (oasisJdbcUrl != null && !oasisJdbcUrl.isEmpty()) {
            dbProps.setUrl(oasisJdbcUrl);
        } else {
            dbProps.setUrl(configs.getStrReq(ConfigKeys.KEY_JDBC_URL));
        }
        dbProps.setUsername(configs.getStrReq(ConfigKeys.KEY_JDBC_USERNAME));
        dbProps.setPassword(configs.getStrReq(ConfigKeys.KEY_JDBC_PASSWORD));
        File scriptsDir = new File(configs.getStrReq(ConfigKeys.KEY_JDBC_SCRIPTS_PATH));
        if (scriptsDir.exists()) {
            dbProps.setQueryLocation(scriptsDir.getAbsolutePath());
        } else {
            throw new FileNotFoundException("The given scripts dir '" + scriptsDir.getAbsolutePath()
                    + "' does not exist!");
        }

        dbProps.setAutoSchema(configs.getBool(ConfigKeys.KEY_JDBC_AUTO_SCHEMA, false));
        dbProps.setSchemaDir(configs.getStr(ConfigKeys.KEY_JDBC_SCHEMA_DIR, "./scripts"));
        if (dbProps.isAutoSchema() && !new File(dbProps.getSchemaDir()).exists()) {
            throw new FileNotFoundException("The given schema dir does not exist '"
                + dbProps.getSchemaDir() + "'!");
        }

        Map<String, Object> map = OasisUtils.filterKeys(configs.getProps(), "oasis.db.pool.");
        if (map != null && !map.isEmpty()) {
            dbProps.setOtherOptions(map);
        }
        return dbProps;
    }
}
