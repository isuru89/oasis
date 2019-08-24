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

/**
 * @author iweerarathna
 */
public class PurchasedItem {

    private int itemId;
    private int userId;
    private Double cost;
    private Long purchasedAt;
    private Long sharedAt;
    private boolean viaFriend;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Long getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(Long purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public Long getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(Long sharedAt) {
        this.sharedAt = sharedAt;
    }

    public boolean isViaFriend() {
        return viaFriend;
    }

    public void setViaFriend(boolean viaFriend) {
        this.viaFriend = viaFriend;
    }
}
