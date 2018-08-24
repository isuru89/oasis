package io.github.isuru.oasis.services.utils.local;

import io.github.isuru.oasis.game.Main;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.utils.local.sinks.DbSink;
import io.github.isuru.oasis.services.utils.local.sinks.LocalSinks;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * @author iweerarathna
 */
public class LocalRunner implements Runnable {

    private Configs appConfigs;
    private final long gameId;
    private IOasisDao dao;
    private LocalSinks localSinks;
    private ExecutorService pool;

    LocalRunner(Configs appConfigs, ExecutorService pool, IOasisDao dao, long gameId) {
        this.appConfigs = appConfigs;
        this.gameId = gameId;
        this.dao = dao;
        this.pool = pool;
    }

    @Override
    public void run() {
        // setup explicit configs
        Properties props = appConfigs.getProps();
        Map<String, Object> localProps = OasisUtils.filterKeys(props, "oasis.localrun.");

        Configs configs = Configs.from(props);
        for (Map.Entry<String, Object> entry : localProps.entrySet()) {
            configs.append(entry.getKey(), entry.getValue());
        }

        QueueSource queueSource = new QueueSource(gameId);
        DbSink dbSink = new DbSink(gameId);
        localSinks = LocalSinks.applySinks(pool, dbSink, dao, gameId);

        configs.append(ConfigKeys.KEY_LOCAL_REF_SOURCE, queueSource);
        configs.append(ConfigKeys.KEY_LOCAL_REF_OUTPUT, dbSink);

        try {
            OasisGameDef gameDef = DataCache.get().loadGameDefs(gameId);
            Main.startGame(configs, gameDef);
        } catch (Throwable e) {
            e.printStackTrace();
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
}
