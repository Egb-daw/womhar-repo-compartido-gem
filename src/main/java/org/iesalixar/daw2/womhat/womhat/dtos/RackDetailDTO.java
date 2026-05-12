package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de detalle para racks.
 *
 * Incluye datos calculados de ocupación y equipos asociados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RackDetailDTO {

    /** ID del rack. */
    private Long id;

    /** ID de la sala. */
    private Long roomId;

    /** Nombre de la sala. */
    private String roomName;

    /** Planta de la sala. */
    private String floor;

    /** Código del CPD. */
    private String dataCenterCode;

    /** Nombre del CPD. */
    private String dataCenterName;

    /** Etiqueta del rack. */
    private String locationLabel;

    /** Capacidad total en U. */
    private Integer capacityU;

    /** Función principal del rack. */
    private String functionName;

    /** Grupo lógico. */
    private String groupName;

    /** Dimensión del rack. */
    private String dimension;

    /** Posición X. */
    private Integer positionX;

    /** Posición Y. */
    private Integer positionY;

    /** Estado del rack. */
    private RackStatus status;

    /** Número de equipos instalados. */
    private Integer equipmentCount;

    /** U ocupadas actualmente. */
    private Integer occupiedU;

    /** U libres. */
    private Integer freeU;

    /** Porcentaje de ocupación. */
    private Integer occupancyPercent;

    /** Equipos del rack en formato compacto. */
    private List<EquipmentCompactDTO> equipments = new ArrayList<>();

    /** Usuarios con acceso específico a este rack. */
    private List<UserRackAccessDTO> userAccessList = new ArrayList<>();

    /** Indica si el rack está publicado en catálogo. */
    private boolean catalogVisible;

    /** Precio unitario público. */
    private BigDecimal catalogPrice;

    /** Stock disponible para compra. */
    private Integer catalogStock;

    /** Resumen corto de la ficha pública. */
    private String catalogSummary;

    /** Nombre comercial mostrado en catálogo (modelo/tipo de rack). */
    private String catalogDisplayName;
}
