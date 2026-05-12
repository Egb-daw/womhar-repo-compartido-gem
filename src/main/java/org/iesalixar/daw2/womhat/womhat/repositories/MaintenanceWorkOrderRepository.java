package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.MaintenanceWorkOrder;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderPriority;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link MaintenanceWorkOrder}.
 *
 * <p>Permite gestionar órdenes de mantenimiento y sus búsquedas habituales.</p>
 */
public interface MaintenanceWorkOrderRepository extends JpaRepository<MaintenanceWorkOrder, Long> {

    /**
     * Recupera las órdenes de un equipo ordenadas por fecha de apertura descendente.
     *
     * @param equipmentId id del equipo
     * @return lista de órdenes
     */
    List<MaintenanceWorkOrder> findByEquipment_IdOrderByOpenedAtDesc(Long equipmentId);

    /**
     * Recupera las órdenes creadas por un usuario.
     *
     * @param userId id del usuario creador
     * @return lista de órdenes
     */
    List<MaintenanceWorkOrder> findByCreatedBy_IdOrderByOpenedAtDesc(Long userId);

    /**
     * Lista órdenes por estado.
     *
     * @param status estado de la orden
     * @return lista filtrada
     */
    List<MaintenanceWorkOrder> findByStatusOrderByOpenedAtDesc(WorkOrderStatus status);

    /**
     * Lista órdenes por prioridad.
     *
     * @param priority prioridad de la orden
     * @return lista filtrada
     */
    List<MaintenanceWorkOrder> findByPriorityOrderByOpenedAtDesc(WorkOrderPriority priority);

    /**
     * Recupera una orden por id cargando equipo, creador y notas.
     *
     * @param id id de la orden
     * @return orden detallada si existe
     */
    @EntityGraph(attributePaths = {"equipment", "createdBy", "notes"})
    Optional<MaintenanceWorkOrder> findDetailedById(Long id);
}