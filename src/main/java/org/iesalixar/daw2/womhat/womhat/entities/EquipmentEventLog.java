package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction;

import java.time.LocalDateTime;

/**
 * Entidad JPA para `equipment_event_log`.
 *
 * Esta tabla almacena auditoría de eventos sobre equipos.
 * A tener en cuenta:
 *  - equipment_id es NULLABLE => relación opcional.
 *  - changed_by_user_id también es NULLABLE => relación opcional.
 *  - old_rack_id/new_rack_id NO tienen FK en el schema => aquí se mapean como Long simples.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"equipment", "changedBy"})
@Entity
@Table(name = "equipment_event_log")
public class EquipmentEventLog {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK opcional -> equipments.id (ON DELETE SET NULL) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    /** FK opcional -> users.id (ON DELETE SET NULL) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    /** ENUM('INSERT','UPDATE','DELETE') NOT NULL */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 10)
    private EquipmentLogAction action;

    /** ENUM('CREATED','UPDATED','MOVED_RACK','STATUS_CHANGED','DELETED') NOT NULL */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EquipmentEventType eventType;

    /** BIGINT NULL (sin FK) */
    @Column(name = "old_rack_id")
    private Long oldRackId;

    /** BIGINT NULL (sin FK) */
    @Column(name = "new_rack_id")
    private Long newRackId;

    /** VARCHAR(30) NULL */
    @Column(name = "old_status", length = 30)
    private String oldStatus;

    /** VARCHAR(30) NULL */
    @Column(name = "new_status", length = 30)
    private String newStatus;

    /** VARCHAR(255) NULL */
    @Column(name = "message", length = 255)
    private String message;

    /** DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP */
    @Column(name = "changed_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime changedAt;
}
