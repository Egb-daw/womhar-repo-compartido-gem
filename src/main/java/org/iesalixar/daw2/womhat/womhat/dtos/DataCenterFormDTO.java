package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.DataCenterStatus;

/**
 * DTO de formulario para crear y editar CPDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterFormDTO {

    /** ID del CPD. En alta será null. */
    private Long id;

    /** Código único del CPD. */
    @NotBlank(message = "{msg.data-center-form.code.notblank}")
    @Size(max = 30, message = "{msg.data-center-form.code.size}")
    private String code;

    /** Nombre del CPD. */
    @NotBlank(message = "{msg.data-center-form.name.notblank}")
    @Size(max = 120, message = "{msg.data-center-form.name.size}")
    private String name;

    /** Ciudad opcional. */
    @Size(max = 80, message = "{msg.data-center-form.city.size}")
    private String city;

    /** Edificio opcional. */
    @Size(max = 120, message = "{msg.data-center-form.building.size}")
    private String building;

    /** Estado del CPD. */
    @NotNull(message = "{msg.data-center-form.status.notnull}")
    private DataCenterStatus status = DataCenterStatus.ACTIVE;
}
