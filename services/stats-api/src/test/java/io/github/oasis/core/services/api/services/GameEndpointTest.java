package io.github.oasis.core.services.api.services;

import io.github.oasis.core.Game;
import io.github.oasis.core.model.GameState;
import io.github.oasis.core.model.GameStatus;
import io.github.oasis.core.services.api.app.request.GameCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameEndpointTest extends BaseServiceTest {

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();

        baseUrl = "http://localhost:" + port;

        gameRepository.deleteAll();
    }

    @Test
    void testGameAdd() {
        var request = GameCreateRequest.builder()
                .name("test-game1")
                .motto("all the way up")
                .logoRef("http://logo.jpg")
                .description("the game description")
                .build();

        var game = mustPostSuccess(baseUrl + "/api/games", withAdminReq(request), Game.class);
        System.out.println(game);

        var status = mustGetSuccess(baseUrl + "/api/games/"+game.getId(), withAdminReq(), GameStatus.class);
        System.out.println(status);
    }

}
