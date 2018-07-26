package io.github.isuru.oasis.game.process.sinks;

import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author iweerarathna
 */
public class OasisBadgesSink implements SinkFunction<BadgeNotification> {

    private IBadgeHandler badgeHandler;

    public OasisBadgesSink(IBadgeHandler badgeHandler) {
        this.badgeHandler = badgeHandler;
    }

    @Override
    public void invoke(BadgeNotification value, Context context) {
        badgeHandler.badgeReceived(value);
    }

}
