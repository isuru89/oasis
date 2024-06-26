package io.github.oasis.core.services.api.infrastructure.repository.dto;

import io.github.oasis.core.model.EventSource;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hibernate.annotations.SQLDelete;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@With
@Entity
@Table(name = "OA_EVENT_SOURCE")
@SQLDelete(sql = "UPDATE OA_EVENT_SOURCE SET is_active = false WHERE id = ?")
public class ApplicationDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @EqualsAndHashCode.Include
    @Column(unique = true, updatable = false, nullable = false)
    private String token;

    @Column(name = "display_name")
    private String displayName;

    @OneToOne(mappedBy = "application", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ApplicationSecretDto secrets;

    @ManyToMany
    @JoinTable(
            name = "OA_EVENT_SOURCE_GAME",
            joinColumns = @JoinColumn(name = "event_source_id"),
            inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private Set<GameDto> games;

    @Column(name = "is_active")
    private Boolean isActive;

    @Version
    private Integer version;

    @PrePersist
    protected void onCreate() {
        isActive = true;
        version = 1;
    }

    public EventSource toEventSource() {
        var builder = EventSource.builder()
                .id(id)
                .name(displayName)
                .token(token)
                .active(isActive);

        if (games != null) {
            builder = builder.games(games.stream().map(GameDto::getId).collect(Collectors.toSet()));
        }

        return builder.build();
    }

}
