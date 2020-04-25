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

package io.github.oasis.model.defs;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
public class FieldDef extends BaseDef implements Serializable {

    private String forEvent;
    private String expression;
    private Serializable executableExpression;
    private int priority;
    private String fieldName;

    public boolean matchesWithEvent(String eventType) {
        return forEvent.equals(eventType);
    }

    public Serializable getExecutableExpression() {
        return executableExpression;
    }

    public void setExecutableExpression(Serializable executableExpression) {
        this.executableExpression = executableExpression;
    }

    public String getForEvent() {
        return forEvent;
    }

    public void setForEvent(String forEvent) {
        this.forEvent = forEvent;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
