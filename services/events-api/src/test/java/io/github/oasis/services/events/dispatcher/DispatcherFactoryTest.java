package io.github.oasis.services.events.dispatcher;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Dispatcher Factory Test")
public class DispatcherFactoryTest {

    private <T> T waitWhile(Promise<T> promise) throws InterruptedException {
        Future<T> future = promise.future();
        while (!future.isComplete()) {
            Thread.sleep(20);
        }
        return future.result();
    }

    private <T> Throwable waitWhileFail(Promise<T> promise) throws InterruptedException {
        Future<T> future = promise.future();
        while (!future.isComplete()) {
            Thread.sleep(20);
        }
        return future.cause();
    }

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
        Promise<Callable<Verticle>> promise = Promise.promise();
        factory.createVerticle("oasis:" + TestVerticle.class.getName(),
                Thread.currentThread().getContextClassLoader(), promise);
        Callable<Verticle> verticleCallable = waitWhile(promise);
        Assertions.assertThat(verticleCallable.call()).isExactlyInstanceOf(TestVerticle.class);
    }

    @Test
    @DisplayName("Unknown provider type")
    void createInstanceUnknown() throws InterruptedException {
        DispatcherFactory factory = new DispatcherFactory();
        Promise<Callable<Verticle>> promise = Promise.promise();
        factory.createVerticle("unknown:" + TestVerticle.class.getName(),
                Thread.currentThread().getContextClassLoader(), promise);
        Throwable throwable = waitWhileFail(promise);
        Assertions.assertThat(throwable).isInstanceOf(ReflectiveOperationException.class);
    }

    @Test
    @DisplayName("Unknown class")
    void testUnknownClass() throws Exception {
        DispatcherFactory factory = new DispatcherFactory();
        Promise<Callable<Verticle>> promise = Promise.promise();
        factory.createVerticle("oasis:a.b.c",
                Thread.currentThread().getContextClassLoader(), promise);
        Throwable throwable = waitWhileFail(promise);
        Assertions.assertThat(throwable).isInstanceOf(ReflectiveOperationException.class);
    }

    public static class TestVerticle extends AbstractVerticle {}
}
