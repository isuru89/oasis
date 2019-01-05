package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.services.api.dto.HeroDto;

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


    long addShopItem(long gameId, ShopItem item) throws Exception;
    List<ShopItem> listShopItems(long gameId) throws Exception;
    List<ShopItem> listShopItems(long gameId, int heroId) throws Exception;
    ShopItem readShopItem(long id) throws Exception;
    boolean disableShopItem(long id) throws Exception;

    List<HeroDto> listHeros() throws Exception;

    boolean changeUserHero(long userId, int newHeroId) throws Exception;

}
