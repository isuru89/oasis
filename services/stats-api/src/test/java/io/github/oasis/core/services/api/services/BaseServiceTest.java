package io.github.oasis.core.services.api.services;

import io.github.oasis.core.Game;
import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.EventSourceSecrets;
import io.github.oasis.core.model.GameState;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserGender;
import io.github.oasis.core.services.KeyGeneratorSupport;
import io.github.oasis.core.services.api.beans.TestEngineConfigs;
import io.github.oasis.core.services.api.infrastructure.beans.jdbc.JpaBasedRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.ApplicationRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.GameRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.PlayerRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.RegistrationRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.TeamRepository;
import io.github.oasis.core.services.api.infrastructure.repository.dto.GameDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.security.KeyPair;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestEngineConfigs.class)
public class BaseServiceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private ApplicationRepository applicationRepository;


    @Autowired
    private JpaBasedRepository jpaBasedRepository;

    @Autowired
    private KeyGeneratorSupport keyGenerator;

    @BeforeEach
    public void beforeEach() {
        gameRepository.deleteAll();
        applicationRepository.deleteAll();

        registrationRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();
        gameRepository.deleteAll();
    }

    @Test
    void testRanks() {
        var game = Game.builder()
                .name("rank-test-game")
                .build();
        game = jpaBasedRepository.addNewGame(game);

        jpaBasedRepository.addRank(game.getId(), RankInfo.builder().name("gold").priority(1).build());
        jpaBasedRepository.addRank(game.getId(), RankInfo.builder().name("silver").priority(2).build());
        jpaBasedRepository.addRank(game.getId(), RankInfo.builder().name("platinum").priority(3).build());

        var allRanks = jpaBasedRepository.listAllRanks(game.getId());
        System.out.println(allRanks);

        jpaBasedRepository.readGame(game.getId());
    }

    @Test
    void testGameState() {
        var game = Game.builder()
                .name("status-test-game")
                .build();
        game = jpaBasedRepository.addNewGame(game);

        jpaBasedRepository.updateGameStatus(game.getId(), GameState.CREATED.name(), Instant.now().toEpochMilli());

    }

    @Test
    void testEventApps() throws OasisException {
        KeyPair oasisKeyPair = keyGenerator.generate("es-1");
        EventSourceSecrets sourceSecrets = new EventSourceSecrets();
        sourceSecrets.setPublicKey(Base64.getEncoder().encodeToString(oasisKeyPair.getPublic().getEncoded()));
        sourceSecrets.setPrivateKey(Base64.getEncoder().encodeToString(oasisKeyPair.getPrivate().getEncoded()));

        var app = jpaBasedRepository.addEventSource(EventSource.builder()
                        .name("es-1")
                        .token(UUID.randomUUID().toString())
                        .secrets(sourceSecrets)
                .build());

        var g1 = gameRepository.save(GameDto.builder()
                .name("g1")
                .build());

        jpaBasedRepository.addEventSourceToGame(app.getId(), g1.getId());

        System.out.println(g1);

        System.out.println(app);
    }

    @Test
    void testAddPlayer() {
        var game = jpaBasedRepository.addNewGame(Game.builder()
                        .name("Test Game 1")
                        .description("Game description")
                .build());
        System.out.println(game);

        var team = jpaBasedRepository.addTeam(TeamObject.builder()
                        .name("team warriors")
                        .colorCode("#ff0000")
                        .avatarRef("https://logo.io/team1.jpg")
                        .gameId(game.getId())
                .build());
        System.out.println(team);

        var team2 = jpaBasedRepository.addTeam(TeamObject.builder()
                .name("team jaguar")
                .colorCode("#00ff00")
                .avatarRef("https://logo.io/team2.jpg")
                .gameId(game.getId())
                .build());
        System.out.println(team2);
        System.out.println(jpaBasedRepository.updateTeam(team2.getId(), TeamObject.builder()
                        .avatarRef("new avatar link")
                .build()));
        jpaBasedRepository.updateTeam(team2.getId(), TeamObject.builder()
                .colorCode("#000fff")
                .build());

        System.out.println(jpaBasedRepository.readTeam("team jaguar"));


        var player = jpaBasedRepository.addPlayer(PlayerObject.builder()
                        .email("isuru@oasis.io")
                        .displayName("Isuru wee")
                        .gender(UserGender.MALE)
                        .timeZone("Asia/Colombo")
                        .active(true)
                .build());
        System.out.println(player);

        jpaBasedRepository.addPlayerToTeam(player.getId(), game.getId(), team.getId());


       var dbTeam = jpaBasedRepository.readTeam(team.getId());

        System.out.println(dbTeam);

        System.out.println(jpaBasedRepository.readPlayer(player.getId()));

        //jpaBasedRepository.addPlayerToTeam(player.getId(), game.getId(), team2.getId());
    }

}
