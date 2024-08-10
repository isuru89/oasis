package io.github.oasis.core.services.api.infrastructure.repository.dto;

import io.github.oasis.core.Game;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "OA_GAME")
public class GameDto extends BaseDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @EqualsAndHashCode.Include
    @Column(unique = true, nullable = false)
    private String name;
    private String motto;
    private String description;

    @Column(name = "logo_ref")
    private String logoRef;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "OA_GAME_STATUS", joinColumns = @JoinColumn(name ="game_id"))
    private List<GameStateDto> status;

    @OneToMany(mappedBy = "game")
    private Set<RankDto> ranks;

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TeamDto> teams;

    @OneToMany(mappedBy = "game" ,fetch = FetchType.LAZY)
    private Set<AssignmentDto> players;

    @ManyToMany(mappedBy = "games", fetch = FetchType.LAZY)
    private Set<EventSourceDto> eventSources;

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
    private Set<RuleDto> rules;

    public Game toGame() {
        return Game.builder()
                .id(id)
                .name(name)
                .motto(motto)
                .description(description)
                .updatedAt(getUpdatedAt().toEpochMilli())
                .createdAt(getCreatedAt().toEpochMilli())
                .active(getIsActive())
                .build();
    }

}
