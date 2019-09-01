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

package io.github.oasis.services.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserProfile {

    private long id;
    private Long extId;
    private String name;
    private String nickName;
    private String email;
    private boolean male;
    private String avatarId;
    private boolean active;
    private boolean autoUser;
    private boolean activated;
    private Long lastLogoutAt;
    @JsonIgnore
    private String password;

    private Integer heroId;
    private int heroUpdateTimes;
    private Long heroLastUpdatedAt;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getHeroId() {
        return heroId;
    }

    public void setHeroId(Integer heroId) {
        this.heroId = heroId;
    }

    public int getHeroUpdateTimes() {
        return heroUpdateTimes;
    }

    public void setHeroUpdateTimes(int heroUpdateTimes) {
        this.heroUpdateTimes = heroUpdateTimes;
    }

    public Long getHeroLastUpdatedAt() {
        return heroLastUpdatedAt;
    }

    public void setHeroLastUpdatedAt(Long heroLastUpdatedAt) {
        this.heroLastUpdatedAt = heroLastUpdatedAt;
    }

    public Long getLastLogoutAt() {
        return lastLogoutAt;
    }

    public void setLastLogoutAt(Long lastLogoutAt) {
        this.lastLogoutAt = lastLogoutAt;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isAutoUser() {
        return autoUser;
    }

    public void setAutoUser(boolean autoUser) {
        this.autoUser = autoUser;
    }

    public Long getExtId() {
        return extId;
    }

    public void setExtId(Long extId) {
        this.extId = extId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
