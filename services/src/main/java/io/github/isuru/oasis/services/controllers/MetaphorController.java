package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.services.api.dto.HeroDto;
import io.github.isuru.oasis.services.dto.DefinitionAddResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MetaphorController {

    @PostMapping("/metaphor/user/{id}/change-hero/{heroId}")
    @ResponseBody
    public void changeUserHero(@PathVariable("id") long userId, @PathVariable("heroId") int heroId) {

    }


    @PostMapping("/metaphor/shop/buy")
    @ResponseBody
    public void buyItem() {

    }

    @PostMapping("/metaphor/shop/share")
    @ResponseBody
    public void shareItem() {

    }


    @GetMapping("/metaphor/def/game/heros")
    @ResponseBody
    public List<HeroDto> listAllHeros() {
        return null;
    }



    @PostMapping("/metaphor/def/game/{id}/item")
    @ResponseBody
    public DefinitionAddResponse addItem(@RequestBody ShopItem shopItem) {
        return new DefinitionAddResponse("shopItem", 0);
    }

}
