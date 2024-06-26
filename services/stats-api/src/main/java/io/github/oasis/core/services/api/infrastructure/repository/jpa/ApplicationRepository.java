package io.github.oasis.core.services.api.infrastructure.repository.jpa;

import io.github.oasis.core.services.api.infrastructure.repository.dto.ApplicationDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationDto, Integer> {

    Optional<ApplicationDto> findByDisplayName(String name);
    Optional<ApplicationDto> findByToken(String token);

}
