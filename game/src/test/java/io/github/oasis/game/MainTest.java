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

package io.github.oasis.game;

import io.github.oasis.game.process.sources.CsvEventSource;
import io.github.oasis.model.Event;
import io.github.oasis.model.configs.ConfigKeys;
import io.github.oasis.model.configs.Configs;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class MainTest {

    @Test
    public void testCreateSource() throws FileNotFoundException {
        {
            Properties properties = new Properties();
            properties.put(ConfigKeys.KEY_SOURCE_TYPE, "file");
            properties.put(ConfigKeys.KEY_SOURCE_FILE, "../non/existing/file");
            try {
                Main.createSource(Configs.from(properties));
                Assertions.fail("File source creation should fail when source does not exist!");
            } catch (FileNotFoundException e) {
                // ok
            }
        }
        {
            Properties properties = new Properties();
            properties.put(ConfigKeys.KEY_SOURCE_TYPE, "file");
            properties.put(ConfigKeys.KEY_SOURCE_FILE, "../scripts/examples/input.csv");

            SourceFunction<Event> source = Main.createSource(Configs.from(properties));
            Assertions.assertTrue(source instanceof CsvEventSource);
        }
    }

    @Test
    public void testDbPropertiesCreation() throws Exception {
//        File scriptsDir = deriveScriptsDir();
//
//        Properties properties = new Properties();
//        properties.put(Constants.KEY_JDBC_INSTANCE, "testing");
//        properties.put(Constants.KEY_JDBC_URL, "jdbc:mysql://localhost/oasis");
//        properties.put(Constants.KEY_JDBC_USERNAME, "isuru");
//        properties.put(Constants.KEY_JDBC_PASSWORD, "isuru");
//        properties.put(Constants.KEY_DB_SCRIPTS_DIR, scriptsDir.getAbsolutePath());
//
//        {
//            DbProperties configs = Main.createConfigs(Configs.from(properties));
//            Assertions.assertNotNull(configs);
//            Assertions.assertEquals(configs.getDaoName(), "testing");
//            Assertions.assertEquals(configs.getUsername(), "isuru");
//            Assertions.assertEquals(configs.getPassword(), "isuru");
//            Assertions.assertEquals(configs.getUrl(), "jdbc:mysql://localhost/oasis");
//            Assertions.assertEquals(configs.getQueryLocation(), scriptsDir.getAbsolutePath());
//        }
//        {
//            properties.remove(Constants.KEY_JDBC_PASSWORD);
//            DbProperties configs = Main.createConfigs(Configs.from(properties));
//            Assertions.assertNull(configs.getPassword());
//            properties.put(Constants.KEY_JDBC_PASSWORD, "");
//
//            configs = Main.createConfigs(Configs.from(properties));
//            Assertions.assertNotNull(configs.getPassword());
//            Assertions.assertEquals(configs.getPassword(), "");
//        }
//        {
//            properties.put(Constants.KEY_DB_SCRIPTS_DIR, "../hello/non/existing");
//            try {
//                Main.createConfigs(Configs.from(properties));
//                Assertions.fail("Non existing script dir should fail!");
//            } catch (FileNotFoundException ex) {
//                // ok
//            }
//        }
    }

    @Test
    public void testOutputDbCreation() throws Exception {
//        {
//            Properties properties = new Properties();
//            properties.put(Constants.KEY_JDBC_INSTANCE, "testing");
//            properties.put(Constants.KEY_OUTPUT_TYPE, "db");
//            properties.put("db.scripts.dir", "../scripts/db");
//            properties.put("jdbc.url", "jdbc:h2:./test");
//            properties.put("jdbc.username", "");
//            properties.put("jdbc.password", "");
//
//            OasisExecution execution = Main.createOutputHandler(Configs.from(properties), new OasisExecution());
//            Assertions.assertNotNull(execution);
//
//            IOutputHandler outputHandler = execution.getOutputHandler();
//            Assertions.assertNotNull(outputHandler);
//            Assertions.assertNull(execution.getKafkaSink());
//
//            Assertions.assertTrue(outputHandler instanceof DbOutputHandler);
//            DbOutputHandler dbOutputHandler = (DbOutputHandler) outputHandler;
//            Assertions.assertEquals(dbOutputHandler.getDbRef(), "testing");
//        }
//
//        Properties properties = new Properties();
//        properties.put(Constants.KEY_JDBC_INSTANCE, "testing");
//        properties.put(Constants.KEY_OUTPUT_TYPE, "db");
//        OasisChallengeExecution challengeExecution = Main.createOutputHandler(Configs.from(properties), new OasisChallengeExecution());
//        Assertions.assertNotNull(challengeExecution);
//
//        IOutputHandler outputHandler = challengeExecution.getOutputHandler();
//        Assertions.assertNotNull(outputHandler);
//        Assertions.assertNull(challengeExecution.getOutputSink());
//
//        Assertions.assertTrue(outputHandler instanceof DbOutputHandler);
//        DbOutputHandler dbOutputHandler = (DbOutputHandler) outputHandler;
//        Assertions.assertEquals(dbOutputHandler.getDbRef(), "testing");
    }

    private File deriveScriptsDir() throws IOException {
        File dir = new File("./scripts/db");
        if (dir.exists()) {
            return dir.getCanonicalFile();
        } else {
            dir = new File("../scripts/db");
            if (dir.exists()) {
                return dir.getCanonicalFile();
            }
        }
        throw new RuntimeException("Scripts dir cannot be found!");
    }
}
