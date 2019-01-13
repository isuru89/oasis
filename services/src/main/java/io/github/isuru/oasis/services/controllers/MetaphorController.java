package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.services.dto.DefinitionAddResponse;
import io.github.isuru.oasis.services.dto.ItemBuyReq;
import io.github.isuru.oasis.services.dto.ItemShareReq;
import io.github.isuru.oasis.services.dto.StatusResponse;
import io.github.isuru.oasis.services.dto.defs.HeroDto;
import io.github.isuru.oasis.services.security.CurrentUser;
import io.github.isuru.oasis.services.security.UserPrincipal;
import io.github.isuru.oasis.services.services.IMetaphorService;
import io.github.isuru.oasis.services.utils.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/metaphor")
public class MetaphorController {

    @Autowired
    private IMetaphorService metaphorService;

    @PostMapping("/user/{id}/change-hero/{heroId}")
    @ResponseBody
    public StatusResponse changeUserHero(@PathVariable("id") long userId, @PathVariable("heroId") int heroId) throws Exception {
        return new StatusResponse(metaphorService.changeUserHero(userId, heroId));
    }


    @PostMapping("/shop/buy")
    @ResponseBody
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
    @ResponseBody
    public void shareItem(@CurrentUser UserPrincipal user,
                          @RequestBody ItemShareReq shareReq) throws Exception {
        long userId = user.getId();

        long itemId = shareReq.getItemId();
        long toUser = shareReq.getToUser();
        metaphorService.shareItem(userId, itemId, toUser, shareReq.getAmount());
    }


    @GetMapping("/def/game/heros")
    @ResponseBody
    public List<HeroDto> listAllHeros() throws Exception {
        return metaphorService.listHeros();
    }


    @Secured(UserRole.ROLE_ADMIN)
    @PostMapping("/def/game/{id}/item")
    @ResponseBody
    public DefinitionAddResponse addItem(@PathVariable("id") int gameId,
                                         @RequestBody ShopItem shopItem) throws Exception {
        long itemId = metaphorService.addShopItem(gameId, shopItem);
        return new DefinitionAddResponse("shopItem", itemId);
    }

}
