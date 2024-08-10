package io.github.oasis.core.services.api.infrastructure.repository.jpa;

import io.github.oasis.core.services.api.infrastructure.repository.dto.EventSourceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventSourceRepository extends JpaRepository<EventSourceDto, Integer> {

    Optional<EventSourceDto> findByDisplayName(String name);
    Optional<EventSourceDto> findByToken(String token);

}
