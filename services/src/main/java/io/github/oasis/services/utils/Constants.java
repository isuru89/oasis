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

package io.github.oasis.services.utils;

/**
 * @author iweerarathna
 */
public final class Constants {

    public static final String RUN_ARGS_FORMAT = "--configs \"%s\"";

    public static final int DEF_PARALLELISM = 2;

    public static final String DEF_WORKSPACE_DIR = "./ws";

    public static final String ARTIFACTS_SUB_DIR = "artifacts";
    public static final String ALL_EXECUTIONS_DIR = "exec";
    public static final String SAVEPOINT_DIR = "sp";

    public static final String GAME_RULES_FILE = "rules.yml";
    public static final String GAME_JAR = "game.jar";

    public static final String DEF_LOCATION_RUN_TEMPLATE = "./configs/template/run.properties";


}
