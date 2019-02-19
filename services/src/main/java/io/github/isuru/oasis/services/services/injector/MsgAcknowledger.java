package io.github.isuru.oasis.services.services.injector;

import java.io.IOException;

@FunctionalInterface
public interface MsgAcknowledger {

    void ack(long tag) throws IOException;

}
