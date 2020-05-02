package io.github.oasis.services.events.dispatcher;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Dispatcher Factory Test")
public class DispatcherFactoryTest {

    @Test
    @DisplayName("Returns correct prefix")
    void testProvidedName() {
        DispatcherFactory factory = new DispatcherFactory();
        Assertions.assertThat(factory.prefix()).isEqualTo("oasis");
    }

    @Test
    @DisplayName("Create verticle successfully")
    void createInstance() throws Exception {
        DispatcherFactory factory = new DispatcherFactory();
        Verticle verticle = factory.createVerticle("oasis:" + TestVerticle.class.getName(),
                Thread.currentThread().getContextClassLoader());
        Assertions.assertThat(verticle).isExactlyInstanceOf(TestVerticle.class);
    }

    @Test
    @DisplayName("Unknown provider type")
    void createInstanceUnknown() {
        DispatcherFactory factory = new DispatcherFactory();
        Assertions.assertThatThrownBy(() -> {
                factory.createVerticle("unknown:" + TestVerticle.class.getName(),
                        Thread.currentThread().getContextClassLoader());
        }).isInstanceOf(ReflectiveOperationException.class);
    }

    @Test
    @DisplayName("Unknown class")
    void testUnknownClass() throws Exception {
        DispatcherFactory factory = new DispatcherFactory();
        Assertions.assertThatThrownBy(() -> {
            factory.createVerticle("oasis:a.b.c",
                    Thread.currentThread().getContextClassLoader());
        }).isInstanceOf(ReflectiveOperationException.class);
    }

    public static class TestVerticle extends AbstractVerticle {}
}
