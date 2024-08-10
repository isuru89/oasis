package io.github.oasis.core.services.api.infrastructure.repository.dto;

import io.github.oasis.core.model.UserGender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "OA_PLAYER")
public class PlayerDto extends BaseDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name")
    private String displayName;

    @Column(unique = true)
    @EqualsAndHashCode.Include
    private String email;

    @Column(name = "avatar_ref")
    private String avatarRef;
    private String timezone;
    private UserGender gender;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
    private Set<AssignmentDto> teams;

}
