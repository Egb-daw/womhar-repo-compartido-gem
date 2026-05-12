package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;

/**
 * DTO para pintar cada rack dentro del mapa del dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRackMapItemDTO {

    /** ID del rack. */
    private Long rackId;

    /** Etiqueta visible del rack. */
    private String locationLabel;

    /** Nombre de la sala. */
    private String roomName;

    /** Nombre del CPD asociado (si existe). */
    private String dataCenterName;

    /** Posición X del rack. */
    private Integer positionX;

    /** Posición Y del rack. */
    private Integer positionY;

    /** Capacidad total en U. */
    private Integer capacityU;

    /** U ocupadas. */
    private Integer occupiedU;

    /** U libres. */
    private Integer freeU;

    /** Porcentaje de ocupación. */
    private Integer occupancyPercent;

    /** Número de equipos instalados. */
    private Integer equipmentCount;

    /** Estado del rack. */
    private RackStatus status;

    /** Nivel de acceso del usuario sobre el rack (null para vista ADMIN global). */
    private RackPermission accessPermission;

    /** Indica si el usuario es propietario funcional original de ese rack. */
    private boolean originalOwner;
}
