package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.services.api.dto.HeroDto;
import io.github.isuru.oasis.services.dto.DefinitionAddResponse;
import io.github.isuru.oasis.services.dto.ItemBuyReq;
import io.github.isuru.oasis.services.dto.ItemShareReq;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.security.CurrentUser;
import io.github.isuru.oasis.services.security.UserPrincipal;
import io.github.isuru.oasis.services.services.IMetaphorService;
import io.github.isuru.oasis.services.utils.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/metaphor")
public class MetaphorController {

    @Autowired
    private IMetaphorService metaphorService;

    @PostMapping("/user/{id}/change-hero/{heroId}")
    @ResponseBody
    public void changeUserHero(@PathVariable("id") long userId, @PathVariable("heroId") int heroId) throws Exception {
        metaphorService.changeUserHero(userId, heroId);
        // @TODO response
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
            throw new InputValidationException("Item has sold out!");
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
