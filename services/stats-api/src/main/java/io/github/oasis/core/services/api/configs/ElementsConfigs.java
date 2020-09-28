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

package io.github.oasis.core.services.api.configs;

import io.github.oasis.core.services.AbstractStatsApiService;
import io.github.oasis.core.services.OasisServiceApiFactory;
import io.github.oasis.core.services.ServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
@Configuration
public class ElementsConfigs implements BeanDefinitionRegistryPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ElementsConfigs.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        List<? extends Class<? extends OasisServiceApiFactory>> apiServices = ServiceLoader.load(OasisServiceApiFactory.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .peek(factory -> LOG.info("Found service plugin: {}", factory.getName()))
                .collect(Collectors.toList());

        SpringServiceRegistrar registrar = new SpringServiceRegistrar(registry);

        try {
            for (Class<? extends OasisServiceApiFactory> apiService : apiServices) {
                OasisServiceApiFactory pluginApiFactory = apiService.getDeclaredConstructor().newInstance();

                pluginApiFactory.initialize(registrar);
            }
        } catch (ReflectiveOperationException ex) {
            throw new BeanCreationException("Unable to load plugin!", ex);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    private static class SpringServiceRegistrar implements ServiceRegistrar {
        private final BeanDefinitionRegistry registry;

        private SpringServiceRegistrar(BeanDefinitionRegistry registry) {
            this.registry = registry;
        }

        @Override
        public void registerStatsService(Class<? extends AbstractStatsApiService> apiService) {
            LOG.info("Registering Stats Service: {}...", apiService.getName());
            registry.registerBeanDefinition(apiService.getSimpleName(),
                    BeanDefinitionBuilder.genericBeanDefinition(apiService).getBeanDefinition());
        }
    }
}
