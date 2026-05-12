package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.RackPurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de lectura para pedidos del catálogo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RackPurchaseOrderDTO {

    private Long id;
    private String requesterEmail;
    private Long rackId;
    private String rackLocationLabel;
    private String rackDisplayName;
    private String rackSummary;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private RackPurchaseOrderStatus status;
    private String notes;
    private LocalDateTime createdAt;
}
