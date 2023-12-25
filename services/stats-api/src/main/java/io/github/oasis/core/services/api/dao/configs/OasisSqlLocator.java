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

package io.github.oasis.core.services.api.dao.configs;

import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.locator.ClasspathSqlLocator;
import org.jdbi.v3.core.locator.internal.ClasspathBuilder;
import org.jdbi.v3.sqlobject.internal.SqlAnnotations;
import org.jdbi.v3.sqlobject.locator.SqlLocator;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author Isuru Weerarathna
 */
public class OasisSqlLocator implements SqlLocator {

    private final ClasspathSqlLocator sqlLocator = ClasspathSqlLocator.create();

    @Override
    public String locate(Class<?> sqlObjectType, Method method, ConfigRegistry config) {
        String path = config.get(SqlLocationConfigs.class).getSqlScriptPath();
        ClasspathBuilder clzBuilder = new ClasspathBuilder().appendVerbatim(path);
        Function<String, String> transformation = value -> value.isEmpty() ?
                this.loadSql(sqlObjectType, method, clzBuilder) : value;

        return SqlAnnotations.getAnnotationValue(method, transformation)
                .orElseThrow(() -> new IllegalStateException(String.format("method %s has no query annotations", method)));
    }

    private String loadSql(Class<?> sqlObjectType, Method method, ClasspathBuilder clzBuilder) {
        // first check for class level annotation with a value
        //
        UseOasisSqlLocator annotation = sqlObjectType.getAnnotation(UseOasisSqlLocator.class);
        if (annotation != null && StringUtils.isNotBlank(annotation.value())) {
            clzBuilder.appendVerbatim(annotation.value());
        }

        // then check for method level annotation with a value
        //
        annotation = method.getAnnotation(UseOasisSqlLocator.class);
        if (annotation != null && StringUtils.isNotBlank(annotation.value())) {
            clzBuilder.appendVerbatim(annotation.value());
        } else {
            clzBuilder.appendVerbatim(method.getName());
        }
        String fullPath = clzBuilder.setExtension("sql").build();
        return sqlLocator.getResource(Thread.currentThread().getContextClassLoader(), fullPath);
    }
}