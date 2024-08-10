package io.github.oasis.core.services.api.infrastructure.repository.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class AssignmentKey {

    @Column(name = "game_id")
    private Integer gameId;

    @Column(name = "player_id")
    private Long playerId;

}
