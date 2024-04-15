package io.github.oasis.eventsapi;

import io.github.oasis.core.EventJson;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.eventsapi.to.EventPublishResponse;
import io.github.oasis.eventsapi.to.EventRequest;
import io.github.oasis.eventsapi.to.PingResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@SpringBootTest
@AutoConfigureWebTestClient
class EventsApiApplicationTests {

	@Autowired
	private WebTestClient webClient;

	@Test
	void testPingRequestIsPublic() {
		webClient.get().uri("/public/ping")
				.exchange()
				.expectStatus().isOk()
				.expectBody(PingResponse.class)
				.value(pingResponse -> {
                    Assertions.assertEquals("OK", pingResponse.health());
                    Assertions.assertTrue(StringUtils.isNotBlank(pingResponse.tz()));
                });
	}

	@Test
	void testSingleEventPublish() {
		var event = new EventJson(Map.of(
				EventJson.USER_NAME, "isuru@oasis.io",
				EventJson.TIMESTAMP, System.currentTimeMillis(),
				EventJson.USER_ID, 100,
				EventJson.GAME_ID, 1,
				EventJson.SOURCE_ID, 1,
				EventJson.TEAM_ID, 200
		));
		webClient.put().uri("/events")
				.header("X-APP-ID", "appisuru")
				.header("X-APP-KEY", "keyisuru")
				.bodyValue(new EventRequest(event))
				.exchange()
				.expectStatus().isOk()
				.expectBody(EventPublishResponse.class)
				.value(res -> {
					System.out.println(res);
				});
	}

}
