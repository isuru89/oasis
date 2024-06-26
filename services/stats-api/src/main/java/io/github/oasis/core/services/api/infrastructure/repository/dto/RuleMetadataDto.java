package io.github.oasis.core.services.api.infrastructure.repository.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Embeddable
public class RuleMetadataDto {

    @EqualsAndHashCode.Include
    private String ruleId;

    private String ruleName;
    private String ruleDescription;

}
