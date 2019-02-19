package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.game.Main;
import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * @author iweerarathna
 */
class LocalRunner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LocalRunner.class);

    private final long gameId;
    private OasisSink oasisSink;
    private DataCache dataCache;
    private Sources sources;
    private OasisConfigurations oasisConfigurations;

    LocalRunner(OasisConfigurations configurations,
                long gameId,
                DataCache dataCache,
                Sources sources,
                OasisSink oasisSink) {
        this.oasisConfigurations = configurations;
        this.gameId = gameId;
        this.oasisSink = oasisSink;
        this.dataCache = dataCache;
        this.sources = sources;
    }

    @Override
    public void run() {
        // setup explicit configs
        Map<String, Object> localRunProps = oasisConfigurations.getLocalRun();

        // @TODO convert OasisConfiguration object to Map and pass it here
        Configs configs = Configs.from(new Properties());
        for (Map.Entry<String, Object> entry : localRunProps.entrySet()) {
            configs.append(entry.getKey(), entry.getValue());
        }

        QueueSource queueSource = new QueueSource(sources, gameId);

        configs.append(ConfigKeys.KEY_LOCAL_REF_SOURCE, queueSource);
        configs.append(ConfigKeys.KEY_LOCAL_REF_OUTPUT, oasisSink);

        try {
            OasisGameDef gameDef = dataCache.loadGameDefs(gameId);
            Main.startGame(configs, gameDef);
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    void stop() throws InterruptedException {
        sources.finish(gameId);
    }

    void submitEvent(Map<String, Object> event) throws Exception {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.putAll(event);
        sources.poll(gameId).put(jsonEvent);
    }

    public long getGameId() {
        return gameId;
    }

}
