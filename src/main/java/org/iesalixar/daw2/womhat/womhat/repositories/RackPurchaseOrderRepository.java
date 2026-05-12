package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.RackPurchaseOrder;
import org.iesalixar.daw2.womhat.womhat.enums.RackPurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de pedidos públicos de racks.
 */
public interface RackPurchaseOrderRepository extends JpaRepository<RackPurchaseOrder, Long> {

    Page<RackPurchaseOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<RackPurchaseOrder> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<RackPurchaseOrder> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByRack_IdAndStatus(Long rackId, RackPurchaseOrderStatus status);

    long countByRack_IdAndStatus(Long rackId, RackPurchaseOrderStatus status);

    long countByUser_Id(Long userId);

    long countByUser_IdAndStatus(Long userId, RackPurchaseOrderStatus status);

    long countByStatus(RackPurchaseOrderStatus status);
}
