package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;
import org.iesalixar.daw2.womhat.womhat.enums.DataCenterStatus;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para la tabla `data_centers`.
 *
 * Representa un CPD (Centro de Procesamiento de Datos).
 * Relación:
 *  - 1 CPD tiene muchas salas (data_center_rooms).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "rooms")
@Entity
@Table(
        name = "data_centers",
        uniqueConstraints = @UniqueConstraint(name = "uq_data_centers_code", columnNames = "code")
)
public class DataCenter {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** VARCHAR(30) NOT NULL UNIQUE */
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    /** VARCHAR(120) NOT NULL */
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    /** VARCHAR(80) NULL */
    @Column(name = "city", length = 80)
    private String city;

    /** VARCHAR(120) NULL */
    @Column(name = "building", length = 120)
    private String building;

    /** ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DataCenterStatus status = DataCenterStatus.ACTIVE;

    /**
     * Relación 1:N con salas.
     * mappedBy apunta al atributo 'dataCenter' en DataCenterRoom.
     */
    @OneToMany(mappedBy = "dataCenter", fetch = FetchType.LAZY)
    private Set<DataCenterRoom> rooms = new HashSet<>();
}
