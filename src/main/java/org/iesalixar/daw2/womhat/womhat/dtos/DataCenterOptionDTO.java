package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simple para poblar selects de CPDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterOptionDTO {

    /** ID del CPD. */
    private Long id;

    /** Código del CPD. */
    private String code;

    /** Nombre visible del CPD. */
    private String name;
}