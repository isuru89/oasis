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

package io.github.oasis.game.process;

import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Event;
import io.github.oasis.model.FieldCalculator;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class FieldInjector<E extends Event> implements MapFunction<E, E> {

    private final List<FieldCalculator> fieldCalculatorList;

    public FieldInjector(List<FieldCalculator> fieldCalculatorList) {
        this.fieldCalculatorList = Utils.isNonEmpty(fieldCalculatorList) ?
                fieldCalculatorList : new LinkedList<>();
    }

    @Override
    public E map(E value) {
        if (Utils.isNonEmpty(fieldCalculatorList)) {
            for (FieldCalculator fc : fieldCalculatorList) {
                if (Utils.eventEquals(value, fc.getForEvent())) {
                    Object evaluated = evaluate(fc, value.getAllFieldValues());
                    value.setFieldValue(fc.getFieldName(), evaluated);
                }
            }
        }
        return value;
    }

    private static Object evaluate(FieldCalculator fieldCalculator, Map<String, Object> vars) {
        return Utils.executeExpression(fieldCalculator.getExpression(), vars);
    }
}
