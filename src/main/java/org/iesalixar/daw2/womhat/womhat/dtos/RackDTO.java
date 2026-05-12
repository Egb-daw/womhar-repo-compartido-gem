package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO genérico de lectura para racks.
 *
 * Pensado para listados y tablas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RackDTO {

    /** ID del rack. */
    private Long id;

    /** ID de la sala. */
    private Long roomId;

    /** Nombre de la sala. */
    private String roomName;

    /** Código del CPD donde se encuentra. */
    private String dataCenterCode;

    /** Nombre del CPD donde se encuentra. */
    private String dataCenterName;

    /** Etiqueta visible del rack. */
    private String locationLabel;

    /** Capacidad total en U. */
    private Integer capacityU;

    /** Función principal del rack. */
    private String functionName;

    /** Grupo lógico del rack. */
    private String groupName;

    /** Dimensión o formato del rack. */
    private String dimension;

    /** Posición X en el mapa de sala. */
    private Integer positionX;

    /** Posición Y en el mapa de sala. */
    private Integer positionY;

    /** Estado actual del rack. */
    private RackStatus status;

    /** Número de equipos instalados. */
    private Integer equipmentCount;

    /** Publicación activa en catálogo. */
    private boolean catalogVisible;

    /** Precio unitario público. */
    private BigDecimal catalogPrice;

    /** Stock disponible. */
    private Integer catalogStock;

    /** Resumen comercial corto. */
    private String catalogSummary;

    /** Nombre comercial mostrado en catálogo (modelo/tipo de rack). */
    private String catalogDisplayName;

    /** Lista de equipos instalados en este rack. */
    private List<EquipmentDTO> equipments;
}
