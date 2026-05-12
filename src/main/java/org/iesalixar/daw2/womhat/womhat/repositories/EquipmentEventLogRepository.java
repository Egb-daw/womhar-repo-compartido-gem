package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.EquipmentEventLog;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link EquipmentEventLog}.
 *
 * <p>Se usa para auditoría e histórico de cambios de equipos.</p>
 */
public interface EquipmentEventLogRepository extends JpaRepository<EquipmentEventLog, Long> {

    /**
     * Devuelve el histórico de un equipo ordenado del evento más reciente al más antiguo.
     *
     * @param equipmentId id del equipo
     * @return lista de eventos
     */
    List<EquipmentEventLog> findByEquipment_IdOrderByChangedAtDesc(Long equipmentId);

    /**
     * Devuelve los eventos realizados por un usuario ordenados por fecha descendente.
     *
     * @param userId id del usuario
     * @return lista de eventos
     */
    List<EquipmentEventLog> findByChangedBy_IdOrderByChangedAtDesc(Long userId);

    /**
     * Filtra eventos por acción.
     *
     * @param action acción del log
     * @return lista filtrada
     */
    List<EquipmentEventLog> findByActionOrderByChangedAtDesc(EquipmentLogAction action);

    /**
     * Filtra eventos por tipo de evento.
     *
     * @param eventType tipo de evento
     * @return lista filtrada
     */
    List<EquipmentEventLog> findByEventTypeOrderByChangedAtDesc(EquipmentEventType eventType);

    /**
     * Recupera los últimos 10 eventos del sistema.
     *
     * @return lista de los 10 eventos más recientes
     */
    @EntityGraph(attributePaths = {"equipment", "changedBy"})
    List<EquipmentEventLog> findTop10ByOrderByChangedAtDesc();
}