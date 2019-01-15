package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.game.Main;
import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.services.control.sinks.DbSink;
import io.github.isuru.oasis.services.services.control.sinks.LocalSinks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * @author iweerarathna
 */
class LocalRunner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LocalRunner.class);

    private final long gameId;
    private IOasisDao dao;
    private LocalSinks localSinks;
    private ExecutorService pool;
    private DbSink oasisSink;
    private DataCache dataCache;
    private OasisConfigurations oasisConfigurations;

    LocalRunner(OasisConfigurations configurations,
                ExecutorService pool,
                IOasisDao dao,
                long gameId,
                DataCache dataCache) {
        this.oasisConfigurations = configurations;
        this.gameId = gameId;
        this.dao = dao;
        this.pool = pool;
        this.oasisSink = new DbSink(gameId);
        this.dataCache = dataCache;
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

        QueueSource queueSource = new QueueSource(gameId);
        localSinks = LocalSinks.applySinks(pool, oasisSink, dao, gameId);

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
        if (localSinks != null) {
            localSinks.stop();
        }
        Sources.get().finish(gameId);
    }

    void submitEvent(Map<String, Object> event) throws Exception {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.putAll(event);
        Sources.get().poll(gameId).put(jsonEvent);
    }

    public long getGameId() {
        return gameId;
    }

    OasisSink getOasisSink() {
        return oasisSink;
    }
}
