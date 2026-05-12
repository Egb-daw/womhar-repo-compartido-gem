package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de formulario para crear y editar salas de CPD.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterRoomFormDTO {

    /** ID de la sala. En alta será null. */
    private Long id;

    /** ID del CPD al que pertenece la sala. */
    @NotNull(message = "{msg.data-center-room-form.dataCenterId.notnull}")
    private Long dataCenterId;

    /** Nombre visible de la sala. */
    @NotBlank(message = "{msg.data-center-room-form.name.notblank}")
    @Size(max = 80, message = "{msg.data-center-room-form.name.size}")
    private String name;

    /** Planta o nivel de la sala. */
    @Size(max = 20, message = "{msg.data-center-room-form.floor.size}")
    private String floor;

    /** Notas opcionales sobre la sala. */
    @Size(max = 255, message = "{msg.data-center-room-form.notes.size}")
    private String notes;
}
