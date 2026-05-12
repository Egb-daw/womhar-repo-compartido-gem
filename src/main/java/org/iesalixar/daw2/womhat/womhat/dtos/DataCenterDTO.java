package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.DataCenterStatus;

/**
 * DTO genérico de lectura para CPD.
 *
 * Pensado para listados y tablas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterDTO {

    /** ID del CPD. */
    private Long id;

    /** Código único del CPD. */
    private String code;

    /** Nombre del CPD. */
    private String name;

    /** Ciudad donde se ubica. */
    private String city;

    /** Edificio o centro donde se encuentra. */
    private String building;

    /** Estado del CPD. */
    private DataCenterStatus status;

    /** Número de salas asociadas al CPD. */
    private Integer roomCount;
}