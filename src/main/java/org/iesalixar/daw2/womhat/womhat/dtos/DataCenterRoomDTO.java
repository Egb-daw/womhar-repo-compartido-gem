package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico de lectura para salas de CPD.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterRoomDTO {

    /** ID de la sala. */
    private Long id;

    /** ID del CPD padre. */
    private Long dataCenterId;

    /** Código del CPD padre. */
    private String dataCenterCode;

    /** Nombre del CPD padre. */
    private String dataCenterName;

    /** Nombre de la sala. */
    private String name;

    /** Planta o nivel. */
    private String floor;

    /** Notas de la sala. */
    private String notes;

    /** Número de racks registrados en la sala. */
    private Integer rackCount;
}