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
 */

package io.github.oasis.core.utils;

import io.github.oasis.core.annotations.DefinitionDetails;
import io.github.oasis.core.elements.SpecAttributeSchema;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public final class ReflectionUtils {

    private static final Map<String, String> TYPE_MAP;
    private static final String OBJECT_TYPE = "object";

    static {
        TYPE_MAP = new HashMap<>();
        TYPE_MAP.put("int", "number");
        TYPE_MAP.put("byte", "number");
        TYPE_MAP.put("long", "number");
        TYPE_MAP.put("short", "number");
        TYPE_MAP.put("boolean", "boolean");
        TYPE_MAP.put("double", "decimal");
        TYPE_MAP.put("float", "decimal");
        TYPE_MAP.put(Boolean.class.getName(), "boolean");
        TYPE_MAP.put(String.class.getName(), "string");
        TYPE_MAP.put(Integer.class.getName(), "number");
        TYPE_MAP.put(Long.class.getName(), "number");
        TYPE_MAP.put(Short.class.getName(), "number");
        TYPE_MAP.put(Byte.class.getName(), "number");
        TYPE_MAP.put(Double.class.getName(), "decimal");
        TYPE_MAP.put(Float.class.getName(), "decimal");
        TYPE_MAP.put(BigDecimal.class.getName(), "decimal");
        TYPE_MAP.put(BigInteger.class.getName(), "number");
        TYPE_MAP.put(Object.class.getName(), "any");
    }

    public static List<SpecAttributeSchema> describeClass(Class<?> clz) {
        if (TYPE_MAP.containsKey(clz.getName())) {
            return List.of();
        }

        List<SpecAttributeSchema> list = new ArrayList<>();
        for (Class<?> superClz : ClassUtils.getAllSuperclasses(clz)) {
            List<SpecAttributeSchema> results = describeClass(superClz);
            list.addAll(results);
        }


        for (Field declaredField : clz.getDeclaredFields()) {
            if (isFieldInvalid(declaredField)) {
                continue;
            }

            var spec = new SpecAttributeSchema()
                    .setName(declaredField.getName());

            var fieldType = declaredField.getType();
            var typeStr = TYPE_MAP.get(fieldType.getName());

            var definitionDetails = declaredField.getAnnotation(DefinitionDetails.class);

            if (fieldType.isEnum()) {
                spec.setType("enum");
                spec.setValueSet(Arrays.stream(fieldType.getEnumConstants())
                        .map(String::valueOf).collect(Collectors.toList()));
            } else if (fieldType.isArray()) {
                handleArrayType(spec, fieldType);
            } else if (isCollectionType(fieldType)) {
                handleCollectionType(spec, definitionDetails);
            } else {
                if (typeStr == null) {
                    spec.setType(OBJECT_TYPE);
                    spec.setChildren(describeClass(fieldType));
                } else {
                    spec.setType(typeStr);
                }
            }

            appendDefinitionDetailsOverrides(spec, definitionDetails);

            list.add(spec);
        }
        return list;
    }

    private static boolean isFieldInvalid(Field field) {
        return (field.getName().startsWith("_") || Modifier.isStatic(field.getModifiers()));
    }

    private static void appendDefinitionDetailsOverrides(SpecAttributeSchema spec, DefinitionDetails definitionDetails) {
        if (definitionDetails != null) {
            spec.setDescription(StringUtils.trimToNull(definitionDetails.description()));
            if (ArrayUtils.isNotEmpty(definitionDetails.valueSet())) {
                spec.setValueSet(List.of(definitionDetails.valueSet()));
            }
            if (ArrayUtils.isNotEmpty(definitionDetails.possibleTypes())) {
                spec.setType(StringUtils.join(definitionDetails.possibleTypes(), " | "));
            }
        }
    }

    private static void handleArrayType(SpecAttributeSchema spec, Class<?> fieldType) {
        spec.setType("list");
        spec.setValueType(TYPE_MAP.getOrDefault(fieldType.getComponentType().getName(), OBJECT_TYPE));
        if (OBJECT_TYPE.equals(spec.getValueType())) {
            spec.setChildren(describeClass(fieldType.getComponentType()));
        }
    }

    private static void handleCollectionType(SpecAttributeSchema spec, DefinitionDetails definitionDetails) {
        spec.setType("list");
        if (definitionDetails != null) {
            spec.setValueType(TYPE_MAP.getOrDefault(definitionDetails.parameterizedType().getName(), OBJECT_TYPE));
            if (OBJECT_TYPE.equals(spec.getValueType())) {
                spec.setChildren(describeClass(definitionDetails.parameterizedType()));
            }

        }
    }

    private static boolean isCollectionType(Class<?> fieldType) {
        return ClassUtils.isAssignable(fieldType, List.class)
                || ClassUtils.isAssignable(fieldType, Set.class);
    }

    private ReflectionUtils() {}

}
