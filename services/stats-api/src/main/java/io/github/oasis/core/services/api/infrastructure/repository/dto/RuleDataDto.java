package io.github.oasis.core.services.api.infrastructure.repository.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "OA_ELEMENT_DATA")
@SQLDelete(sql = "UPDATE OA_ELEMENT_DATA SET is_active = false WHERE rule_id = ?")
@Where(clause = "is_active = false")
public class RuleDataDto {

    @Id
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "element_id")
    private RuleDto rule;

    @Lob
    @Column(name = "def_data", columnDefinition = "BLOB")
    private byte[] data;

    @Column(name = "is_active")
    private Boolean isActive;

}
