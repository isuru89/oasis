package io.github.isuru.oasis.services.utils.local;

import io.github.isuru.oasis.game.Main;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.model.events.JsonEvent;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class LocalRunner implements Runnable {

    private Configs appConfigs;
    private final long gameId;
    private QueueSource queueSource;
    private IOasisDao dao;

    LocalRunner(Configs appConfigs, IOasisDao dao, long gameId) {
        this.appConfigs = appConfigs;
        this.gameId = gameId;
        this.dao = dao;
    }

    @Override
    public void run() {
        // @TODO configs
        Configs configs = Configs.from(appConfigs.getProps());
        queueSource = new QueueSource();
        DbSink dbSink = new DbSink(dao, gameId);

        configs.append(ConfigKeys.KEY_LOCAL_REF_SOURCE, queueSource);
        configs.append(ConfigKeys.KEY_LOCAL_REF_OUTPUT, dbSink);
        OasisGameDef gameDef = new OasisGameDef();

        try {
            Main.startGame(configs, gameDef);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stop() throws InterruptedException {
        if (queueSource != null) {
            queueSource.append(new LocalEndEvent());
        }
    }

    void submitEvent(Map<String, Object> event) throws Exception {
        if (queueSource != null) {
            JsonEvent jsonEvent = new JsonEvent();
            jsonEvent.putAll(event);
            queueSource.append(jsonEvent);
        }
    }

    public long getGameId() {
        return gameId;
    }
}
