package io.github.isuru.oasis.services.services.injector;

public interface IConsumer<T> {

    void handleMessage(byte[] body, Object deliveryTag);

}
