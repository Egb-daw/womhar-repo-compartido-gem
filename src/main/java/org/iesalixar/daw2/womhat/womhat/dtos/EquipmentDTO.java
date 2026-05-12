package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentType;

import java.time.LocalDateTime;

/**
 * DTO genérico de lectura para equipos.
 *
 * Pensado para listados y tablas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDTO {

    /** ID del equipo. */
    private Long id;

    /** ID del rack donde está instalado. */
    private Long rackId;

    /** Etiqueta del rack donde se encuentra. */
    private String rackLocationLabel;

    /** Nombre de la sala. */
    private String roomName;

    /** Código del CPD. */
    private String dataCenterCode;

    /** Nombre del equipo. */
    private String name;

    /** Tipo del equipo. */
    private EquipmentType type;

    /** Número de serie. */
    private String serialNumber;

    /** IP principal. */
    private String primaryIp;

    /** IP de gestión. */
    private String managementIp;

    /** VLAN asociada. */
    private Integer vlanId;

    /** Dirección MAC. */
    private String macAddress;

    /** Posición inicial en U dentro del rack. */
    private Integer slotPositionU;

    /** Altura en U del equipo. */
    private Integer slotHeightU;

    /** Estado del equipo. */
    private EquipmentStatus status;

    /** Última actualización del equipo. */
    private LocalDateTime lastUpdate;
}