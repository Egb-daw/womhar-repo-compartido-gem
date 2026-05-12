package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.MaintenanceNote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link MaintenanceNote}.
 *
 * <p>Gestiona las notas vinculadas a órdenes de mantenimiento.</p>
 */
public interface MaintenanceNoteRepository extends JpaRepository<MaintenanceNote, Long> {

    /**
     * Recupera las notas de una orden de mantenimiento ordenadas cronológicamente.
     *
     * @param workOrderId id de la orden
     * @return lista de notas
     */
    List<MaintenanceNote> findByWorkOrder_IdOrderByCreatedAtAsc(Long workOrderId);

    /**
     * Recupera las notas creadas por un usuario ordenadas de la más reciente a la más antigua.
     *
     * @param userId id del usuario
     * @return lista de notas
     */
    List<MaintenanceNote> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    /**
     * Recupera todas las notas de una orden cargando también la orden y el usuario creador.
     *
     * @param workOrderId id de la orden
     * @return lista de notas detalladas
     */
    @EntityGraph(attributePaths = {"workOrder", "createdBy"})
    List<MaintenanceNote> findDetailedByWorkOrder_IdOrderByCreatedAtAsc(Long workOrderId);
}