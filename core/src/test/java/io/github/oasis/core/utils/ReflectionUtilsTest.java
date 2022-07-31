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
import io.github.oasis.core.elements.spec.AwardDef;
import io.github.oasis.core.elements.spec.TimeUnitDef;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReflectionUtilsTest {

    private void assertAttrByName(String attrName, List<SpecAttributeSchema> spec, SpecAttributeSchema check) {
        var attr = spec.stream().filter(s -> s.getName().equals(attrName)).findFirst().orElseThrow();
        System.out.println(attrName + " => " + attr);
        assertTwoAttr(attr, check);
    }

    private void assertTwoAttr(SpecAttributeSchema source, SpecAttributeSchema check) {
        Assertions.assertEquals(check.getName(), source.getName());
        Assertions.assertEquals(check.getDescription(), source.getDescription());
        Assertions.assertEquals(check.getType(), source.getType());
        Assertions.assertEquals(check.getValueSet(), source.getValueSet());
        Assertions.assertEquals(check.getValueType(), source.getValueType());

        if (source.getChildren() == null ^ check.getChildren() == null) {
            Assertions.fail("Expected children schema is not available!");
        } else if (source.getChildren() != null && source.getChildren() != null) {
            Assertions.assertEquals(check.getChildren().size(), source.getChildren().size());
            assertAttrCollection(source.getChildren(), check.getChildren());
        }
    }

    private void assertAttrCollection(List<SpecAttributeSchema> source, List<SpecAttributeSchema> check) {
        for (SpecAttributeSchema specAttributeSchema : check) {
            assertAttrByName(specAttributeSchema.getName(), source, specAttributeSchema);
        }

    }

    @Test
    void shouldDescribePrimitivesCorrectly() {
        var result = ReflectionUtils.describeClass(PrimitiveSchema.class);
        System.out.println(result);
        assertAttrByName("pint", result, new SpecAttributeSchema().setName("pint").setType("number"));
        assertAttrByName("plong", result, new SpecAttributeSchema().setName("plong").setType("number"));
        assertAttrByName("pbyte", result, new SpecAttributeSchema().setName("pbyte").setType("number"));
        assertAttrByName("pshort", result, new SpecAttributeSchema().setName("pshort").setType("number"));
        assertAttrByName("pfloat", result, new SpecAttributeSchema().setName("pfloat").setType("decimal"));
        assertAttrByName("pdouble", result, new SpecAttributeSchema().setName("pdouble").setType("decimal"));
        assertAttrByName("pbool", result, new SpecAttributeSchema().setName("pbool").setType("boolean"));
        assertAttrByName("pstr", result, new SpecAttributeSchema().setName("pstr").setType("string"));
        assertAttrByName("bigd", result, new SpecAttributeSchema().setName("bigd").setType("decimal"));
        assertAttrByName("bigi", result, new SpecAttributeSchema().setName("bigi").setType("number"));

        assertAttrByName("wpint", result, new SpecAttributeSchema().setName("wpint").setType("number"));
        assertAttrByName("wplong", result, new SpecAttributeSchema().setName("wplong").setType("number"));
        assertAttrByName("wpbyte", result, new SpecAttributeSchema().setName("wpbyte").setType("number"));
        assertAttrByName("wpshort", result, new SpecAttributeSchema().setName("wpshort").setType("number"));
        assertAttrByName("wpfloat", result, new SpecAttributeSchema().setName("wpfloat").setType("decimal"));
        assertAttrByName("wpdouble", result, new SpecAttributeSchema().setName("wpdouble").setType("decimal"));
        assertAttrByName("wpbool", result, new SpecAttributeSchema().setName("wpbool").setType("boolean"));
        assertAttrByName("baseobj", result, new SpecAttributeSchema().setName("baseobj").setType("any"));
        assertAttrByName("posstype", result, new SpecAttributeSchema().setName("posstype")
                .setType("string | number"));
    }

    @Test
    void shouldDescribeArraysCorrectly() {
        var result = ReflectionUtils.describeClass(ArraysSchema.class);
        System.out.println(result);
        assertAttrByName("intArray", result, new SpecAttributeSchema()
                .setName("intArray").setType("list").setValueType("number"));
        assertAttrByName("wintArray", result, new SpecAttributeSchema()
                .setName("wintArray").setType("list").setValueType("number"));

        assertAttrByName("strArray", result, new SpecAttributeSchema()
                .setName("strArray").setType("list").setValueType("string"));

        var timeUnitDef = ReflectionUtils.describeClass(TimeUnitDef.class);
        assertAttrByName("timeArray", result, new SpecAttributeSchema()
                .setName("timeArray").setType("list").setValueType("object").setChildren(timeUnitDef));
    }

    @Test
    void shouldDescribeListsCorrectly() {
        var result = ReflectionUtils.describeClass(ListSchema.class);
        System.out.println(result);
        assertAttrByName("flags", result, new SpecAttributeSchema()
                .setName("flags").setType("list").setValueType("string"));
        assertAttrByName("flagswo", result, new SpecAttributeSchema()
                .setName("flagswo").setType("list").setValueType(null));


        var timeUnitDef = ReflectionUtils.describeClass(TimeUnitDef.class);
        assertAttrByName("timeUnitDefs", result, new SpecAttributeSchema()
                .setName("timeUnitDefs").setType("list").setValueType("object").setChildren(timeUnitDef));
        assertAttrByName("timeUnitDefswo", result, new SpecAttributeSchema()
                .setName("timeUnitDefswo").setType("list").setValueType(null));

        assertAttrByName("streaks", result, new SpecAttributeSchema()
                .setName("streaks").setType("list").setValueType("number"));
    }

    @Test
    void shouldDescribeEnumsCorrectly() {
        var result = ReflectionUtils.describeClass(EnumSchema.class);
        System.out.println(result);
        assertAttrByName("testEnum", result, new SpecAttributeSchema()
                .setName("testEnum").setType("enum").setValueSet(List.of("VALUE_1", "VALUE_2", "VALUE_3")));
        assertAttrByName("withQualified", result, new SpecAttributeSchema()
                .setName("withQualified").setType("enum")
                .setDescription("sample description")
                .setValueSet(List.of("value1", "value2", "value3")));
    }

    @Test
    void shouldDescribeInheritanceCorrectly() {
        var result = ReflectionUtils.describeClass(InheritanceSchema.class);
        System.out.println(result);
        Assertions.assertEquals(3, result.size());
        assertAttrByName("testEnum", result, new SpecAttributeSchema()
                .setName("testEnum").setType("enum").setValueSet(List.of("VALUE_1", "VALUE_2", "VALUE_3")));
        assertAttrByName("withQualified", result, new SpecAttributeSchema()
                .setName("withQualified").setType("enum")
                .setDescription("sample description")
                .setValueSet(List.of("value1", "value2", "value3")));
        assertAttrByName("id", result, new SpecAttributeSchema().setName("id").setType("number"));
    }

    @Test
    void shouldDescribeReferencesCorrectly() {
        var result = ReflectionUtils.describeClass(DirectRefSchema.class);
        System.out.println(result);

        var inner = ReflectionUtils.describeClass(InnerSchemaClz.class);
        assertAttrByName("innerSchema", result, new SpecAttributeSchema()
                .setName("innerSchema").setType("object").setChildren(inner));
    }

    private static class PrimitiveSchema {
        private int pint;
        private long plong;
        private byte pbyte;
        private short pshort;
        private float pfloat;
        private double pdouble;
        private boolean pbool;
        private String pstr;
        private Integer wpint;
        private Long wplong;
        private Byte wpbyte;
        private Short wpshort;
        private Float wpfloat;
        private Double wpdouble;
        private Boolean wpbool;
        private Object baseobj;
        @DefinitionDetails(possibleTypes = {"string", "number"})
        private Object posstype;
        private BigDecimal bigd;
        private BigInteger bigi;
    }

    private static class ArraysSchema {
        private int[] intArray;
        private Integer[] wintArray;
        private String[] strArray;
        private TimeUnitDef[] timeArray;
    }

    private static class ListSchema {
        @DefinitionDetails(parameterizedType = String.class)
        private List<String> flags;
        private List<String> flagswo;

        @DefinitionDetails(parameterizedType = TimeUnitDef.class)
        private ArrayList<TimeUnitDef> timeUnitDefs;
        private ArrayList<TimeUnitDef> timeUnitDefswo;

        @DefinitionDetails(parameterizedType = Long.class)
        private Set<Long> streaks;
    }

    private static class EnumSchema {
        private TestEnum testEnum;
        @DefinitionDetails(valueSet = {"value1", "value2", "value3"},
        description = "sample description")
        private TestEnum withQualified;
    }

    private static class InheritanceSchema extends EnumSchema {
        private int id;
    }

    private static class DirectRefSchema {
        private InnerSchemaClz innerSchema;
    }

    private static class InnerSchemaClz {
        private Integer id;

        @DefinitionDetails(parameterizedType = AwardDef.class)
        private List<AwardDef> awards;
    }

    private enum TestEnum {
        VALUE_1,
        VALUE_2,
        VALUE_3
    }

}
