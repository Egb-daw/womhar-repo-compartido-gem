package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para la tabla `data_center_rooms`.
 *
 * Representa una sala dentro de un CPD.
 * Relación:
 *  - Muchas salas pertenecen a un CPD (N:1).
 *  - Una sala tiene muchos racks (1:N).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"dataCenter", "racks"})
@Entity
@Table(name = "data_center_rooms")
public class DataCenterRoom {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK NOT NULL -> data_centers.id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "data_center_id", nullable = false)
    private DataCenter dataCenter;

    /** VARCHAR(80) NOT NULL */
    @Column(name = "name", nullable = false, length = 80)
    private String name;

    /** VARCHAR(20) NULL */
    @Column(name = "floor", length = 20)
    private String floor;

    /** VARCHAR(255) NULL */
    @Column(name = "notes", length = 255)
    private String notes;

    /** 1 sala -> N racks */
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<Rack> racks = new HashSet<>();
}
