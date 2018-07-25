package io.github.isuru.oasis.game.process.challenge;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class ChallengeContext implements Serializable {

    private SourceFunction<Event> sourceFunction;
    private ChallengeDef challengeDef;

    public ChallengeContext(ChallengeDef def, SourceFunction<Event> sourceFunction) {
        this.sourceFunction = sourceFunction;
        this.challengeDef = def;
    }

    public void challengeStopped() {
        sourceFunction.cancel();
    }

    public ChallengeDef getChallengeDef() {
        return challengeDef;
    }
}
