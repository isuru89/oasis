package io.github.oasis.core.services.api.infrastructure.repository.dto;

import io.github.oasis.core.model.EventSourceSecrets;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

import java.util.Base64;

@Getter
@Setter
@Builder(toBuilder = true)
@With
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "OA_EVENT_SOURCE_KEY")
public class EventSourceSecretsDto {

    @Id
    private Integer id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "event_source_id")
    private EventSourceDto application;

    @Lob
    @Column(name = "public_key", columnDefinition = "BLOB")
    private byte[] publicKey;
    @Lob
    @Column(name = "private_key", columnDefinition = "BLOB")
    private byte[] privateKey;

    @Column(name = "download_count")
    private Integer downloadCount;

    public EventSourceSecrets toEventSourceSecrets(boolean withPrivateKey) {
        var secrets = EventSourceSecrets.builder()
                .publicKey(Base64.getEncoder().encodeToString(publicKey))
                .viewedTimes(downloadCount);

        if (withPrivateKey) {
            secrets = secrets.privateKey(Base64.getEncoder().encodeToString(privateKey));
        }

        return secrets.build();
    }

}
