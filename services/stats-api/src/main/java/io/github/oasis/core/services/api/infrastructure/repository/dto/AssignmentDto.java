package io.github.oasis.core.services.api.infrastructure.repository.dto;


import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "OA_PLAYER_TEAM")
public class AssignmentDto {

    @EmbeddedId
    private AssignmentKey id;

    @ManyToOne
    @MapsId("gameId")
    @JoinColumn(name = "game_id")
    private GameDto game;

    @ManyToOne
    @MapsId("playerId")
    @JoinColumn(name = "player_id")
    private PlayerDto player;

    @ManyToOne
    @JoinColumn(name = "team_id", updatable = false)
    private TeamDto team;

    @Column(name = "created_at", updatable = false)
    private Instant registeredAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = Instant.now();
    }

}
