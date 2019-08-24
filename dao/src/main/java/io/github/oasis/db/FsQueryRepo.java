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

package io.github.oasis.db;

import io.github.oasis.model.db.DbProperties;
import io.github.oasis.model.db.IQueryRepo;
import io.github.oasis.model.db.ScriptNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class FsQueryRepo implements IQueryRepo {

    private static final Pattern JDBC_PATTERN = Pattern.compile("^(\\w+):(\\w+):.+");
    private static final String SQL = ".sql";

    private final Map<String, String> queries = new ConcurrentHashMap<>();

    private String DB = "";

    @Override
    public void init(DbProperties dbProperties) throws Exception {
        // initialize all scripts
        File folder = new File(dbProperties.getQueryLocation());
        if (!folder.exists()) {
            throw new FileNotFoundException("The given script folder does not exist!");
        }

        startScan(folder.getCanonicalFile().toPath());

        captureDbName(dbProperties.getUrl());
    }

    String captureDbName(String url) {
        DB = Utils.captureDbName(url);
        return DB;
    }

    void startScan(Path root) throws IOException {
        ScriptScanner scriptScanner = new ScriptScanner();
        Files.walkFileTree(root, scriptScanner);

        Set<Path> scriptFiles = scriptScanner.getScriptFiles();
        for (Path path : scriptFiles) {
            String content = Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .collect(Collectors.joining("\n"));
            String prefix = root.relativize(path).toString();
            queries.put(prefix, content);
        }
    }

    @Override
    public String fetchQuery(String queryId) throws ScriptNotFoundException {
        String script;
        if (queryId.endsWith(SQL)) {
            script = queries.get(queryId);
        } else {
            script = queries.getOrDefault(queryId + "." + DB + SQL, queries.get(queryId + SQL));
        }

        if (script == null) {
            throw new ScriptNotFoundException("Given script is not found in directory! [" + queryId + "]");
        }
        return script;
    }

    Map<String, String> getQueries() {
        return queries;
    }

    @Override
    public void close() {
        queries.clear();
    }

    private static class ScriptScanner extends SimpleFileVisitor<Path> {

        private final Set<Path> scriptFiles = new HashSet<>();

        private ScriptScanner() {
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (attrs.isRegularFile() && file.toString().endsWith(SQL)) {
                scriptFiles.add(file.normalize());
            }
            return FileVisitResult.CONTINUE;
        }

        private Set<Path> getScriptFiles() {
            return scriptFiles;
        }
    }
}
