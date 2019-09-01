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

package io.github.oasis.services.services;

import io.github.oasis.model.ShopItem;
import io.github.oasis.services.dto.defs.HeroDto;
import io.github.oasis.services.model.PurchasedItem;

import java.util.List;

/**
 *
 * @author isuru89
 */
public interface IMetaphorService {

    /**
     * Allocate an item before purchasing. Decreases <code>max_availability</code>
     * by one when this method returns true.
     *
     * @param itemId item id to be purchased.
     * @return true if an item is available. false, otherwise.
     * @throws Exception item allocation exception.
     */
    boolean allocateBuyingItem(long itemId) throws Exception;
    void buyItem(long userBy, long itemId, float price) throws Exception;
    void buyItem(long userBy, long itemId) throws Exception;
    void shareItem(long userBy, long itemId, long toUser, int amount) throws Exception;


    List<PurchasedItem> readUserPurchasedItems(long userId, long since) throws Exception;

    long addShopItem(long gameId, ShopItem item) throws Exception;
    List<ShopItem> listShopItems(long gameId) throws Exception;
    List<ShopItem> listShopItems(long gameId, int heroId) throws Exception;
    ShopItem readShopItem(long id) throws Exception;
    boolean disableShopItem(long id) throws Exception;

    List<HeroDto> listHeros() throws Exception;

    boolean changeUserHero(long userId, int newHeroId) throws Exception;

}
