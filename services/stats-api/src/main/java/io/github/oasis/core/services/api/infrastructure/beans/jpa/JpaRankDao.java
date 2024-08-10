package io.github.oasis.core.services.api.infrastructure.beans.jpa;

import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.external.storage.RankDao;
import io.github.oasis.core.services.api.infrastructure.repository.dto.RankDto;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.AttributeRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.GameRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
class JpaRankDao implements RankDao {

    private final GameRepository gameRepository;
    private final AttributeRepository attributeRepository;

    JpaRankDao(GameRepository gameRepository, AttributeRepository attributeRepository) {
        this.gameRepository = gameRepository;
        this.attributeRepository = attributeRepository;
    }

    @Transactional
    @Override
    public RankInfo addRank(int gameId, RankInfo newRank) {
        var gameRef = gameRepository.getReferenceById(gameId);

        RankDto dto = RankDto.builder()
                .game(gameRef)
                .priority(newRank.getPriority())
                .colorCode(newRank.getColorCode())
                .name(newRank.getName())
                .build();

        return attributeRepository.save(dto).toRank();
    }

    @Override
    public List<RankInfo> listAllRanks(int gameId) {
        return attributeRepository.findAllByGameId(gameId)
                .stream()
                .map(RankDto::toRank)
                .collect(Collectors.toList());
    }
}
