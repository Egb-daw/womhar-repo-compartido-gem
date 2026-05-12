package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA para `host_specifications`.
 *
 * Tabla 1:1 con equipments por PRIMARY KEY compartida:
 *  - host_specifications.equipment_id es PK y también FK -> equipments.id
 *
 * Solo existe fila para equipos tipo SERVER.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "equipment")
@Entity
@Table(name = "host_specifications")
public class HostSpecification {

    /** PK compartida con equipments.id */
    @Id
    @Column(name = "equipment_id")
    private Long id;

    /** @MapsId => id = equipment.id */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "operating_system", length = 120)
    private String operatingSystem;

    @Column(name = "purpose", length = 120)
    private String purpose;

    @Column(name = "install_date")
    private LocalDate installDate;

    @Column(name = "cpu_architecture", length = 80)
    private String cpuArchitecture;

    @Column(name = "cpu_model", length = 120)
    private String cpuModel;

    @Column(name = "cpu_cores")
    private Integer cpuCores;

    @Column(name = "cpu_cache_mb")
    private Integer cpuCacheMb;

    /** DECIMAL(6,2) */
    @Column(name = "cpu_ghz", precision = 6, scale = 2)
    private BigDecimal cpuGhz;

    @Column(name = "ram_type", length = 60)
    private String ramType;

    @Column(name = "ram_total_gb")
    private Integer ramTotalGb;

    /** DECIMAL(6,2) */
    @Column(name = "ram_ghz", precision = 6, scale = 2)
    private BigDecimal ramGhz;

    @Column(name = "disk_total_gb")
    private Integer diskTotalGb;

    @Column(name = "disk_read_mbps")
    private Integer diskReadMbps;

    @Column(name = "disk_write_mbps")
    private Integer diskWriteMbps;

    @Column(name = "nic_count")
    private Integer nicCount;

    @Column(name = "nic_speed_mbps")
    private Integer nicSpeedMbps;

    @Column(name = "notes", length = 255)
    private String notes;
}
