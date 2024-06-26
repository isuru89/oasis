package io.github.oasis.core.services.api.infrastructure.repository.dto;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@With
@Entity
@Table(name = "OA_ELEMENT")
public class RuleDto extends BaseDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "ruleId", column = @Column(name = "def_id", unique = true)),
            @AttributeOverride(name = "ruleName", column = @Column(name = "name")),
            @AttributeOverride(name = "ruleDescription", column = @Column(name = "description")),
    })
    private RuleMetadataDto definition;

    @OneToOne(mappedBy = "rule")
    private RuleDataDto data;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameDto game;

}
