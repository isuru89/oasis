package io.github.oasis.core.services.api.infrastructure.repository.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "OA_TEAM")
public class TeamDto extends BaseDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    @EqualsAndHashCode.Include
    private String name;

    @Column(name = "color_code")
    private String colorCode;

    @Column(name = "avatar_ref")
    private String avatarRef;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameDto game;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private Set<AssignmentDto> players;

}
