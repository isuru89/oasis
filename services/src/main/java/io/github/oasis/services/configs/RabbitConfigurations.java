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

package io.github.oasis.services.configs;

import io.github.oasis.model.configs.ConfigKeys;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigurations {

    private String host = "localhost";

    private String serviceWriterUsername = "guest";

    private String serviceWriterPassword = "guest";

    private int port = 5672;

    private String virtualHost = "oasis";

    private String sourceExchangeName = "oasis.event.exchange";

    private String sourceExchangeType = "fanout";

    private boolean sourceExchangeDurable = true;

    // output queues
    private String injectorUser = "injector";
    private String injectorPassword = "injector";

    private String injectorPointsQueue = ConfigKeys.DEF_RABBIT_Q_POINTS_SINK;
    private String injectorBadgesQueue = ConfigKeys.DEF_RABBIT_Q_BADGES_SINK;
    private String injectorMilestonesQueue = ConfigKeys.DEF_RABBIT_Q_MILESTONES_SINK;
    private String injectorMilestoneStatesQueue = ConfigKeys.DEF_RABBIT_Q_MILESTONESTATE_SINK;
    private String injectorChallengesQueue = ConfigKeys.DEF_RABBIT_Q_CHALLENGES_SINK;
    private String injectorRatingsQueue = ConfigKeys.DEF_RABBIT_Q_RATINGS_SINK;
    private String injectorRacesQueue = ConfigKeys.DEF_RABBIT_Q_RACES_SINK;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServiceWriterUsername() {
        return serviceWriterUsername;
    }

    public void setServiceWriterUsername(String serviceWriterUsername) {
        this.serviceWriterUsername = serviceWriterUsername;
    }

    public String getServiceWriterPassword() {
        return serviceWriterPassword;
    }

    public void setServiceWriterPassword(String serviceWriterPassword) {
        this.serviceWriterPassword = serviceWriterPassword;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getSourceExchangeName() {
        return sourceExchangeName;
    }

    public void setSourceExchangeName(String sourceExchangeName) {
        this.sourceExchangeName = sourceExchangeName;
    }

    public String getSourceExchangeType() {
        return sourceExchangeType;
    }

    public void setSourceExchangeType(String sourceExchangeType) {
        this.sourceExchangeType = sourceExchangeType;
    }

    public boolean isSourceExchangeDurable() {
        return sourceExchangeDurable;
    }

    public void setSourceExchangeDurable(boolean sourceExchangeDurable) {
        this.sourceExchangeDurable = sourceExchangeDurable;
    }

    public String getInjectorUser() {
        return injectorUser;
    }

    public void setInjectorUser(String injectorUser) {
        this.injectorUser = injectorUser;
    }

    public String getInjectorPassword() {
        return injectorPassword;
    }

    public void setInjectorPassword(String injectorPassword) {
        this.injectorPassword = injectorPassword;
    }

    public String getInjectorPointsQueue() {
        return injectorPointsQueue;
    }

    public void setInjectorPointsQueue(String injectorPointsQueue) {
        this.injectorPointsQueue = injectorPointsQueue;
    }

    public String getInjectorBadgesQueue() {
        return injectorBadgesQueue;
    }

    public void setInjectorBadgesQueue(String injectorBadgesQueue) {
        this.injectorBadgesQueue = injectorBadgesQueue;
    }

    public String getInjectorMilestonesQueue() {
        return injectorMilestonesQueue;
    }

    public void setInjectorMilestonesQueue(String injectorMilestonesQueue) {
        this.injectorMilestonesQueue = injectorMilestonesQueue;
    }

    public String getInjectorMilestoneStatesQueue() {
        return injectorMilestoneStatesQueue;
    }

    public void setInjectorMilestoneStatesQueue(String injectorMilestoneStatesQueue) {
        this.injectorMilestoneStatesQueue = injectorMilestoneStatesQueue;
    }

    public String getInjectorChallengesQueue() {
        return injectorChallengesQueue;
    }

    public void setInjectorChallengesQueue(String injectorChallengesQueue) {
        this.injectorChallengesQueue = injectorChallengesQueue;
    }

    public String getInjectorRatingsQueue() {
        return injectorRatingsQueue;
    }

    public void setInjectorRatingsQueue(String injectorRatingsQueue) {
        this.injectorRatingsQueue = injectorRatingsQueue;
    }

    public String getInjectorRacesQueue() {
        return injectorRacesQueue;
    }

    public void setInjectorRacesQueue(String injectorRacesQueue) {
        this.injectorRacesQueue = injectorRacesQueue;
    }
}
