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

package io.github.oasis.core.parser;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.nio.file.Paths;

/**
 * @author Isuru Weerarathna
 */
public class ClasspathParserContext extends AbstractParserContext {

    private static final String CLZ_PATH_SEPARATOR = "/";

    private final ClassLoader classLoader;

    public ClasspathParserContext(String initialClzLocation) {
        this(initialClzLocation, Thread.currentThread().getContextClassLoader());
    }

    public ClasspathParserContext(String initialClzLocation, ClassLoader classLoader) {
        super(initialClzLocation);
        this.classLoader = classLoader;
    }

    @Override
    public InputStream loadPath(String path) {
        return classLoader.getResourceAsStream(path);
    }

    @Override
    public String manipulateFullPath(String relativePath) {
        String currLocation = getCurrentLocation();
        String prefix = StringUtils.substringBeforeLast(currLocation, CLZ_PATH_SEPARATOR);
        if (prefix.equals(currLocation)) {
            return Paths.get(StringUtils.stripStart(relativePath, CLZ_PATH_SEPARATOR)).normalize().toString();
        } else {
            return Paths.get(prefix, CLZ_PATH_SEPARATOR + StringUtils.stripStart(relativePath, CLZ_PATH_SEPARATOR))
                    .normalize().toString();
        }
    }

}
