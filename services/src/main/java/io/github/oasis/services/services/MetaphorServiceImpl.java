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

import io.github.oasis.model.Constants;
import io.github.oasis.model.ShopItem;
import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.model.events.EventNames;
import io.github.oasis.services.DataCache;
import io.github.oasis.services.dto.defs.HeroDto;
import io.github.oasis.services.exception.InputValidationException;
import io.github.oasis.services.exception.OasisGameException;
import io.github.oasis.services.model.PurchasedItem;
import io.github.oasis.services.model.UserProfile;
import io.github.oasis.services.model.UserTeam;
import io.github.oasis.services.utils.Checks;
import io.github.oasis.services.utils.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service("metaphorService")
public class MetaphorServiceImpl implements IMetaphorService {

    @Autowired
    private IOasisDao dao;

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IEventsService eventsService;

    @Autowired
    private DataCache dataCache;

    @Override
    public void buyItem(long userBy, long itemId) throws Exception {
        ShopItem shopItem = readShopItem(itemId);
        if (shopItem != null) {
            if (shopItem.getExpirationAt() != null && shopItem.getExpirationAt() < System.currentTimeMillis()) {
                // if item is expired, then disable it.
                if (disableShopItem(itemId)) {
                    throw new InputValidationException("Item '" + itemId + "' is already expired!");
                } else {
                    throw new OasisGameException("Failed to disable expired item '" + itemId + "'!");
                }
            }

            buyItem(userBy, itemId, shopItem.getPrice());
        } else {
            throw new InputValidationException("No item is found by id " + itemId + "!");
        }
    }

    @Override
    public boolean allocateBuyingItem(long itemId) throws Exception {
        Checks.greaterThanZero(itemId, "itemId");

        return dao.executeCommand(Q.METAPHOR.UPDATE_ITEM_AVAILABILITY,
                Maps.create()
                        .put("itemId", itemId)
                        .put("ts", System.currentTimeMillis())
                        .build()) > 0;
    }

    @Override
    public void buyItem(long userBy, long itemId, float price) throws Exception {
        Checks.greaterThanZero(userBy, "userId");
        Checks.greaterThanZero(itemId, "itemId");

        // can buy?
        UserProfile userProfile = profileService.readUserProfile(userBy);
        List<Map<String, Object>> buyableList = ServiceUtils.toList(
                dao.executeQuery(Q.METAPHOR.ITEM_HERO_BUYABLE,
                        Maps.create()
                                .put("itemId", itemId)
                                .put("userHero", userProfile.getHeroId()).build()));
        if (buyableList.isEmpty()) {
            throw new InputValidationException("User cannot purchase this item as it is not available for your hero!");
        }

        dao.runTx(ctx -> {
            Iterable<Map<String, Object>> userPoints = ctx.executeQuery(
                    Q.METAPHOR.GET_USER_AVAILABLE_POINTS,
                    Maps.create("userId", userBy));
            Map<String, Object> balanceMap = userPoints.iterator().next();
            float balance = ((Double) balanceMap.get("Balance")).floatValue();

            if (balance > price) {
                Iterable<UserTeam> userTeams = ctx.executeQuery(Q.PROFILE.FIND_CURRENT_TEAM_OF_USER,
                        Maps.create()
                                .put("userId", userBy)
                                .put("currentEpoch", System.currentTimeMillis()).build(),
                        UserTeam.class);
                UserTeam currTeam = null;
                if (userTeams.iterator().hasNext()) {
                    currTeam = userTeams.iterator().next();
                }

                long teamId = currTeam != null ? currTeam.getTeamId() : dataCache.getTeamDefault().getId();
                long scopeId = currTeam != null ? currTeam.getScopeId() : dataCache.getTeamScopeDefault().getId();

                Map<String, Object> data = Maps.create()
                        .put("userId", userBy)
                        .put("forHero", userProfile.getHeroId())
                        .put("itemId", itemId)
                        .put("cost", price)
                        .put("teamId", teamId)
                        .put("scopeId", scopeId)
                        .put("purchasedAt", System.currentTimeMillis())
                        .build();
                ctx.executeCommand(Q.METAPHOR.BUY_ITEM, data);
                return true;
            } else {
                throw new OasisGameException("You do not have enough money to buy this item!");
            }
        });
    }

    @Override
    public void shareItem(long userBy, long itemId, long toUser, int amount) throws Exception {
        Checks.greaterThanZero(userBy, "userId");
        Checks.greaterThanZero(itemId, "itemId");
        Checks.greaterThanZero(toUser, "toUser");

        List<Map<String, Object>> userHeros = ServiceUtils.toList(
                dao.executeQuery(Q.METAPHOR.ITEM_HERO_SHARABLE,
                        Maps.create().put("fromUserId", userBy).put("toUserId", toUser).build()));
        if (userHeros.isEmpty()) {
            throw new InputValidationException("The user you are going to share the item is not following the same hero as you!");
        }
        final int toUserHero = Integer.parseInt(String.valueOf(userHeros.get(0).get("user2Hero")));

        dao.runTx(ctx -> {
            long currTs = System.currentTimeMillis();
            Map<String, Object> data = Maps.create().put("userId", userBy)
                    .put("itemId", itemId)
                    .put("currentEpoch", currTs)
                    .put("amount", amount)
                    .build();
            long l = ctx.executeCommand(Q.METAPHOR.SHARE_ITEM, data);
            if (l == amount) {
                Iterable<UserTeam> userTeams = ctx.executeQuery(Q.PROFILE.FIND_CURRENT_TEAM_OF_USER,
                        Maps.create().put("userId", toUser)
                                .put("currentEpoch", currTs).build(),
                        UserTeam.class);
                UserTeam currTeam = null;
                if (userTeams.iterator().hasNext()) {
                    currTeam = userTeams.iterator().next();
                }

                long teamId = currTeam != null ? currTeam.getTeamId() : dataCache.getTeamDefault().getId();
                long scopeId = currTeam != null ? currTeam.getScopeId() : dataCache.getTeamScopeDefault().getId();

                Map<String, Object> item = Maps.create()
                        .put("userId", toUser)
                        .put("forHero", toUserHero)
                        .put("teamId", teamId)
                        .put("teamScopeId", scopeId)
                        .put("itemId", itemId)
                        .build();
                ctx.executeCommand(Q.METAPHOR.SHARE_TO_ITEM, item);

                // add an event to stream processor
                eventsService.submitEvent(
                        dataCache.getInternalEventSourceToken().getToken(),
                        Maps.create()
                                .put(Constants.FIELD_EVENT_TYPE, EventNames.OASIS_EVENT_SHOP_ITEM_SHARE)
                                .put(Constants.FIELD_TIMESTAMP, currTs)
                                .put(Constants.FIELD_USER, userBy)
                                .put(Constants.FIELD_TEAM, teamId)
                                .put(Constants.FIELD_SCOPE, scopeId)
                                .put(Constants.FIELD_GAME_ID, dataCache.getDefGameId())
                                .put(Constants.FIELD_ID, null)
                                .put("toUser", toUser)
                                .put("itemId", itemId)
                                .put("itemAmount", amount)
                                .build());


                return true;
            } else {
                throw new OasisGameException("Cannot share this item! Maybe the item itself is shared to you by friend!");
            }
        });
    }

    @Override
    public List<PurchasedItem> readUserPurchasedItems(long userId, long since) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("hasSince", since > 0);

        return ServiceUtils.toList(dao.executeQuery(Q.METAPHOR.GET_PURCHASED_ITEMS,
                Maps.create()
                        .put("userId", userId).put("since", since)
                        .build(),
                PurchasedItem.class,
                tdata));
    }


    @Override
    public long addShopItem(long gameId, ShopItem item) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");
        Checks.nonNullOrEmpty(item.getTitle(), "title");
        Checks.nonNullOrEmpty(item.getDescription(), "description");

        Map<String, Object> templateData = Maps.create("hasMaxItems",
                item.getMaxAvailableItems() != null && item.getMaxAvailableItems() > 0);

        Map<String, Object> data = Maps.create()
                .put("title", item.getTitle())
                .put("description", item.getDescription())
                .put("scope", item.getScope())
                .put("level", item.getLevel())
                .put("price", item.getPrice())
                .put("imageRef", item.getImageRef())
                .put("forHero", item.getForHero())
                .put("maxAvailable", item.getMaxAvailableItems())
                .put("expirationAt", item.getExpirationAt())
                .build();

        return dao.executeInsert(Q.METAPHOR.ADD_SHOP_ITEM, data, templateData, "id");
    }

    @Override
    public List<ShopItem> listShopItems(long gameId) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");

        Iterable<ShopItem> items = dao.executeQuery(Q.METAPHOR.LIST_ITEMS, null, ShopItem.class);
        LinkedList<ShopItem> shopItems = new LinkedList<>();
        for (ShopItem item : items) {
            shopItems.add(item);
        }
        return shopItems;
    }

    @Override
    public List<ShopItem> listShopItems(long gameId, int heroId) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");

        Map<String, Object> data = Maps.create()
                .put("gameId", gameId)
                .put("userHero", heroId).build();
        return ServiceUtils.toList(dao.executeQuery(Q.METAPHOR.LIST_ITEMS, data, ShopItem.class));
    }

    @Override
    public ShopItem readShopItem(long id) throws Exception {
        Checks.greaterThanZero(id, "id");

        return ServiceUtils.getTheOnlyRecord(dao, Q.METAPHOR.READ_ITEM,
                Maps.create("itemId", id),
                ShopItem.class);
    }


    @Override
    public boolean disableShopItem(long id) throws Exception {
        Checks.greaterThanZero(id, "id");

        Map<String, Object> dataMap = Maps.create("itemId", id);
        boolean success = dao.executeCommand(Q.METAPHOR.DISABLE_ITEM, dataMap) > 0;
        if (success) {
            dao.executeCommand(Q.METAPHOR.DISABLE_PURCHASES_OF_ITEM, dataMap);
        }
        return success;
    }

    @Override
    public List<HeroDto> listHeros() throws Exception {
        return ServiceUtils.toList(dao.executeQuery(Q.METAPHOR.LIST_HEROS, new HashMap<>(), HeroDto.class));
    }


    @Override
    public boolean changeUserHero(long userId, int newHeroId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        List<HeroDto> heros = listHeros();
        if (heros.stream().noneMatch(hero -> hero.getHeroId() == newHeroId)) {
            throw new InputValidationException("No hero is found by hero id " + newHeroId + "!");
        }

        Map<String, Object> data = Maps.create()
                .put("userId", userId)
                .put("heroId", newHeroId)
                .put("heroUpdatedAt", System.currentTimeMillis())
                .put("updateLimit", 2)      // @TODO load from deployment configs
                .build();

        return (Boolean) dao.runTx(ctx -> {
            boolean success = ctx.executeCommand(Q.METAPHOR.UPDATE_HERO, data) > 0;
            if (success) {
                Map<String, Object> userMap = Maps.create("userId", userId);

                // re-available limited edition items
                ctx.executeCommand(Q.METAPHOR.REAVAILABLE_PURCHASES_OF_USER, userMap);
                // disable all purchases
                ctx.executeCommand(Q.METAPHOR.DISABLE_PURCHASES_OF_USER, userMap);
            }
            return success;
        });
    }
}
