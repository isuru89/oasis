package io.github.oasis.core.services.api.infrastructure.repository.jpa;

import io.github.oasis.core.services.api.infrastructure.repository.dto.RuleDataDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleDataRepository extends JpaRepository<RuleDataDto, Long> {
}
