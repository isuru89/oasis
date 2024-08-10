package io.github.oasis.core.external.storage;

import io.github.oasis.core.elements.RankInfo;

import java.util.List;

public interface RankDao {


    RankInfo addRank(int gameId, RankInfo newRank);
    List<RankInfo> listAllRanks(int gameId);


}
