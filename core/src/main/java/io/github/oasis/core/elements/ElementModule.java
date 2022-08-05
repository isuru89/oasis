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

package io.github.oasis.core.elements;

import io.github.oasis.core.context.RuleExecutionContextSupport;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public abstract class ElementModule {

    /**
     * Called just before engine or service is to be initialized.
     * @param context running context.
     * @throws OasisException any exception thrown while loading.
     */
    public void init(RuntimeContextSupport context) throws OasisException {
    }

    /**
     * Returns the id of this plugin module.
     *
     * @return string non-empty plugin id.
     */
    public abstract String getId();

    public ElementParser getParser() {
        return null;
    }

    public abstract List<Class<? extends AbstractSink>> getSupportedSinks();

    public abstract AbstractSink createSink(Class<? extends AbstractSink> sinkReq, RuntimeContextSupport context);

    public abstract AbstractProcessor<? extends AbstractRule, ? extends Signal> createProcessor(AbstractRule rule, RuleExecutionContextSupport ruleExecutionContext);

    /**
     * Returns a mapping of possible feed definitions this module can generate.
     * The returned map should contain, feed type as key and its spec class as value.
     *
     * @return a map of feed definitions.
     */
    public Map<String, Class<? extends Serializable>> getFeedDefinitions() {
        return Map.of();
    }

    /**
     * Customize the already parsed and derived module definition from the information given on this same class.
     *
     * This method could be used to modify any module specific data. If nothing needs to be changed,
     * just return the same provided instance as return value.
     *
     * @param alreadyParsedDefinition definition already derived using existing provided information.
     * @return module definition instance to be used.
     */
    public ModuleDefinition customizeModuleDef(ModuleDefinition alreadyParsedDefinition) {
        return alreadyParsedDefinition;
    }

    /**
     * Loads scripts package under the given class.
     * @param db db instance to refer.
     * @param clz clz to derive package.
     * @param classLoader classloader to load class.
     * @throws OasisException any exception thrown while loading scripts.
     */
    protected void loadScriptsUnderPackage(Db db, Class<?> clz, ClassLoader classLoader) throws OasisException {
        String pkg = clz.getPackageName().replace('.', '/');
        db.registerScripts(pkg, classLoader);
    }
}
