package io.github.oasis.core.services.api.infrastructure.repository.jpa;

import io.github.oasis.core.services.api.infrastructure.repository.dto.PlayerRegistration;
import io.github.oasis.core.services.api.infrastructure.repository.dto.RegistrationKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends JpaRepository<PlayerRegistration, RegistrationKey> {
}
