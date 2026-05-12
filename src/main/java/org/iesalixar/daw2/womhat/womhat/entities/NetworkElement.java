package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;
import org.iesalixar.daw2.womhat.womhat.enums.NetworkConnectionType;

/**
 * Entidad JPA para `network_elements`.
 *
 * 1:1 con equipments por PK compartida:
 *  - network_elements.equipment_id es PK y FK -> equipments.id
 *
 * Existe normalmente para SWITCH / ROUTER / FIREWALL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "equipment")
@Entity
@Table(name = "network_elements")
public class NetworkElement {

    /** PK compartida con equipments.id */
    @Id
    @Column(name = "equipment_id")
    private Long id;

    /** @MapsId => id = equipment.id */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    /** ENUM('ETHERNET','FIBER','WIFI','OTHER') NOT NULL DEFAULT 'ETHERNET' */
    @Enumerated(EnumType.STRING)
    @Column(name = "connection", nullable = false, length = 20)
    private NetworkConnectionType connection = NetworkConnectionType.ETHERNET;

    /** INT NULL (CHECK total_ports IS NULL OR >= 0) */
    @Column(name = "total_ports")
    private Integer totalPorts;
}
