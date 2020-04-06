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

package io.github.oasis.engine.factory;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

import java.util.function.Function;

/**
 * @author Isuru Weerarathna
 */
public class AbstractActorProviderModule extends AbstractModule {

    protected Injector injector;

    public AbstractActorProviderModule() {
        ActorProviderModuleFactory.registerInstance(this);
    }

    public Injector getInjector() {
        return injector;
    }

    public <T extends Actor> T createInjectedActor(Class<T> actorClz) {
        return getInjector().getInstance(actorClz);
    }

    protected void bindActor(ActorSystem system, Class<? extends Actor> actorClass, String name) {
        bind(ActorRef.class)
                .annotatedWith(Names.named(name))
                .toProvider(Providers.guicify(new ActorRefProvider(system, name, actorClass)));
    }

    private class ActorRefProvider implements Provider<ActorRef> {
        private final ActorRefFactory parentContext;
        private final String name;
        private final Class<? extends Actor> actorClass;
        private final Function<Props, Props> props;

        ActorRefProvider(ActorRefFactory parentContext, String name, Class<? extends Actor> actorClass) {
            this(parentContext, name, actorClass, Function.identity());
        }

        ActorRefProvider(ActorRefFactory parentContext, String name, Class<? extends Actor> actorClass,
                         Function<Props, Props> props) {
            this.name = name;
            this.actorClass = actorClass;
            this.parentContext = parentContext;
            this.props = props;
        }

        @Override
        public ActorRef get() {
            Props appliedProps = props.apply(Props.create(OasisActorProducer.class, injector, name, actorClass));
            return parentContext.actorOf(appliedProps, name);
        }
    }
}
