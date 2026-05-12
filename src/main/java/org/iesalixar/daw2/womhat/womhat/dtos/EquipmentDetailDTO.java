package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de detalle completo para equipos.
 *
 * Incluye la información base, la especialización 1:1
 * y las órdenes de mantenimiento relacionadas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDetailDTO {

    /** ID del equipo. */
    private Long id;

    /** ID del rack donde está instalado. */
    private Long rackId;

    /** Etiqueta del rack. */
    private String rackLocationLabel;

    /** Nombre de la sala. */
    private String roomName;

    /** Código del CPD. */
    private String dataCenterCode;

    /** Nombre del CPD. */
    private String dataCenterName;

    /** Nombre visible del equipo. */
    private String name;

    /** Tipo del equipo. */
    private EquipmentType type;

    /** Número de serie único. */
    private String serialNumber;

    /** IP principal. */
    private String primaryIp;

    /** IP de gestión. */
    private String managementIp;

    /** VLAN. */
    private Integer vlanId;

    /** Dirección MAC. */
    private String macAddress;

    /** Posición inicial en U. */
    private Integer slotPositionU;

    /** Altura total en U. */
    private Integer slotHeightU;

    /** Estado actual. */
    private EquipmentStatus status;

    /** Fecha/hora de última actualización. */
    private LocalDateTime lastUpdate;

    /** Especificaciones de host si aplica. */
    private HostSpecificationDTO hostSpecification;

    /** Datos de red si aplica. */
    private NetworkElementDTO networkElement;

    /** Datos de storage si aplica. */
    private StorageBackupDTO storageBackup;

    /** Órdenes de mantenimiento del equipo. */
    private List<MaintenanceWorkOrderDTO> workOrders = new ArrayList<>();
}