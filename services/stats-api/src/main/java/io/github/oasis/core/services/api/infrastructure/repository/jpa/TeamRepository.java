package io.github.oasis.core.services.api.infrastructure.repository.jpa;

import io.github.oasis.core.services.api.infrastructure.repository.dto.TeamDto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<TeamDto, Integer> {

    @Override
    Optional<TeamDto> findById(Integer integer);

    @Override
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    <S extends TeamDto> S save(S entity);
}
