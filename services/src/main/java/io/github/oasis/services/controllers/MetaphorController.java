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

package io.github.oasis.services.controllers;

import io.github.oasis.model.ShopItem;
import io.github.oasis.services.dto.DefinitionAddResponse;
import io.github.oasis.services.dto.ItemBuyReq;
import io.github.oasis.services.dto.ItemShareReq;
import io.github.oasis.services.dto.StatusResponse;
import io.github.oasis.services.dto.defs.HeroDto;
import io.github.oasis.services.model.UserRole;
import io.github.oasis.services.security.CurrentUser;
import io.github.oasis.services.security.UserPrincipal;
import io.github.oasis.services.services.IMetaphorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/metaphor")
public class MetaphorController {

    @Autowired
    private IMetaphorService metaphorService;

    @PostMapping("/user/{id}/change-hero/{heroId}")
    public StatusResponse changeUserHero(@PathVariable("id") long userId, @PathVariable("heroId") int heroId) throws Exception {
        return new StatusResponse(metaphorService.changeUserHero(userId, heroId));
    }


    @PostMapping("/shop/buy")
    public void buyItem(@CurrentUser UserPrincipal user,
                        @RequestBody ItemBuyReq buyReq) throws Exception {
        long userId = user.getId();
        long itemId = buyReq.getItemId();

        // check item availability
        if (metaphorService.allocateBuyingItem(itemId)) {
            if (buyReq.getPrice() != null && buyReq.getPrice() > 0.0f) {
                float price = buyReq.getPrice();
                metaphorService.buyItem(userId, itemId, price);
            } else {
                metaphorService.buyItem(userId, itemId);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item has sold out!");
        }
    }

    @PostMapping("/shop/share")
    public void shareItem(@CurrentUser UserPrincipal user,
                          @RequestBody ItemShareReq shareReq) throws Exception {
        long userId = user.getId();

        long itemId = shareReq.getItemId();
        long toUser = shareReq.getToUser();
        metaphorService.shareItem(userId, itemId, toUser, shareReq.getAmount());
    }


    @GetMapping("/def/game/heros")
    public List<HeroDto> listAllHeros() throws Exception {
        return metaphorService.listHeros();
    }


    @Secured(UserRole.ROLE_ADMIN)
    @PostMapping("/def/game/{id}/item")
    public DefinitionAddResponse addItem(@PathVariable("id") int gameId,
                                         @RequestBody ShopItem shopItem) throws Exception {
        long itemId = metaphorService.addShopItem(gameId, shopItem);
        return new DefinitionAddResponse("shopItem", itemId);
    }

}
