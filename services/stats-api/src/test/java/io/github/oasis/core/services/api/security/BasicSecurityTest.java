package io.github.oasis.core.services.api.security;

import io.github.oasis.core.services.api.TestUtils;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.TestEngineConfigs;
import io.github.oasis.core.services.api.dao.IApiKeyDao;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static io.github.oasis.core.services.ApiConstants.APP_ID_HEADER;
import static io.github.oasis.core.services.ApiConstants.APP_KEY_HEADER;
import static io.github.oasis.core.services.api.security.ApiKeyService.ROLE_ADMIN_FLAG;
import static io.github.oasis.core.services.api.security.ApiKeyService.ROLE_CURATOR_FLAG;
import static io.github.oasis.core.services.api.security.ApiKeyService.ROLE_PLAYER_FLAG;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests related to api authentication and authorization.
 *
 * @author Isuru Weerarathna
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestEngineConfigs.class)
class BasicSecurityTest {

    private final GameCreateRequest aSampleGame = GameCreateRequest.builder()
            .name("mygame")
            .description("This is a test game")
            .motto("All the way!").build();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IApiKeyDao apiKeyDao;

    @Autowired
    private BackendRepository backendRepository;

    @Test
    void testWithoutApiKeyShouldFail() throws Exception {
        mvc.perform(ofJson("/games"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp", Matchers.notNullValue()))
                .andExpect(jsonPath("$.status", Matchers.equalTo(401)))
                .andExpect(jsonPath("$.errorCode", Matchers.equalTo(ErrorCodes.AUTH_NO_SUCH_CREDENTIALS)))
                .andExpect(jsonPath("$.path", Matchers.equalTo("/games")))
                .andExpect(jsonPath("$.message", Matchers.notNullValue()));
    }

    @Test
    void testWithWrongAppIdShouldFail() throws Exception {
        mvc.perform(ofJson("/games")
                .header(APP_KEY_HEADER, "root")
                .header(APP_ID_HEADER, "jondoe"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testWithWrongApiKeyShouldFail() throws Exception {
        mvc.perform(ofJson("/games")
                .header(APP_KEY_HEADER, "root2")
                .header(APP_ID_HEADER, "root"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testWithOnlyApiKeyShouldFail() throws Exception {
        mvc.perform(ofJson("/games")
                .header(APP_KEY_HEADER, "root2"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testWithOnlyAppIdShouldFail() throws Exception {
        mvc.perform(ofJson("/games")
                .header(APP_ID_HEADER, "unknownapp"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSuccessfulReadFromDefaultApiKey() throws Exception {
        createSampleGameSuccessfully();

        mvc.perform(ofJsonWithRoot("/games"))
                .andDo(MockMvcResultHandlers.log())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.records", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.completed", Matchers.equalTo(true)));

    }

    @Test
    void testCuratorShouldNotBeAbleToDoAdminOperation() throws Exception {
        apiKeyDao.addNewApiKey("curator", "curator", ROLE_CURATOR_FLAG + ROLE_PLAYER_FLAG);

        mvc.perform(MockMvcRequestBuilders.post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APP_ID_HEADER, "curator")
                .header(APP_KEY_HEADER, "curator")
                .content(TestUtils.toJson(aSampleGame)
                )
        ).andDo(print())
            .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp", Matchers.notNullValue()))
                .andExpect(jsonPath("$.status", Matchers.equalTo(403)))
                .andExpect(jsonPath("$.errorCode", Matchers.equalTo(ErrorCodes.AUTH_BAD_CREDENTIALS)))
                .andExpect(jsonPath("$.path", Matchers.equalTo("/games")))
                .andExpect(jsonPath("$.message", Matchers.notNullValue()));;

    }

    @Test
    void testPlayerShouldNotBeAbleToDoAdminOperation() throws Exception {
        apiKeyDao.addNewApiKey("player", "player", ROLE_PLAYER_FLAG);

        mvc.perform(MockMvcRequestBuilders.post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .header(APP_ID_HEADER, "player")
                .header(APP_KEY_HEADER, "player")
                .content(TestUtils.toJson(aSampleGame)
                )
        ).andExpect(status().isForbidden());
    }

    @Test
    void testAdminOnlyShouldBeAbleToDoPlayerOperation() throws Exception {
        apiKeyDao.addNewApiKey("adminonly", "adminonly", ROLE_ADMIN_FLAG);

        mvc.perform(ofJson("/games")
                .header(APP_ID_HEADER, "adminonly")
                .header(APP_KEY_HEADER, "adminonly"))
                .andDo(MockMvcResultHandlers.log())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    private void createSampleGameSuccessfully() {
        try {
            // clear all existing games
            backendRepository.getAdminRepository().listGames("0", 100).getRecords()
                .forEach(g -> backendRepository.deleteGame(g.getId()));

            mvc.perform(MockMvcRequestBuilders.post("/games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(APP_ID_HEADER, "root")
                    .header(APP_KEY_HEADER, "root")
                    .content(TestUtils.toJson(aSampleGame)
                    )
            ).andExpect(status().isOk());
        } catch (Exception e) {
            Assertions.fail("Should not expected to fail!", e);
        }
    }

    private MockHttpServletRequestBuilder ofJsonWithRoot(String path) {
        return MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(APP_ID_HEADER, "root")
                .header(APP_KEY_HEADER, "root");
    }

    private MockHttpServletRequestBuilder ofJson(String path) {
        return MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}
