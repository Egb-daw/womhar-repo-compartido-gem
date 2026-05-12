package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;
import org.iesalixar.daw2.womhat.womhat.enums.StorageType;

import java.time.LocalDate;

/**
 * Entidad JPA para `storage_backups`.
 *
 * 1:1 con equipments por PK compartida:
 *  - storage_backups.equipment_id es PK y FK -> equipments.id
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "equipment")
@Entity
@Table(name = "storage_backups")
public class StorageBackup {

    /** PK compartida con equipments.id */
    @Id
    @Column(name = "equipment_id")
    private Long id;

    /** @MapsId => id = equipment.id */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    /** VARCHAR(120) NULL */
    @Column(name = "distribution_type", length = 120)
    private String distributionType;

    /** DATE NULL */
    @Column(name = "install_date")
    private LocalDate installDate;

    /** ENUM('NAS','SAN','BACKUP','OBJECT','OTHER') NOT NULL DEFAULT 'BACKUP' */
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    private StorageType storageType = StorageType.BACKUP;
}
