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
import io.github.oasis.model.DefinitionUpdateEvent;
import io.github.oasis.model.DefinitionUpdateType;
import io.github.oasis.model.Event;
import io.github.oasis.model.defs.BaseDef;
import io.github.oasis.model.defs.FieldDef;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.io.Serializable;
import java.util.Map;

import static io.github.oasis.game.states.DefinitionUpdateState.BROADCAST_DEF_UPDATE_DESCRIPTOR;

/**
 * @author iweerarathna
 */
public class FieldInjector<E extends Event> extends BroadcastProcessFunction<Event, DefinitionUpdateEvent, Event> {

    @Override
    public void processElement(Event event, ReadOnlyContext ctx, Collector<Event> out) throws Exception {
        Iterable<Map.Entry<Long, BaseDef>> entries = ctx.getBroadcastState(BROADCAST_DEF_UPDATE_DESCRIPTOR).immutableEntries();
        for (Map.Entry<Long, BaseDef> fieldDefEntry : entries) {
            FieldDef definition = (FieldDef) fieldDefEntry.getValue();

            if (definition.matchesWithEvent(event.getEventType())) {
                Object evaluated = evaluate(definition.getExecutableExpression(), event.getAllFieldValues());
                event.setFieldValue(definition.getFieldName(), evaluated);
            }
        }
        out.collect(event);
    }

    private static Object evaluate(Serializable expression, Map<String, Object> vars) {
        return Utils.executeExpression(expression, vars);
    }

    @Override
    public void processBroadcastElement(DefinitionUpdateEvent updateEvent, Context ctx, Collector<Event> out) throws Exception {
        if (updateEvent.getBaseDef() instanceof FieldDef) {
            FieldDef definition = (FieldDef) updateEvent.getBaseDef();
            System.out.println(">>>>>>>>>>>>" + "Field Def Update event " + definition.getId());
            definition.setExecutableExpression(Utils.compileExpression(definition.getExpression()));

            if (updateEvent.getType() == DefinitionUpdateType.DELETED) {
                ctx.getBroadcastState(BROADCAST_DEF_UPDATE_DESCRIPTOR).remove(definition.getId());
            } else {
                ctx.getBroadcastState(BROADCAST_DEF_UPDATE_DESCRIPTOR).put(definition.getId(), definition);
            }
        }
    }
}
