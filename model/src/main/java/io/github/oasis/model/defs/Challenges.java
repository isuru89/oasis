package io.github.oasis.model.defs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents all challenges.
 */
public class Challenges implements Serializable {

    private final List<ChallengeDef> challengeDefinitions;

    public Challenges() {
        this.challengeDefinitions = new ArrayList<>();
    }

    public boolean isEmpty() {
        return challengeDefinitions.isEmpty();
    }

    public List<ChallengeDef> getChallengeDefinitions() {
        return challengeDefinitions;
    }
}
