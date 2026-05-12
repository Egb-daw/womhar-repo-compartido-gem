package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Formulario para crear pedidos desde el catálogo público.
 */
@Data
public class RackPurchaseOrderFormDTO {

    @NotNull(message = "{msg.catalog.order.rack.required}")
    @Positive(message = "{msg.catalog.order.rack.invalid}")
    private Long rackId;

    @NotNull(message = "{msg.catalog.order.quantity.required}")
    @Min(value = 1, message = "{msg.catalog.order.quantity.min}")
    private Integer quantity = 1;

    @Size(max = 500, message = "{msg.catalog.order.notes.size}")
    private String notes;
}
