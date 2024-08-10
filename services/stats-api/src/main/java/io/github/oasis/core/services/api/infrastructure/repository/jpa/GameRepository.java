package io.github.oasis.core.services.api.infrastructure.repository.jpa;

import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisEntityNotFoundException;
import io.github.oasis.core.services.api.infrastructure.repository.dto.GameDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<GameDto, Integer> {

    Optional<GameDto> findByName(String name);

    default GameDto mustGetById(int id) {
        return findById(id).orElseThrow(() -> new OasisEntityNotFoundException(ErrorCodes.GAME_NOT_EXISTS));
    }

}
