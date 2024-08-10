package io.github.oasis.core.services.api.infrastructure.repository.jpa;

import io.github.oasis.core.services.api.infrastructure.repository.dto.AssignmentDto;
import io.github.oasis.core.services.api.infrastructure.repository.dto.AssignmentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository extends JpaRepository<AssignmentDto, AssignmentKey> {
}
