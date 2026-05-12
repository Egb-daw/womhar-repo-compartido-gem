package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * DTO de formulario para crear y editar racks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RackFormDTO {

    /** ID del rack. En alta será null. */
    private Long id;

    /** Sala donde se ubica el rack. */
    @NotNull(message = "{msg.rack-form.roomId.notnull}")
    private Long roomId;

    /** Etiqueta visible y única del rack. */
    @NotBlank(message = "{msg.rack-form.locationLabel.notblank}")
    @Size(max = 150, message = "{msg.rack-form.locationLabel.size}")
    private String locationLabel;

    /** Capacidad total del rack en U. */
    @NotNull(message = "{msg.rack-form.capacityU.notnull}")
    @Min(value = 1, message = "{msg.rack-form.capacityU.min}")
    private Integer capacityU;

    /** Función principal del rack. */
    @NotBlank(message = "{msg.rack-form.functionName.notblank}")
    @Size(max = 100, message = "{msg.rack-form.functionName.size}")
    private String functionName;

    /** Grupo lógico opcional. */
    @Size(max = 60, message = "{msg.rack-form.groupName.size}")
    private String groupName;

    /** Dimensión o formato opcional. */
    @Size(max = 60, message = "{msg.rack-form.dimension.size}")
    private String dimension;

    /** Posición X del rack en el mapa de sala. */
    @NotNull(message = "{msg.rack-form.positionX.notnull}")
    @Min(value = 0, message = "{msg.rack-form.positionX.min}")
    private Integer positionX = 0;

    /** Posición Y del rack en el mapa de sala. */
    @NotNull(message = "{msg.rack-form.positionY.notnull}")
    @Min(value = 0, message = "{msg.rack-form.positionY.min}")
    private Integer positionY = 0;

    /** Estado del rack. */
    @NotNull(message = "{msg.rack-form.status.notnull}")
    private RackStatus status = RackStatus.ACTIVE;

    /** Publicación del rack en el catálogo público. */
    private boolean catalogVisible;

    /** Precio unitario público. */
    @DecimalMin(value = "0.0", inclusive = true, message = "{msg.rack-form.catalogPrice.min}")
    private BigDecimal catalogPrice;

    /** Stock disponible para pedidos. */
    @NotNull(message = "{msg.rack-form.catalogStock.notnull}")
    @Min(value = 0, message = "{msg.rack-form.catalogStock.min}")
    private Integer catalogStock = 0;

    /** Resumen corto de la ficha pública. */
    @Size(max = 255, message = "{msg.rack-form.catalogSummary.size}")
    private String catalogSummary;
}
