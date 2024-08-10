package io.github.oasis.core.services.api.infrastructure.repository.jpa;

import io.github.oasis.core.services.api.infrastructure.repository.dto.RankDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeRepository extends JpaRepository<RankDto, Integer> {

    List<RankDto> findAllByGameId(int gameId);

}
